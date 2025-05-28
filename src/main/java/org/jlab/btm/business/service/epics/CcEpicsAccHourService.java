package org.jlab.btm.business.service.epics;

import gov.aps.jca.dbr.DBR;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.jlab.btm.business.util.CALoadException;
import org.jlab.btm.business.util.HourUtil;
import org.jlab.btm.persistence.entity.CcAccHour;
import org.jlab.btm.persistence.epics.AcceleratorBeamAvailability;
import org.jlab.btm.persistence.epics.Constant;
import org.jlab.btm.persistence.epics.SimpleGet;

/**
 * Responsible for querying experimenter hall EPICS time accounting data.
 *
 * @author ryans
 */
@Stateless
public class CcEpicsAccHourService {

  private static final Logger logger = Logger.getLogger(CcEpicsAccHourService.class.getName());
  @EJB PVCache cache;

  /**
   * Fetches EPICS accounting information for a particular experimenter hall, optionally rounded,
   * and restricted to only a subset of the value return from an IOC query.
   *
   * <p>The accounting information returned from an EPICS IOC query is generally the past week of
   * data, up to, and including the current hour.
   *
   * <p>An empty list is returned if no data falls within the range. If EPICS does not contain data
   * for the entire range only the available data in the requested range is returned.
   *
   * @param startDayAndHour the start day and hour.
   * @param endDayAndHour the end day and hour.
   * @return a list of experimenter hall hours.
   * @throws CALoadException if unable to obtain EPICS CA data.
   */
  public List<CcAccHour> find(Date startDayAndHour, Date endDayAndHour) throws CALoadException {
    List<CcAccHour> hours;

    if (HourUtil.isInEpicsWindow(endDayAndHour)) {
      hours = loadAccounting();

      hours = HourUtil.subset(startDayAndHour, endDayAndHour, hours);

      HourRounder rounder = new HourRounder();
      rounder.roundAcceleratorHourList(hours);
    } else {
      hours = new ArrayList<>();
    }

    return hours;
  }

  /**
   * Fetches EPICS accounting information.
   *
   * <p>The accounting information returned from an EPICS IOC query is generally the past week of
   * data, up to, and including the current hour.
   *
   * @return the accounting information as a list of experimenter hall hours.
   * @throws CALoadException If unable to load EPICS data
   */
  private List<CcAccHour> loadAccounting() throws CALoadException {
    AcceleratorBeamAvailability accounting;

    accounting = getFromCache();

    return accounting.getOpAccHours();
  }

  private AcceleratorBeamAvailability getFromCache() {
    AcceleratorBeamAvailability accounting = new AcceleratorBeamAvailability();

    List<DBR> dbrs = new ArrayList<>();

    dbrs.add(cache.get(Constant.TIME_CHANNEL_NAME).getDbr());
    dbrs.add(cache.get(Constant.ACC_UP_CHANNEL_NAME).getDbr());
    dbrs.add(cache.get(Constant.ACC_SAD_CHANNEL_NAME).getDbr());
    dbrs.add(cache.get(Constant.ACC_DOWN_CHANNEL_NAME).getDbr());
    dbrs.add(cache.get(Constant.ACC_STUDIES_CHANNEL_NAME).getDbr());
    dbrs.add(cache.get(Constant.ACC_RESTORE_CHANNEL_NAME).getDbr());
    dbrs.add(cache.get(Constant.ACC_ACC_CHANNEL_NAME).getDbr());

    accounting.setTime(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setUp(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setSad(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setDown(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setStudies(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setRestore(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setAcc(SimpleGet.getDoubleValue(dbrs.remove(0)));

    return accounting;
  }
}
