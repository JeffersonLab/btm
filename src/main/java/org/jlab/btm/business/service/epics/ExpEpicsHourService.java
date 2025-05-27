package org.jlab.btm.business.service.epics;

import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.jlab.btm.business.util.HourUtil;
import org.jlab.btm.persistence.entity.ExpHour;
import org.jlab.btm.persistence.epics.*;
import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * Responsible for querying experimenter hall EPICS time accounting data.
 *
 * @author ryans
 */
@Stateless
public class ExpEpicsHourService {

  @EJB PVCache cache;

  private static final Logger logger = Logger.getLogger(ExpEpicsHourService.class.getName());

  /**
   * Fetches EPICS accounting information for a particular experimenter hall, optionally rounded,
   * and restricted to only a subset of the value return from an IOC query.
   *
   * <p>The accounting information returned from an EPICS IOC query is generally the past week of
   * data, up to, and including the current hour.
   *
   * <p>Rounding, if enabled, modifies the two mutually exclusive sets experimenter and accelerator
   * time accounting statuses such that they each sum to exactly one hour.
   *
   * <p>An empty list is returned if no data falls within the range. If EPICS does not contain data
   * for the entire range only the available data in the requested range is returned.
   *
   * @param hall the experimenter hall.
   * @param startDayAndHour the start day and hour.
   * @param endDayAndHour the end day and hour.
   * @param round whether or not to round time.
   * @return a list of experimenter hall hours.
   * @throws TimeoutException if a network request takes too long.
   * @throws InterruptedException if a thread gets unexpectedly interrupted.
   * @throws CAException if a channel access problem occurs.
   */
  public List<ExpHour> loadAccounting(
      Hall hall, Date startDayAndHour, Date endDayAndHour, boolean round)
      throws TimeoutException, InterruptedException, CAException {
    List<ExpHour> hours = null;

    if (HourUtil.isInEpicsWindow(endDayAndHour)) {
      hours = loadAccounting(hall);

      hours = HourUtil.subset(startDayAndHour, endDayAndHour, hours);

      if (round) {
        HourRounder rounder = new HourRounder();
        rounder.roundExpHourList(hours);
      }
    } else {
      hours = new ArrayList<ExpHour>();
    }

    return hours;
  }

  /**
   * Fetches EPICS accounting information for a particular experimenter hall.
   *
   * <p>The accounting information returned from an EPICS IOC query is generally the past week of
   * data, up to, and including the current hour.
   *
   * @param hall The experimenter hall.
   * @return the accounting information as a list of experimenter hall hours.
   * @throws TimeoutException if a network request takes too long.
   * @throws InterruptedException if a thread gets unexpectedly interrupted.
   * @throws CAException if a channel access problem occurs.
   */
  public List<ExpHour> loadAccounting(Hall hall)
      throws TimeoutException, InterruptedException, CAException {
    logger.log(Level.FINEST, "EpicsDataSource.loadAccounting.hall: {}", hall);

    ExperimenterAccounting accounting = null;

    long start = System.currentTimeMillis();
    accounting = getFromCache(hall);
    long end = System.currentTimeMillis();
    logger.log(Level.FINEST, "EPICS load time (milliseconds): {}", (end - start));

    return accounting.getExpHallHours();
  }

  private ExperimenterAccounting getFromCache(Hall hall) {
    ExperimenterAccounting accounting = new ExperimenterAccounting();
    accounting.setHall(hall);

    List<DBR> dbrs = new ArrayList<>();

    dbrs.add(cache.get(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_TIME_SUFFIX));
    dbrs.add(cache.get(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_ABU_SUFFIX));
    dbrs.add(cache.get(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_BANU_SUFFIX));
    dbrs.add(cache.get(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_BNA_SUFFIX));
    dbrs.add(cache.get(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_ACC_SUFFIX));
    dbrs.add(cache.get(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_ER_SUFFIX));
    dbrs.add(cache.get(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_PCC_SUFFIX));
    dbrs.add(cache.get(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_UED_SUFFIX));
    dbrs.add(cache.get(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_OFF_SUFFIX));

    accounting.setTime(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setABU(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setBANU(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setBNA(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setACC(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setER(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setPCC(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setUED(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setOFF(SimpleGet.getDoubleValue(dbrs.remove(0)));

    return accounting;
  }
}
