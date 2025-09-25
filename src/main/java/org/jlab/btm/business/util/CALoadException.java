package org.jlab.btm.business.util;

import jakarta.ejb.ApplicationException;

/**
 * Signal that request could not load EPICS CA data.
 *
 * <p>Unlike a more generic UserFriendlyException, this one has rollback = false so that it can be
 * caught and logged, and otherwise ignored. Otherwise, the Container will rollback the transaction.
 */
@ApplicationException(inherited = true, rollback = false)
public class CALoadException extends Exception {
  /**
   * Create a new CALoadException with the provided message.
   *
   * @param msg The message
   */
  public CALoadException(String msg) {
    super(msg);
  }

  /**
   * Create a new CALoadException with the provided message and cause.
   *
   * @param msg The message
   * @param cause The cause
   */
  public CALoadException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
