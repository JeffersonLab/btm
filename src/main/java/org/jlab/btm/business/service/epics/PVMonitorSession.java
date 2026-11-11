package org.jlab.btm.business.service.epics;

import com.cosylab.epics.caj.CAJContext;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.JCALibrary;
import gov.aps.jca.Monitor;
import gov.aps.jca.configuration.Configuration;
import gov.aps.jca.event.ConnectionEvent;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * One Channel Access context with a channel and a value monitor per PV.
 *
 * <p>A channel's monitor is registered from its connection callback, the first time it connects, so
 * a PV that is unavailable at startup starts updating once its IOC is reachable, and one missing PV
 * doesn't keep the others from being monitored. CAJ re-subscribes existing monitors when a channel
 * reconnects, so each channel gets at most one monitor for the life of the session.
 *
 * <p>Closing the session clears the monitors and destroys the channels and the context; a closed
 * session registers no further monitors and delivers no further values.
 */
final class PVMonitorSession implements AutoCloseable {
  private static final Logger LOGGER = Logger.getLogger(PVMonitorSession.class.getName());

  private final BiConsumer<String, PVCacheEntry> sink;
  private final CAJContext context;
  private final Map<String, Channel> channels = new LinkedHashMap<>();
  private final Map<String, Monitor> monitors = new HashMap<>();
  private final Set<String> received = new HashSet<>();

  /** Guards channels, monitors and closed; held while registering a monitor. */
  private final Object lock = new Object();

  /** Guards received; a separate lock so value callbacks never wait for a registration. */
  private final Object valuesLock = new Object();

  private volatile boolean closed = false;

  private PVMonitorSession(BiConsumer<String, PVCacheEntry> sink, CAJContext context) {
    this.sink = sink;
    this.context = context;
  }

  /**
   * Creates a context and a channel for each PV, and asks Channel Access to connect them. Doesn't
   * wait for connections; see {@link #awaitInitialValues(Duration)}.
   *
   * @param config The Channel Access client configuration
   * @param pvs The PV names
   * @param sink Receives each monitor update
   * @return The session
   * @throws CAException If the context or a channel can't be created; the session is then closed
   */
  static PVMonitorSession open(
      Configuration config, List<String> pvs, BiConsumer<String, PVCacheEntry> sink)
      throws CAException {
    CAJContext context = (CAJContext) JCALibrary.getInstance().createContext(config);
    PVMonitorSession session = new PVMonitorSession(sink, context);

    try {
      for (String pv : pvs) {
        Channel channel = context.createChannel(pv, event -> session.connectionChanged(pv, event));

        synchronized (session.lock) {
          session.channels.put(pv, channel);
        }
      }

      context.flushIO();
    } catch (CAException | RuntimeException e) {
      session.close();
      throw e;
    }

    return session;
  }

  private void connectionChanged(String pv, ConnectionEvent event) {
    if (!event.isConnected()) {
      return;
    }

    Channel channel = (Channel) event.getSource();

    synchronized (lock) {
      if (closed || monitors.containsKey(pv)) {
        return;
      }

      try {
        Monitor monitor =
            channel.addMonitor(
                channel.getFieldType(),
                channel.getElementCount(),
                Monitor.VALUE,
                ev -> update(pv, new PVCacheEntry(ev.getStatus(), ev.getDBR(), new Date())));

        monitors.put(pv, monitor);

        context.flushIO();
      } catch (CAException | RuntimeException e) {
        LOGGER.log(Level.WARNING, "Unable to monitor " + pv, e);
      }
    }
  }

  private void update(String pv, PVCacheEntry entry) {
    if (closed) {
      return;
    }

    sink.accept(pv, entry);

    synchronized (valuesLock) {
      received.add(pv);
      valuesLock.notifyAll();
    }
  }

  /**
   * Waits until every PV has delivered a value, or the timeout passes. Monitors keep being
   * registered after the timeout as channels connect.
   *
   * @param timeout The longest to wait
   * @return The PVs that haven't delivered a value yet, sorted; empty if all have
   * @throws InterruptedException If interrupted while waiting
   */
  Set<String> awaitInitialValues(Duration timeout) throws InterruptedException {
    long deadline = System.nanoTime() + timeout.toNanos();
    int expected = channelCount();

    synchronized (valuesLock) {
      while (!closed && received.size() < expected) {
        long remainingMillis = Duration.ofNanos(deadline - System.nanoTime()).toMillis();

        if (remainingMillis <= 0) {
          break;
        }

        valuesLock.wait(remainingMillis);
      }
    }

    return missingValues();
  }

  private int channelCount() {
    synchronized (lock) {
      return channels.size();
    }
  }

  /**
   * @return The PVs that haven't delivered a value yet, sorted
   */
  Set<String> missingValues() {
    Set<String> missing;

    synchronized (lock) {
      missing = new TreeSet<>(channels.keySet());
    }

    synchronized (valuesLock) {
      missing.removeAll(received);
    }

    return missing;
  }

  /**
   * @return The number of monitors registered
   */
  int monitorCount() {
    synchronized (lock) {
      return monitors.size();
    }
  }

  /**
   * @return Whether the context has been destroyed
   */
  boolean isContextDestroyed() {
    return context.isDestroyed();
  }

  /**
   * Clears the monitors and destroys the channels and the context. Logs and continues past a
   * failure to clear or destroy one of them, so the context is always destroyed. Calling it again
   * does nothing.
   */
  @Override
  public void close() {
    List<Monitor> monitorsToClear;
    List<Channel> channelsToDestroy;

    synchronized (lock) {
      if (closed) {
        return;
      }

      closed = true;

      monitorsToClear = new ArrayList<>(monitors.values());
      channelsToDestroy = new ArrayList<>(channels.values());
    }

    synchronized (valuesLock) {
      valuesLock.notifyAll();
    }

    // Outside the lock: a connection or monitor callback may be waiting for it on a CA thread.
    for (Monitor monitor : monitorsToClear) {
      try {
        monitor.clear();
      } catch (CAException | RuntimeException e) {
        LOGGER.log(Level.WARNING, "Unable to clear monitor", e);
      }
    }

    for (Channel channel : channelsToDestroy) {
      try {
        channel.destroy();
      } catch (CAException | RuntimeException e) {
        LOGGER.log(Level.WARNING, "Unable to destroy channel " + channel.getName(), e);
      }
    }

    try {
      context.destroy();
    } catch (CAException | RuntimeException e) {
      LOGGER.log(Level.SEVERE, "Unable to destroy Channel Access context", e);
    }
  }
}
