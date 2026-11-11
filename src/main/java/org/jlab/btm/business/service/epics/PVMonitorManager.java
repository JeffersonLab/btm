package org.jlab.btm.business.service.epics;

import gov.aps.jca.CAException;
import gov.aps.jca.JCALibrary;
import gov.aps.jca.configuration.Configuration;
import gov.aps.jca.configuration.DefaultConfiguration;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.DependsOn;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import java.time.Duration;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.btm.persistence.epics.Constant;
import org.jlab.smoothness.persistence.enumeration.Hall;

@Singleton
@Startup
@DependsOn("PVCache")
public class PVMonitorManager {
  private static final Logger LOGGER = Logger.getLogger(PVMonitorManager.class.getName());

  @EJB PVCache cache;

  private PVMonitorSession session;

  public static final List<String> PV_LIST;

  static {
    final List<String> fixed =
        List.of(
            Constant.CREW_CHIEF_CHANNEL_NAME,
            Constant.OPERATORS_CHANNEL_NAME,
            Constant.PROGRAM_CHANNEL_NAME,
            Constant.PROGRAM_DEPUTY_CHANNEL_NAME,
            Constant.COMMENTS_CHANNEL_NAME,
            Constant.TIME_CHANNEL_NAME,
            Constant.ACC_UP_CHANNEL_NAME,
            Constant.ACC_SAD_CHANNEL_NAME,
            Constant.ACC_DOWN_CHANNEL_NAME,
            Constant.ACC_STUDIES_CHANNEL_NAME,
            Constant.ACC_RESTORE_CHANNEL_NAME,
            Constant.ACC_ACC_CHANNEL_NAME,
            Constant.MULTI_ONE_UP,
            Constant.MULTI_TWO_UP,
            Constant.MULTI_THREE_UP,
            Constant.MULTI_FOUR_UP,
            Constant.MULTI_ANY_UP,
            Constant.MULTI_ALL_UP,
            Constant.MULTI_DOWN);

    final List<String> dynamic = new ArrayList<>();

    for (Hall hall : Hall.values()) {
      dynamic.add(Constant.HALL_PREFIX + hall + Constant.HALL_UP_SUFFIX);
      dynamic.add(Constant.HALL_PREFIX + hall + Constant.HALL_TUNE_SUFFIX);
      dynamic.add(Constant.HALL_PREFIX + hall + Constant.HALL_BNR_SUFFIX);
      dynamic.add(Constant.HALL_PREFIX + hall + Constant.HALL_DOWN_SUFFIX);
      dynamic.add(Constant.HALL_PREFIX + hall + Constant.HALL_OFF_SUFFIX);

      dynamic.add(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_TIME_SUFFIX);
      dynamic.add(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_ABU_SUFFIX);
      dynamic.add(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_BANU_SUFFIX);
      dynamic.add(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_BNA_SUFFIX);
      dynamic.add(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_ER_SUFFIX);
      dynamic.add(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_PCC_SUFFIX);
      dynamic.add(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_UED_SUFFIX);
      dynamic.add(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_OFF_SUFFIX);

      if ("true".equals(System.getenv("CA_IOC_LIVENESS"))) {
        dynamic.add(
            Constant.EXP_HALL_LIVENESS_PREFIX
                + hall.toString().toLowerCase()
                + Constant.EXP_HALL_LIVENESS_SUFFIX);
      }
    }

    dynamic.addAll(fixed);

    PV_LIST = Collections.unmodifiableList(dynamic);
  }

  /** How long startup waits for the PVs' first values before carrying on without them. */
  static final Duration STARTUP_TIMEOUT = Duration.ofSeconds(10);

  @PostConstruct
  public void start() {
    try {
      session = PVMonitorSession.open(createConfiguration(), PV_LIST, cache::put);

      Set<String> missing = session.awaitInitialValues(STARTUP_TIMEOUT);

      if (!missing.isEmpty()) {
        LOGGER.log(
            Level.WARNING,
            "No value yet from {0} of {1} PVs after {2} seconds; monitoring them once they connect:"
                + " {3}",
            new Object[] {missing.size(), PV_LIST.size(), STARTUP_TIMEOUT.toSeconds(), missing});
      }
    } catch (CAException e) { // @PostConstruct isn't supposed to throw checked exceptions...
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      LOGGER.log(Level.WARNING, "Interrupted while waiting for initial PV values", e);
    }
  }

  @PreDestroy
  public void stop() {
    if (session != null) {
      session.close();
      session = null;
    }
  }

  private Configuration createConfiguration() {
    DefaultConfiguration config = new DefaultConfiguration("monitor-context");

    String addrList = System.getenv("BTM_EPICS_ADDR_LIST");

    if (addrList == null || addrList.isBlank()) {
      throw new IllegalArgumentException("BTM_EPICS_ADDR_LIST must not be empty");
    }

    config.setAttribute("addr_list", addrList);
    config.setAttribute("auto_addr_list", "false");
    config.setAttribute("class", JCALibrary.CHANNEL_ACCESS_JAVA);

    return config;
  }

  @RolesAllowed({"cc", "btm-admin"})
  public void reset() {
    stop();
    start();
  }
}
