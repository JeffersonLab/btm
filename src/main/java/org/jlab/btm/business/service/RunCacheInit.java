package org.jlab.btm.business.service;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Brings the runs up to date with the published schedules when the app starts, which also fills the
 * RUN table the first time. Publishing a schedule keeps them up to date after that.
 */
@Singleton
@Startup
public class RunCacheInit {
  private static final Logger LOGGER = Logger.getLogger(RunCacheInit.class.getName());

  @EJB RunService runService;

  // RunService's own transaction, so a failure there doesn't spoil this bean's start
  @PostConstruct
  @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
  void recalculate() {
    try {
      runService.recalculate();
    } catch (RuntimeException e) {
      // The app works without it; the runs stay as they were until a schedule is published
      LOGGER.log(Level.WARNING, "Unable to recalculate runs from the published schedules", e);
    }
  }
}
