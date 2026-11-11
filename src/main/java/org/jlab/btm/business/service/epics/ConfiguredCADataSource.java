package org.jlab.btm.business.service.epics;

import com.cosylab.epics.caj.CAJContext;
import gov.aps.jca.CAException;
import gov.aps.jca.Context;
import gov.aps.jca.JCALibrary;
import gov.aps.jca.configuration.Configuration;
import org.epics.gpclient.datasource.ca.CADataSource;

/**
 * A gpclient Channel Access data source whose context uses the given configuration.
 *
 * <p>gpclient 1.0.9's {@link CADataSource} always creates its context from the JVM-wide JCA
 * properties, and doesn't shut down its worker thread when closed. This subclass discards that
 * context, which CAJ hasn't initialized yet (no sockets or threads), and substitutes one made from
 * an explicit configuration, so each session has its own address list and ports.
 */
final class ConfiguredCADataSource extends CADataSource {
  private final Context context;

  /**
   * @param config The Channel Access client configuration
   * @throws CAException If the context can't be created
   */
  ConfiguredCADataSource(Configuration config) throws CAException {
    super();
    super.getContext().dispose();

    try {
      context = JCALibrary.getInstance().createContext(config);
    } catch (CAException | RuntimeException e) {
      exec.shutdownNow();
      throw e;
    }

    if (context instanceof CAJContext caj) {
      caj.setDoNotShareChannels(true); // As CADataSource does for its own context
    }
  }

  @Override
  public Context getContext() {
    return context;
  }

  /**
   * @return Whether the context has been destroyed
   */
  boolean isContextDestroyed() {
    return context instanceof CAJContext caj && caj.isDestroyed();
  }

  /**
   * Destroys the context, with its channels and monitors, and stops the worker thread.
   *
   * @throws CAException If the context can't be destroyed
   */
  void destroy() throws CAException {
    try {
      context.destroy();
    } finally {
      exec.shutdownNow();
    }
  }

  @Override
  public void close() {
    try {
      destroy();
    } catch (CAException | RuntimeException e) {
      throw new IllegalStateException("Unable to destroy Channel Access context", e);
    }
  }
}
