package org.jlab.btm.business.service.epics;

import gov.aps.jca.CAException;
import gov.aps.jca.configuration.Configuration;
import java.time.Duration;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.epics.gpclient.GPClientConfiguration;
import org.epics.gpclient.GPClientInstance;
import org.epics.gpclient.PVEvent;
import org.epics.gpclient.PVReader;
import org.epics.vtype.VType;

/**
 * One Channel Access context with a gpclient reader per PV.
 *
 * <p>gpclient connects each channel, subscribes to it when it connects, and resubscribes when its
 * IOC comes back, so a PV that is unavailable at startup starts updating once its IOC is reachable,
 * and one missing PV doesn't keep the others from being monitored. Every change of a PV's value or
 * connection is delivered to the sink on this session's own thread, in order.
 *
 * <p>Closing the session destroys the context, with its channels and monitors; a closed session
 * delivers no further values.
 */
final class PVMonitorSession implements AutoCloseable {
  private static final Logger LOGGER = Logger.getLogger(PVMonitorSession.class.getName());

  private final BiConsumer<String, PVCacheEntry> sink;
  private final ConfiguredCADataSource dataSource;
  private final ExecutorService notifier;
  private final ScheduledExecutorService scanner;
  private final Map<String, PVReader<VType>> readers = new LinkedHashMap<>();

  /** When each PV's latest value arrived; written on the notifier thread only. */
  private final Map<String, Date> valueTimes = new ConcurrentHashMap<>();

  private final Set<String> received = new HashSet<>();

  /** Guards readers and closed. */
  private final Object lock = new Object();

  /** Guards received; a separate lock so notifications never wait for startup or shutdown. */
  private final Object valuesLock = new Object();

  private volatile boolean closed = false;

  private PVMonitorSession(
      BiConsumer<String, PVCacheEntry> sink, ConfiguredCADataSource dataSource) {
    this.sink = sink;
    this.dataSource = dataSource;
    this.notifier = Executors.newSingleThreadExecutor(r -> daemon(r, "btm-pv-notify"));
    this.scanner = Executors.newSingleThreadScheduledExecutor(r -> daemon(r, "btm-pv-scan"));
  }

  private static Thread daemon(Runnable runnable, String name) {
    Thread thread = new Thread(runnable, name);
    thread.setDaemon(true);
    return thread;
  }

  /**
   * Creates a context and starts a reader for each PV. Doesn't wait for connections; see {@link
   * #awaitInitialValues(Duration)}.
   *
   * @param config The Channel Access client configuration
   * @param pvs The PV names
   * @param sink Receives an entry each time a PV's value or connection changes, once it has a value
   * @return The session
   * @throws CAException If the context can't be created
   */
  static PVMonitorSession open(
      Configuration config, List<String> pvs, BiConsumer<String, PVCacheEntry> sink)
      throws CAException {
    PVMonitorSession session = new PVMonitorSession(sink, new ConfiguredCADataSource(config));

    try {
      GPClientInstance client =
          new GPClientConfiguration()
              .dataSource(session.dataSource)
              .notificationExecutor(session.notifier)
              .dataProcessingThreadPool(session.scanner)
              .defaultMaxRate(Duration.ofMillis(50))
              .build();

      for (String pv : pvs) {
        PVReader<VType> reader =
            client.read(pv).addReadListener((event, r) -> session.changed(pv, event, r)).start();

        synchronized (session.lock) {
          session.readers.put(pv, reader);
        }
      }
    } catch (RuntimeException e) {
      session.close();
      throw e;
    }

    return session;
  }

  private void changed(String pv, PVEvent event, PVReader<VType> reader) {
    if (closed) {
      return;
    }

    if (event.isType(PVEvent.Type.EXCEPTION)) {
      LOGGER.log(Level.WARNING, "Channel Access problem with " + pv, event.getException());
    }

    VType value = reader.getValue();

    if (value == null) {
      return; // Connected, or failed, before any value: nothing to cache yet
    }

    boolean isValue = event.isType(PVEvent.Type.VALUE);

    Date ts = isValue ? new Date() : valueTimes.getOrDefault(pv, new Date());

    valueTimes.put(pv, ts);

    sink.accept(pv, new PVCacheEntry(value, reader.isConnected(), ts));

    if (isValue) {
      synchronized (valuesLock) {
        received.add(pv);
        valuesLock.notifyAll();
      }
    }
  }

  /**
   * Waits until every PV has delivered a value, or the timeout passes. PVs keep being connected
   * after the timeout as their IOCs become reachable.
   *
   * @param timeout The longest to wait
   * @return The PVs that haven't delivered a value yet, sorted; empty if all have
   * @throws InterruptedException If interrupted while waiting
   */
  Set<String> awaitInitialValues(Duration timeout) throws InterruptedException {
    long deadline = System.nanoTime() + timeout.toNanos();
    int expected = readerCount();

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

  private int readerCount() {
    synchronized (lock) {
      return readers.size();
    }
  }

  /**
   * @return The PVs that haven't delivered a value yet, sorted
   */
  Set<String> missingValues() {
    Set<String> missing;

    synchronized (lock) {
      missing = new TreeSet<>(readers.keySet());
    }

    synchronized (valuesLock) {
      missing.removeAll(received);
    }

    return missing;
  }

  /**
   * @return The number of PVs whose channel is connected
   */
  int connectedCount() {
    synchronized (lock) {
      return (int) readers.values().stream().filter(PVReader::isConnected).count();
    }
  }

  /**
   * @return Whether the context has been destroyed
   */
  boolean isContextDestroyed() {
    return dataSource.isContextDestroyed();
  }

  /**
   * Destroys the context, with its channels and monitors, and stops this session's threads. Calling
   * it again does nothing.
   *
   * <p>The readers aren't closed one by one first: gpclient destroys a closed reader's channel on
   * its worker thread, and CAJ abandons the rest of a context's shutdown if a channel is destroyed
   * while the context is destroying them all.
   */
  @Override
  public void close() {
    synchronized (lock) {
      if (closed) {
        return;
      }

      closed = true;
    }

    synchronized (valuesLock) {
      valuesLock.notifyAll();
    }

    try {
      dataSource.destroy();
    } catch (CAException | RuntimeException e) {
      LOGGER.log(Level.SEVERE, "Unable to destroy Channel Access context", e);
    }

    scanner.shutdownNow();
    notifier.shutdownNow();
  }
}
