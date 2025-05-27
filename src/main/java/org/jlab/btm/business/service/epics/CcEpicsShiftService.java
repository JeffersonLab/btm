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
import org.jlab.btm.persistence.entity.CcShift;
import org.jlab.btm.persistence.epics.*;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.enumeration.Shift;

/**
 * Responsible for querying experimenter hall EPICS time accounting data.
 *
 * @author ryans
 */
@Stateless
public class CcEpicsShiftService {

  private static final Logger logger = Logger.getLogger(CcEpicsShiftService.class.getName());
  @EJB PVCache cache;

  private boolean isCurrentLastOrNextShiftStart(Date startDayAndHour) {
    Date now = new Date();
    Date day =
        TimeUtil.getCurrentCrewChiefShiftDay(now); // If hour is 23 then actually return tomorrow...
    Shift currentShift = TimeUtil.calculateCrewChiefShift(now);
    Date currentStartDayAndHour = TimeUtil.getCrewChiefStartDayAndHour(day, currentShift);
    Date lastStartDayAndHour = TimeUtil.previousCrewChiefShiftStart(currentStartDayAndHour);
    Date nextStartDayAndHour = TimeUtil.nextCrewChiefShiftStart(currentStartDayAndHour);
    boolean current = currentStartDayAndHour.equals(startDayAndHour);
    boolean last = lastStartDayAndHour.equals(startDayAndHour);
    boolean next = nextStartDayAndHour.equals(startDayAndHour);

    /*SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyy HH:mm");
    System.out.println("startDayAndHour: " + formatter.format(startDayAndHour));
    System.out.println("computed current: " + formatter.format(currentStartDayAndHour));
    System.out.println("computed last: " + formatter.format(lastStartDayAndHour));
    System.out.println("computed next: " + formatter.format(nextStartDayAndHour));

    Date testDate = TimeUtil.getCrewChiefStartDayAndHour(day, Shift.OWL);
    System.out.println("Previous from " + formatter.format(testDate) + ": " + TimeUtil.previousCrewChiefShiftStart(testDate));
    System.out.println("Next from " + formatter.format(testDate) + ": " + TimeUtil.nextCrewChiefShiftStart(testDate));

    testDate = TimeUtil.getCrewChiefStartDayAndHour(day, Shift.DAY);
    System.out.println("Previous from " + formatter.format(testDate) + ": " + TimeUtil.previousCrewChiefShiftStart(testDate));
    System.out.println("Next from " + formatter.format(testDate) + ": " + TimeUtil.nextCrewChiefShiftStart(testDate));

    testDate = TimeUtil.getCrewChiefStartDayAndHour(day, Shift.SWING);
    System.out.println("Previous from " + formatter.format(testDate) + ": " + TimeUtil.previousCrewChiefShiftStart(testDate));
    System.out.println("Next from " + formatter.format(testDate) + ": " + TimeUtil.nextCrewChiefShiftStart(testDate)); */

    return current || last || next;
  }

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
   * @return a list of experimenter hall hours.
   * @throws TimeoutException if a network request takes too long.
   * @throws InterruptedException if a thread gets unexpectedly interrupted.
   * @throws CAException if a channel access problem occurs.
   */
  public CcShift find(Date startDayAndHour)
      throws TimeoutException, InterruptedException, CAException {
    CcShift shift = null;

    if (isCurrentLastOrNextShiftStart(startDayAndHour)) {
      shift = loadAccounting();
    }

    return shift;
  }

  /**
   * Fetches EPICS accounting information.
   *
   * <p>The accounting information returned from an EPICS IOC query is generally the past week of
   * data, up to, and including the current hour.
   *
   * @return the accounting information as a list of experimenter hall hours.
   * @throws TimeoutException if a network request takes too long.
   * @throws InterruptedException if a thread gets unexpectedly interrupted.
   * @throws CAException if a channel access problem occurs.
   */
  private CcShift loadAccounting() throws TimeoutException, InterruptedException, CAException {

    ShiftInfo accounting;

    long start = System.currentTimeMillis();
    accounting = getFromCache();
    long end = System.currentTimeMillis();
    logger.log(Level.FINEST, "EPICS shift load time (milliseconds): {0}", (end - start));

    return accounting.getOpShift();
  }

  private ShiftInfo getFromCache() {
    ShiftInfo accounting = new ShiftInfo();

    List<DBR> dbrs = new ArrayList<>();

    dbrs.add(cache.get(Constant.CREW_CHIEF_CHANNEL_NAME));
    dbrs.add(cache.get(Constant.OPERATORS_CHANNEL_NAME));
    dbrs.add(cache.get(Constant.PROGRAM_CHANNEL_NAME));
    dbrs.add(cache.get(Constant.PROGRAM_DEPUTY_CHANNEL_NAME));
    dbrs.add(cache.get(Constant.COMMENTS_CHANNEL_NAME));

    accounting.setCrewChief(SimpleGet.getStringValue(dbrs.remove(0))[0]);
    accounting.setOperators(SimpleGet.getStringValue(dbrs.remove(0))[0]);
    accounting.setProgram(SimpleGet.getStringValue(dbrs.remove(0))[0]);
    accounting.setProgramDeputy(SimpleGet.getStringValue(dbrs.remove(0))[0]);
    accounting.setComments(SimpleGet.getStringValue(dbrs.remove(0))[0]);

    return accounting;
  }
}
