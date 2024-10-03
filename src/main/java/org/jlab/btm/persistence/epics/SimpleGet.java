package org.jlab.btm.persistence.epics;

import gov.aps.jca.*;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DOUBLE;
import gov.aps.jca.dbr.INT;
import gov.aps.jca.dbr.STRING;
import gov.aps.jca.event.ConnectionEvent;
import gov.aps.jca.event.ConnectionListener;
import gov.aps.jca.event.GetEvent;
import gov.aps.jca.event.GetListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a simple set of EPICS data access methods.
 *
 * <p>This class wraps around the EPICS CAJ API and provides a slightly higher level API for
 * querying for EPICS data.
 *
 * @author ryans
 */
public class SimpleGet {

  /** The default timeout is five seconds. */
  public static final int DEFAULT_TIMEOUT_MILLI = 1000;

  private static final Logger logger = Logger.getLogger(SimpleGet.class.getName());

  /**
   * Wait for a connection to be created before returning and use the specified timeout.
   *
   * @param conListener the connection listener.
   * @param timeout the timeout.
   * @throws InterruptedException if the waiting thread is interrupted before it is notified.
   * @throws TimeoutException if the waiting has exceeded the timeout period.
   */
  public static void waitForConnection(SimpleConnectionListener conListener, int timeout)
      throws InterruptedException, TimeoutException {
    synchronized (conListener) {
      if (!conListener.isConnected()) {
        conListener.wait(timeout);
      }
    }

    if (!conListener.isConnected()) {
      throw new TimeoutException("Unable to connect to channel: timeout reached");
    }
  }

  /**
   * A convenience method which waits for a connection to be created before returning and uses a
   * default timeout.
   *
   * @param conListener the connection listener.
   * @throws InterruptedException if the waiting thread is interrupted before it is notified.
   * @throws TimeoutException if the waiting has exceeded the timeout period.
   */
  public static void waitForConnection(SimpleConnectionListener conListener)
      throws InterruptedException, TimeoutException {
    waitForConnection(conListener, DEFAULT_TIMEOUT_MILLI);
  }

  /**
   * Wait for data to be transfered before returning and use the specified timeout.
   *
   * @param getListener the get listener.
   * @param timeout the timeout.
   * @throws InterruptedException if the waiting thread is interrupted before it is notified.
   * @throws TimeoutException if the waiting has exceeded the timeout period.
   * @throws CAException if a problem occurs transferring the EPICS data.
   */
  public static void waitForData(SimpleGetListener getListener, int timeout)
      throws InterruptedException, TimeoutException, CAException {
    synchronized (getListener) {
      if (getListener.getStatus() == null) {
        getListener.wait(timeout);
      }
    }

    if (getListener.getStatus() == null) {
      throw new TimeoutException("Unable to get data: timeout reached");
    } else if (getListener.getStatus() != CAStatus.NORMAL) {
      throw new CAException("Unable to get data: non-normal status: " + getListener.getStatus());
    }
  }

  /**
   * A convenience method which waits for data to be transfered before returning and uses the
   * default timeout.
   *
   * @param getListener the get listener.
   * @throws InterruptedException if the waiting thread is interrupted before it is notified.
   * @throws TimeoutException if the waiting has exceeded the timeout period.
   * @throws CAException if a problem occurs transferring the EPICS data.
   */
  public static void waitForData(SimpleGetListener getListener)
      throws InterruptedException, TimeoutException, CAException {
    waitForData(getListener, DEFAULT_TIMEOUT_MILLI);
  }

  /**
   * Perform a CA Get on a single channel performed asynchronously internally and using the
   * specified timeout.
   *
   * @param context the context.
   * @param channelName the channel name.
   * @param timeout the timeout.
   * @return the EPICS record.
   * @throws InterruptedException if the waiting thread is interrupted before it is notified.
   * @throws TimeoutException if the waiting has exceeded the timeout period.
   * @throws CAException if a problem occurs transferring the EPICS data.
   */
  public static DBR doAsyncGet(Context context, String channelName, int timeout)
      throws InterruptedException, TimeoutException, CAException {
    Channel channel = null;
    SimpleConnectionListener conListener = new SimpleConnectionListener();
    SimpleGetListener getListener = new SimpleGetListener();

    DBR dbr = null;

    try {
      channel = context.createChannel(channelName, conListener);

      waitForConnection(conListener, timeout);

      channel.get(getListener);

      context.flushIO();

      waitForData(getListener, timeout);

      dbr = getListener.getDBR();
    } finally {
      if (channel != null) {
        channel.destroy();
      }
    }

    return dbr;
  }

  /**
   * A convenience method which is performed a CA Get on a single channel, is performed
   * asynchronously internally, and uses the default timeout.
   *
   * @param context the context.
   * @param channelName the channel name.
   * @return the EPICS record.
   * @throws InterruptedException if the waiting thread is interrupted before it is notified.
   * @throws TimeoutException if the waiting has exceeded the timeout period.
   * @throws CAException if a problem occurs transferring the EPICS data.
   */
  public static DBR doAsyncGet(Context context, String channelName)
      throws InterruptedException, TimeoutException, CAException {
    return doAsyncGet(context, channelName, DEFAULT_TIMEOUT_MILLI);
  }

  /**
   * Perform a CA Get on multiple channels performed asynchronously internally and using the
   * specified timeout.
   *
   * @param context the context.
   * @param channelNames the channel names.
   * @param timeout the timeout.
   * @return the EPICS records.
   * @throws InterruptedException if the waiting thread is interrupted before it is notified.
   * @throws TimeoutException if the waiting has exceeded the timeout period.
   * @throws CAException if a problem occurs transferring the EPICS data.
   */
  public static List<DBR> doAsyncGet(Context context, List<String> channelNames, int timeout)
      throws InterruptedException, TimeoutException, CAException {
    List<Channel> channels = new ArrayList<Channel>();
    List<SimpleConnectionListener> conListeners = new ArrayList<SimpleConnectionListener>();
    List<SimpleGetListener> getListeners = new ArrayList<SimpleGetListener>();
    List<DBR> dbrs = new ArrayList<DBR>();

    try {
      for (String s : channelNames) {
        SimpleConnectionListener l = new SimpleConnectionListener();
        conListeners.add(l);

        Channel c = context.createChannel(s, l);
        channels.add(c);
      }

      for (SimpleConnectionListener l : conListeners) {
        waitForConnection(l, timeout);
      }

      for (Channel c : channels) {
        SimpleGetListener g = new SimpleGetListener();
        getListeners.add(g);

        c.get(g);
      }

      context.flushIO();

      for (SimpleGetListener g : getListeners) {
        waitForData(g, timeout);

        DBR dbr = g.getDBR();
        dbrs.add(dbr);
      }
    } finally {
      boolean destroyException = false;
      for (Channel c : channels) {
        if (c != null) {
          try {
            c.destroy();
          } catch (CAException e) {
            logger.log(Level.SEVERE, "Unable to destroy channel", e);
            destroyException = true;
          }
        }
      }

      if (destroyException) {
        throw new CAException("Unable to destroy all channels");
      }
    }

    return dbrs;
  }

  /**
   * A convenience method to perform a CA Get on multiple channels performed asynchronously
   * internally and using the default timeout.
   *
   * @param context the context.
   * @param channelNames the channel names.
   * @return the EPICS records.
   * @throws InterruptedException if the waiting thread is interrupted before it is notified.
   * @throws TimeoutException if the waiting has exceeded the timeout period.
   * @throws CAException if a problem occurs transferring the EPICS data.
   */
  public static List<DBR> doAsyncGet(Context context, List<String> channelNames)
      throws InterruptedException, TimeoutException, CAException {
    return doAsyncGet(context, channelNames, DEFAULT_TIMEOUT_MILLI);
  }

  /**
   * Perform a CA Get on a single channel performed synchronously internally and using the specified
   * timeout.
   *
   * @param context the context.
   * @param channelName the channel name.
   * @param timeout the timeout.
   * @return the EPICS record.
   * @throws InterruptedException if the waiting thread is interrupted before it is notified.
   * @throws TimeoutException if the waiting has exceeded the timeout period.
   * @throws CAException if a problem occurs transferring the EPICS data.
   */
  public static DBR doSyncGet(Context context, String channelName, int timeout)
      throws InterruptedException, TimeoutException, CAException {
    Channel channel = null;

    DBR dbr;

    try {
      channel = context.createChannel(channelName);

      context.pendIO(timeout);

      dbr = channel.get();

      context.pendIO(timeout);
    } finally {
      if (channel != null) {
        channel.destroy();
      }
    }

    return dbr;
  }

  /**
   * Perform a CA Get on a single channel performed synchronously internally and using the default
   * timeout.
   *
   * @param context the context.
   * @param channelName the channel name.
   * @return the EPICS record.
   * @throws InterruptedException if the waiting thread is interrupted before it is notified.
   * @throws TimeoutException if the waiting has exceeded the timeout period.
   * @throws CAException if a problem occurs transferring the EPICS data.
   */
  public static DBR doSyncGet(Context context, String channelName)
      throws InterruptedException, TimeoutException, CAException {
    return doSyncGet(context, channelName, DEFAULT_TIMEOUT_MILLI);
  }

  /**
   * A convenience method to get the data array out of the EPICS record as type double.
   *
   * @param dbr the EPICS record.
   * @return the data array.
   */
  public static double[] getDoubleValue(DBR dbr) {
    return ((DOUBLE) dbr).getDoubleValue();
  }

  /**
   * A convenience method to get the data array out of the EPICS record as type int.
   *
   * @param dbr the EPICS record.
   * @return the data array.
   */
  public static int[] getIntValue(DBR dbr) {
    return ((INT) dbr).getIntValue();
  }

  /**
   * A convenience method to get the data array out of the EPICS record as type string.
   *
   * @param dbr the EPICS record.
   * @return the data array.
   */
  public static String[] getStringValue(DBR dbr) {
    return ((STRING) dbr).getStringValue();
  }

  /**
   * A static nested class for handling simple get notifications.
   *
   * @author ryans
   */
  public static class SimpleGetListener implements GetListener {

    private DBR dbr = null;
    private CAStatus status = null;

    @Override
    public synchronized void getCompleted(GetEvent event) {
      status = event.getStatus();
      dbr = event.getDBR();

      this.notifyAll();
    }

    public synchronized CAStatus getStatus() {
      return status;
    }

    public synchronized DBR getDBR() {
      return dbr;
    }
  }

  /**
   * A static nested class for handling simple connection notifications.
   *
   * @author ryans
   */
  public static class SimpleConnectionListener implements ConnectionListener {

    private boolean connected = false;

    @Override
    public synchronized void connectionChanged(ConnectionEvent event) {
      connected = event.isConnected();

      this.notifyAll();
    }

    public synchronized boolean isConnected() {
      return connected;
    }
  }
}
