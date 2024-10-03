package org.jlab.btm.business.service;

import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.jlab.btm.business.service.epics.CcEpicsHallHourService;
import org.jlab.btm.business.util.HourUtil;
import org.jlab.btm.persistence.entity.CcHallHour;
import org.jlab.btm.persistence.entity.PdShiftPlan;
import org.jlab.btm.persistence.enumeration.DataSource;
import org.jlab.btm.persistence.projection.CcHallShiftAvailability;
import org.jlab.btm.persistence.projection.CcHallShiftTotals;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.DateIterator;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * @author ryans
 */
@Stateless
public class CcHallHourService extends AbstractService<CcHallHour> {

  private static final Logger logger = Logger.getLogger(CcHallHourService.class.getName());
  @EJB CcEpicsHallHourService epicsService;

  @PersistenceContext(unitName = "btmPU")
  private EntityManager em;

  public CcHallHourService() {
    super(CcHallHour.class);
  }

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  @SuppressWarnings("unchecked")
  @PermitAll
  public List<CcHallHour> findInDatabase(Hall hall, Date start, Date end) {
    // The following don't work with daylight savings:
    // Use Date
    // Use Calendar
    // Instead, use string and specify date format.
    // Example test case: if start and end are both daylight savings date such as 1 AM Nov. 4 2018
    // EST

    /*TypedQuery<OpHallHour> query = em.createQuery(
            "select a from OpHallHour a where a.hall = :hall and a.dayAndHour between :start and :end",
            OpHallHour.class);

    query.setParameter("hall", hall);
    query.setParameter("start", start);
    query.setParameter("end", end);*/
    Query query =
        em.createNativeQuery(
            "select * from CC_HALL_HOUR a where a.hall = :hall and a.day_and_hour between to_timestamp_tz(:start, 'YYYY-MM-DD HH24 TZD') and to_timestamp_tz(:end, 'YYYY-MM-DD HH24 TZD')",
            CcHallHour.class);

    String startStr = TimeUtil.formatDatabaseDateTimeTZ(start);
    String endStr = TimeUtil.formatDatabaseDateTimeTZ(end);

    query.setParameter("hall", hall.getLetter());
    query.setParameter("start", startStr);
    query.setParameter("end", endStr);

    return query.getResultList();
  }

  @PermitAll
  public List<CcHallHour> findInEpics(Hall hall, Date start, Date end)
      throws UserFriendlyException {
    try {
      return epicsService.find(hall, start, end);
    } catch (TimeoutException | InterruptedException | CAException e) {
      throw new UserFriendlyException("Unable to query EPICS", e);
    }
  }

  @PermitAll
  public List<CcHallHour> fillMissingHoursAndSetSource(
      Map<Date, CcHallHour> dbHourMap,
      Map<Date, CcHallHour> epicsHourMap,
      Hall hall,
      Date start,
      Date end) {
    List<CcHallHour> filledList = new ArrayList<>();

    DateIterator iterator = new DateIterator(start, end, Calendar.HOUR_OF_DAY);

    CcHallHour hallHour;

    for (Date hour : iterator) {
      if (dbHourMap.containsKey(hour)) {
        hallHour = dbHourMap.get(hour);
        hallHour.setSource(DataSource.DATABASE);
      } else if (epicsHourMap.containsKey(hour)) {
        hallHour = epicsHourMap.get(hour);
        hallHour.setSource(DataSource.EPICS);
      } else {
        hallHour = new CcHallHour();
        hallHour.setDayAndHour(hour);
        hallHour.setHall(hall);
        hallHour.setSource(DataSource.NONE);
      }

      filledList.add(hallHour);
    }

    return filledList;
  }

  @PermitAll
  public CcHallShiftTotals calculateTotals(List<CcHallHour> hourList) {
    CcHallShiftTotals totals = new CcHallShiftTotals();

    if (hourList != null) {
      for (CcHallHour hour : hourList) {
        totals.setUpSeconds(
            totals.getUpSeconds() == null
                ? hour.getUpSeconds()
                : totals.getUpSeconds() + hour.getUpSeconds());
        totals.setTuneSeconds(
            totals.getTuneSeconds() == null
                ? hour.getTuneSeconds()
                : totals.getTuneSeconds() + hour.getTuneSeconds());
        totals.setBnrSeconds(
            totals.getBnrSeconds() == null
                ? hour.getBnrSeconds()
                : totals.getBnrSeconds() + hour.getBnrSeconds());
        totals.setDownSeconds(
            totals.getDownSeconds() == null
                ? hour.getDownSeconds()
                : totals.getDownSeconds() + hour.getDownSeconds());
        totals.setOffSeconds(
            totals.getOffSeconds() == null
                ? hour.getOffSeconds()
                : totals.getOffSeconds() + hour.getOffSeconds());
      }
    }

    return totals;
  }

  @RolesAllowed({"cc", "btm-admin"})
  public void editHallHours(
      Hall hall,
      Date[] hourArray,
      Short[] upArray,
      Short[] tuneArray,
      Short[] bnrArray,
      Short[] downArray,
      Short[] offArray)
      throws UserFriendlyException {
    if (hourArray == null
        || upArray == null
        || tuneArray == null
        || bnrArray == null
        || downArray == null
        || offArray == null) {
      throw new UserFriendlyException("Some columns of data are missing");
    }

    if (hourArray.length == 0) {
      throw new UserFriendlyException("No data");
    }

    if (hourArray.length > 9) {
      throw new UserFriendlyException("Only a single shift of data can be edited at a time");
    }

    if (hourArray.length != upArray.length
        || hourArray.length != upArray.length
        || hourArray.length != tuneArray.length
        || hourArray.length != bnrArray.length
        || hourArray.length != downArray.length
        || hourArray.length != offArray.length) {
      throw new UserFriendlyException("Column data does not line up in equal number of rows");
    }

    Date previous = null;
    for (Date hour : hourArray) {
      if (previous == null) {
        previous = hour;
      } else {
        if (hour == null) {
          throw new UserFriendlyException("Day and hour cannot be empty");
        }
        if (!TimeUtil.addHours(hour, -1).equals(previous)) {
          throw new UserFriendlyException("Hours must be contiguous");
        }
        previous = hour;
      }
    }

    List<CcHallHour> hourList = findInDatabase(hall, hourArray[0], hourArray[hourArray.length - 1]);
    Map<Date, CcHallHour> hourMap = HourUtil.createHourMap(hourList);

    // We probably could consolidate with previous loop... (performance vs maintainability)
    for (int i = 0; i < hourArray.length; i++) {
      Date hour = hourArray[i];
      SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy HH:mm z");
      logger.log(Level.FINEST, "Editing hall hour: {0}", dateFormat.format(hour));
      CcHallHour hallHour = hourMap.get(hour);

      int total = upArray[i] + tuneArray[i] + bnrArray[i] + downArray[i] + offArray[i];

      if (total != 3600) {
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
        throw new UserFriendlyException(
            "Hour " + hourFormat.format(hourArray[i]) + " availability must total 1 hour");
      }

      if (hallHour == null) {
        hallHour = new CcHallHour();
        hallHour.setDayAndHour(hour);
        hallHour.setHall(hall);
      }

      hallHour.setUpSeconds(upArray[i]);
      hallHour.setTuneSeconds(tuneArray[i]);
      hallHour.setBnrSeconds(bnrArray[i]);
      hallHour.setDownSeconds(downArray[i]);
      hallHour.setOffSeconds(offArray[i]);

      edit(hallHour);
    }
  }

  @Override
  protected CcHallHour edit(CcHallHour hour) {
    if (hour.getOpHallHourId() == null) {
      hour = this.manualInsert(hour);
    } else {
      hour = super.edit(hour);
    }

    return hour;
  }

  private CcHallHour manualInsert(CcHallHour hour) {

    String dayAndHourStr = TimeUtil.formatDatabaseDateTimeTZ(hour.getDayAndHour());

    Query idq = em.createNativeQuery("select CC_hall_hour_id.nextval from dual");

    BigDecimal idDec = (BigDecimal) idq.getSingleResult();

    BigInteger id = idDec.toBigInteger();

    Query q = em.createNamedQuery("OpHallHour.insertNATIVE");

    q.setParameter("id", id);
    q.setParameter("hall", hour.getHall().getLetter());
    q.setParameter("dayAndHour", dayAndHourStr);
    q.setParameter("up", hour.getUpSeconds());
    q.setParameter("tune", hour.getTuneSeconds());
    q.setParameter("bnr", hour.getBnrSeconds());
    q.setParameter("down", hour.getDownSeconds());
    q.setParameter("off", hour.getOffSeconds());

    int count = q.executeUpdate();

    if (count == 0) {
      logger.log(Level.WARNING, "Insert count is zero");
    }

    hour = this.find(id);

    if (hour == null) {
      logger.log(Level.WARNING, "Unable to find hour after insert");
    }

    return hour;
  }

  @PermitAll
  public List<CcHallShiftAvailability> getHallAvailablilityList(
      Date startHour, Date endHour, boolean queryEpics, PdShiftPlan plan) {
    CcHallShiftTotals hallAPdTotals = new CcHallShiftTotals();
    CcHallShiftTotals hallBPdTotals = new CcHallShiftTotals();
    CcHallShiftTotals hallCPdTotals = new CcHallShiftTotals();
    CcHallShiftTotals hallDPdTotals = new CcHallShiftTotals();

    if (plan != null) {
      hallAPdTotals.setUpSeconds(plan.getHallAUpSeconds());
      hallAPdTotals.setTuneSeconds(plan.getHallATuneSeconds());
      hallAPdTotals.setBnrSeconds(plan.getHallABnrSeconds());
      hallAPdTotals.setDownSeconds(plan.getHallADownSeconds());
      hallAPdTotals.setOffSeconds(plan.getHallAOffSeconds());

      hallBPdTotals.setUpSeconds(plan.getHallBUpSeconds());
      hallBPdTotals.setTuneSeconds(plan.getHallBTuneSeconds());
      hallBPdTotals.setBnrSeconds(plan.getHallBBnrSeconds());
      hallBPdTotals.setDownSeconds(plan.getHallBDownSeconds());
      hallBPdTotals.setOffSeconds(plan.getHallBOffSeconds());

      hallCPdTotals.setUpSeconds(plan.getHallCUpSeconds());
      hallCPdTotals.setTuneSeconds(plan.getHallCTuneSeconds());
      hallCPdTotals.setBnrSeconds(plan.getHallCBnrSeconds());
      hallCPdTotals.setDownSeconds(plan.getHallCDownSeconds());
      hallCPdTotals.setOffSeconds(plan.getHallCOffSeconds());

      hallDPdTotals.setUpSeconds(plan.getHallDUpSeconds());
      hallDPdTotals.setTuneSeconds(plan.getHallDTuneSeconds());
      hallDPdTotals.setBnrSeconds(plan.getHallDBnrSeconds());
      hallDPdTotals.setDownSeconds(plan.getHallDDownSeconds());
      hallDPdTotals.setOffSeconds(plan.getHallDOffSeconds());
    }

    List<CcHallShiftAvailability> hallAvailabilityList = new ArrayList<>();
    hallAvailabilityList.add(
        getHallAvailability(Hall.A, startHour, endHour, queryEpics, hallAPdTotals));
    hallAvailabilityList.add(
        getHallAvailability(Hall.B, startHour, endHour, queryEpics, hallBPdTotals));
    hallAvailabilityList.add(
        getHallAvailability(Hall.C, startHour, endHour, queryEpics, hallCPdTotals));
    hallAvailabilityList.add(
        getHallAvailability(Hall.D, startHour, endHour, queryEpics, hallDPdTotals));

    return hallAvailabilityList;
  }

  private CcHallShiftAvailability getHallAvailability(
      Hall hall,
      Date startHour,
      Date endHour,
      boolean queryEpics,
      CcHallShiftTotals pdShiftTotals) {
    List<CcHallHour> dbHourList = findInDatabase(hall, startHour, endHour);
    List<CcHallHour> epicsHourList;

    if (queryEpics) {
      try {
        epicsHourList = findInEpics(hall, startHour, endHour);
      } catch (UserFriendlyException e) {
        logger.log(Level.FINEST, "Unable to obtain EPICS hall hour data", e);
        epicsHourList = new ArrayList<>();
      }
    } else {
      epicsHourList = new ArrayList<>();
    }

    Map<Date, CcHallHour> dbHourMap = HourUtil.createHourMap(dbHourList);
    Map<Date, CcHallHour> epicsHourMap = HourUtil.createHourMap(epicsHourList);
    List<CcHallHour> hourList =
        fillMissingHoursAndSetSource(dbHourMap, epicsHourMap, hall, startHour, endHour);

    CcHallShiftTotals totals = calculateTotals(hourList);
    CcHallShiftTotals epicsTotals = calculateTotals(epicsHourList);

    CcHallShiftAvailability availability = new CcHallShiftAvailability();
    availability.setHall(hall);
    availability.setHourList(hourList);
    availability.setEpicsHourList(epicsHourList);
    availability.setShiftTotals(totals);
    availability.setEpicsShiftTotals(epicsTotals);
    availability.setPdShiftTotals(pdShiftTotals);
    availability.setDbHourList(dbHourList);

    return availability;
  }
}
