package org.jlab.btm.business.service.epics;

import com.cosylab.epics.caj.CAJContext;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.jlab.btm.business.util.HourUtil;
import org.jlab.btm.persistence.entity.CcHallHour;
import org.jlab.btm.persistence.epics.HallBeamAvailability;
import org.jlab.btm.persistence.epics.HallBeamAvailabilityDao;
import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * Responsible for querying experimenter hall EPICS time accounting data.
 *
 * @author ryans
 */
@Stateless
public class CcEpicsHallHourService {

  private static final Logger logger = Logger.getLogger(CcEpicsHallHourService.class.getName());
  @EJB ContextFactory factory;

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
   * @throws TimeoutException if a network request takes too long.
   * @throws InterruptedException if a thread gets unexpectedly interrupted.
   * @throws CAException if a channel access problem occurs.
   */
  public List<CcHallHour> find(Hall hall, Date startDayAndHour, Date endDayAndHour)
      throws TimeoutException, InterruptedException, CAException {
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
   * @throws TimeoutException if a network request takes too long.
   * @throws InterruptedException if a thread gets unexpectedly interrupted.
   * @throws CAException if a channel access problem occurs.
   */
  private List<CcHallHour> loadAccounting(Hall hall)
      throws TimeoutException, InterruptedException, CAException {

    HallBeamAvailability accounting;

    CAJContext context = factory.getContext();

    try {
      HallBeamAvailabilityDao dao = new HallBeamAvailabilityDao(context);

      long start = System.currentTimeMillis();
      accounting = dao.loadAccounting(hall);
      long end = System.currentTimeMillis();
      logger.log(Level.FINEST, "EPICS hall hours load time (milliseconds): {0}", (end - start));
    } finally {
      factory.returnContext(context);
    }

    return accounting.getOpHallHours();
  }
}
