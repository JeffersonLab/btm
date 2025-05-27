package org.jlab.btm.business.service.epics;

import com.cosylab.epics.caj.CAJChannel;
import com.cosylab.epics.caj.CAJContext;
import gov.aps.jca.CAException;
import gov.aps.jca.JCALibrary;
import gov.aps.jca.Monitor;
import gov.aps.jca.configuration.DefaultConfiguration;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import java.util.ArrayList;
import java.util.List;
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
  @EJB PVCache cache;

  private CAJContext context;
  private List<CAJChannel> channels;
  private List<Monitor> monitors;

  public static final List<String> PV_LIST =
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
          Constant.HALL_PREFIX + Hall.A + Constant.HALL_UP_SUFFIX,
          Constant.HALL_PREFIX + Hall.A + Constant.HALL_TUNE_SUFFIX,
          Constant.HALL_PREFIX + Hall.A + Constant.HALL_BNR_SUFFIX,
          Constant.HALL_PREFIX + Hall.A + Constant.HALL_DOWN_SUFFIX,
          Constant.HALL_PREFIX + Hall.A + Constant.HALL_OFF_SUFFIX,
          Constant.MULTI_ONE_UP,
          Constant.MULTI_TWO_UP,
          Constant.MULTI_THREE_UP,
          Constant.MULTI_FOUR_UP,
          Constant.MULTI_ANY_UP,
          Constant.MULTI_ALL_UP,
          Constant.MULTI_DOWN,
          Constant.EXP_HALL_PREFIX + Hall.A + Constant.EXP_TIME_SUFFIX,
          Constant.EXP_HALL_PREFIX + Hall.A + Constant.EXP_ABU_SUFFIX,
          Constant.EXP_HALL_PREFIX + Hall.A + Constant.EXP_BANU_SUFFIX,
          Constant.EXP_HALL_PREFIX + Hall.A + Constant.EXP_BNA_SUFFIX,
          Constant.EXP_HALL_PREFIX + Hall.A + Constant.EXP_ACC_SUFFIX,
          Constant.EXP_HALL_PREFIX + Hall.A + Constant.EXP_ER_SUFFIX,
          Constant.EXP_HALL_PREFIX + Hall.A + Constant.EXP_PCC_SUFFIX,
          Constant.EXP_HALL_PREFIX + Hall.A + Constant.EXP_UED_SUFFIX,
          Constant.EXP_HALL_PREFIX + Hall.A + Constant.EXP_OFF_SUFFIX);

  @PostConstruct
  public void startMonitors() throws CAException {
    context = createContext();

    channels = new ArrayList<>();
    monitors = new ArrayList<>();

    for (String pv : PV_LIST) {
      CAJChannel channel = (CAJChannel) context.createChannel(pv);

      channels.add(channel);

      Monitor monitor =
          channel.addMonitor(
              channel.getFieldType(),
              1,
              Monitor.VALUE,
              new MonitorListener() {

                @Override
                public void monitorChanged(MonitorEvent ev) {
                  DBR dbr = ev.getDBR();
                  cache.put(pv, dbr);
                }
              });

      monitors.add(monitor);
    }
  }

  @PreDestroy
  public void stopMonitors() throws CAException {
    for (Monitor monitor : monitors) {
      monitor.clear();
    }

    for (CAJChannel channel : channels) {
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
