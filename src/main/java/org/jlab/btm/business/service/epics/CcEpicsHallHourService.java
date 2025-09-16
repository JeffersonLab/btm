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
import org.jlab.btm.persistence.entity.CcHallHour;
import org.jlab.btm.persistence.epics.*;
import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * Responsible for querying experimenter hall EPICS time accounting data.
 *
 * @author ryans
 */
@Stateless
public class CcEpicsHallHourService {

  private static final Logger logger = Logger.getLogger(CcEpicsHallHourService.class.getName());
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
   * @param hall
   * @param startDayAndHour the start day and hour.
   * @param endDayAndHour the end day and hour.
   * @return a list of experimenter hall hours.
   * @throws CALoadException If unable to load CA data
   */
  public List<CcHallHour> find(Hall hall, Date startDayAndHour, Date endDayAndHour)
      throws CALoadException {
    List<CcHallHour> hours;

    if (HourUtil.isInEpicsWindow(endDayAndHour)) {
      hours = loadAccounting(hall);

      hours = HourUtil.subset(startDayAndHour, endDayAndHour, hours);

      HourRounder rounder = new HourRounder();
      rounder.roundHallHourList(hours);
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
   * @throws CALoadException If unable to load CA data
   */
  private List<CcHallHour> loadAccounting(Hall hall) throws CALoadException {

    HallBeamAvailability accounting;

    accounting = getFromCache(hall);

    return accounting.getOpHallHours();
  }

  private HallBeamAvailability getFromCache(Hall hall) throws CALoadException {
    HallBeamAvailability accounting = new HallBeamAvailability();

    List<DBR> dbrs = new ArrayList<>();

    dbrs.add(cache.getOrThrow(Constant.TIME_CHANNEL_NAME).getDbr());
    dbrs.add(cache.getOrThrow(Constant.HALL_PREFIX + hall + Constant.HALL_UP_SUFFIX).getDbr());
    dbrs.add(cache.getOrThrow(Constant.HALL_PREFIX + hall + Constant.HALL_TUNE_SUFFIX).getDbr());
    dbrs.add(cache.getOrThrow(Constant.HALL_PREFIX + hall + Constant.HALL_BNR_SUFFIX).getDbr());
    dbrs.add(cache.getOrThrow(Constant.HALL_PREFIX + hall + Constant.HALL_DOWN_SUFFIX).getDbr());
    dbrs.add(cache.getOrThrow(Constant.HALL_PREFIX + hall + Constant.HALL_OFF_SUFFIX).getDbr());

    accounting.setTime(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setUp(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setTune(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setBnr(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setDown(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setOff(SimpleGet.getDoubleValue(dbrs.remove(0)));

    return accounting;
  }
}
