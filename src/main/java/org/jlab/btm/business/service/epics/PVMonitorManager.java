package org.jlab.btm.business.service.epics;

import com.cosylab.epics.caj.CAJChannel;
import com.cosylab.epics.caj.CAJContext;
import gov.aps.jca.*;
import gov.aps.jca.configuration.DefaultConfiguration;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.jlab.btm.persistence.epics.Constant;
import org.jlab.smoothness.persistence.enumeration.Hall;

@Singleton
@Startup
@DependsOn("PVCache")
public class PVMonitorManager {
  private static final Logger LOGGER = Logger.getLogger(PVMonitorManager.class.getName());

  @EJB PVCache cache;

  private CAJContext context;
  private Map<String, CAJChannel> channels;
  private Map<String, Monitor> monitors;

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
      dynamic.add(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_ACC_SUFFIX);
      dynamic.add(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_ER_SUFFIX);
      dynamic.add(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_PCC_SUFFIX);
      dynamic.add(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_UED_SUFFIX);
      dynamic.add(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_OFF_SUFFIX);
    }

    dynamic.addAll(fixed);

    PV_LIST = Collections.unmodifiableList(dynamic);
  }

  @PostConstruct
  public void start() {
    try {
      startMonitors();
    } catch (CAException
        | TimeoutException e) { // @PostConstruct isn't supposed to throw checked exceptions...
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  @PreDestroy
  public void stop() {
    try {
      stopMonitors();
    } catch (CAException e) { // @PreDestroy isn't supposed to throw exceptions...
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  public void startMonitors() throws CAException, TimeoutException {
    context = createContext();

    channels = new HashMap<>();
    monitors = new HashMap<>();

    for (String pv : PV_LIST) {
      CAJChannel channel = (CAJChannel) context.createChannel(pv);

      channels.put(pv, channel);
    }

    context.pendIO(10000);

    for (String pv : PV_LIST) {
      CAJChannel channel = channels.get(pv);

      Monitor monitor =
          channel.addMonitor(
              channel.getFieldType(),
              channel.getElementCount(),
              Monitor.VALUE,
              new MonitorListener() {

                @Override
                public void monitorChanged(MonitorEvent ev) {
                  CAStatus status = ev.getStatus();
                  DBR dbr = ev.getDBR();
                  cache.put(pv, new PVCacheEntry(status, dbr, new Date()));
                }
              });

      monitors.put(pv, monitor);
    }

    context.pendIO(10000);
  }

  public void stopMonitors() throws CAException {
    for (Monitor monitor : monitors.values()) {
      monitor.clear();
    }

    for (CAJChannel channel : channels.values()) {
      context.destroyChannel(channel, true);
    }

    context.destroy();
  }

  private CAJContext createContext() throws CAException {
    DefaultConfiguration config = new DefaultConfiguration("monitor-context");

    String addrList = System.getenv("BTM_EPICS_ADDR_LIST");

    if (addrList == null || addrList.isBlank()) {
      throw new IllegalArgumentException("BTM_EPICS_ADDR_LIST must not be empty");
    }

    config.setAttribute("addr_list", addrList);
    config.setAttribute("auto_addr_list", "false");
    config.setAttribute("class", JCALibrary.CHANNEL_ACCESS_JAVA);

    return (CAJContext) JCALibrary.getInstance().createContext(config);
  }
}
