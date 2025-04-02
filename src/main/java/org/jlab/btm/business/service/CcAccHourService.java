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
import org.hibernate.envers.RevisionType;
import org.jlab.btm.business.service.epics.CcEpicsAccHourService;
import org.jlab.btm.business.util.HourUtil;
import org.jlab.btm.persistence.entity.CcAccHour;
import org.jlab.btm.persistence.entity.PdShiftPlan;
import org.jlab.btm.persistence.projection.AcceleratorShiftAvailability;
import org.jlab.btm.persistence.projection.CcAccShiftTotals;
import org.jlab.btm.persistence.projection.CcAccSum;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.DateIterator;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.util.JPAUtil;
import org.jlab.smoothness.presentation.filter.AuditContext;

/**
 * @author ryans
 */
@Stateless
public class CcAccHourService extends AbstractService<CcAccHour> {

  private static final Logger logger = Logger.getLogger(CcAccHourService.class.getName());

  @EJB CcEpicsAccHourService epicsService;

  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  public CcAccHourService() {
    super(CcAccHour.class);
  }

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  @SuppressWarnings("unchecked")
  @PermitAll
  public List<CcAccHour> findInDatabase(Date start, Date end) {

    // The following don't work with daylight savings:
    // Use Date
    // Use Calendar
    // Instead, use string and specify date format.
    // Example test case: if start and end are both daylight savings date such as 1 AM Nov. 4 2018
    // EST

    // Query query = em.createNativeQuery("select * from CC_ACC_HOUR a where a.day_and_hour between
    // :start and :end", OpAccHour.class);

    /*TypedQuery<OpAccHour> query = em.createQuery(
    "select a from OpAccHour a where a.dayAndHour between :start and :end",
    OpAccHour.class);*/

    /*query.setParameter("start", start);
    query.setParameter("end", end);*/

    /*TimeZone timeZone = TimeZone.getTimeZone("America/New_York");
    String startTz = timeZone.inDaylightTime(start) ? "EDT" : "EST";
    String endTz = timeZone.inDaylightTime(end) ? "EDT" : "EST";

    //Calendar startCal = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"), Locale.US);
    Calendar startCal = Calendar.getInstance(TimeZone.getTimeZone(startTz), Locale.US);
    startCal.setTime(start);

    //Calendar endCal = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"), Locale.US);
    Calendar endCal = Calendar.getInstance(TimeZone.getTimeZone(endTz), Locale.US);
    endCal.setTime(end);*/

    Query query =
        em.createNativeQuery(
            "select * from CC_ACC_HOUR a where a.day_and_hour between to_timestamp_tz(:start, 'YYYY-MM-DD HH24 TZD') and to_timestamp_tz(:end, 'YYYY-MM-DD HH24 TZD')",
            CcAccHour.class);

    String startStr = TimeUtil.formatDatabaseDateTimeTZ(start);
    String endStr = TimeUtil.formatDatabaseDateTimeTZ(end);

    query.setParameter("start", startStr);
    query.setParameter("end", endStr);

    return query.getResultList();
  }

  @PermitAll
  public List<CcAccHour> findInEpics(Date start, Date end) throws UserFriendlyException {
    try {
      return epicsService.find(start, end);
    } catch (TimeoutException | InterruptedException | CAException e) {
      throw new UserFriendlyException("Unable to query EPICS", e);
    }
  }

  @PermitAll
  public CcAccSum findSummary(Date start, Date end) {
    Query q =
        em.createNativeQuery(
            "select :start0, :end0, sum(up_seconds), sum(sad_seconds), sum(down_seconds), sum(studies_seconds), sum(restore_seconds), sum(acc_seconds) "
                + "from ("
                + "select up_seconds, sad_seconds, down_seconds, studies_seconds, restore_seconds, acc_seconds from CC_acc_hour "
                + "where day_and_hour >= :start1 and day_and_hour < :end1 "
                + "union all select 0, 0, 0, 0, 0, 0 from dual)");

    q.setParameter("start0", start);
    q.setParameter("end0", end);
    q.setParameter("start1", start);
    q.setParameter("end1", end);

    List<CcAccSum> totalsList = JPAUtil.getResultList(q, CcAccSum.class);

    CcAccSum totals = null;

    if (totalsList != null && !totalsList.isEmpty()) {
      totals = totalsList.get(0);
    }

    return totals;
  }

  @PermitAll
  public CcAccShiftTotals calculateTotals(List<CcAccHour> accHourList) {
    CcAccShiftTotals totals = new CcAccShiftTotals();

    if (accHourList != null) {
      for (CcAccHour hour : accHourList) {
        totals.setUpSeconds(
            totals.getUpSeconds() == null
                ? hour.getUpSeconds()
                : totals.getUpSeconds() + hour.getUpSeconds());
        totals.setSadSeconds(
            totals.getSadSeconds() == null
                ? hour.getSadSeconds()
                : totals.getSadSeconds() + hour.getSadSeconds());
        totals.setDownSeconds(
            totals.getDownSeconds() == null
                ? hour.getDownSeconds()
                : totals.getDownSeconds() + hour.getDownSeconds());
        totals.setStudiesSeconds(
            totals.getStudiesSeconds() == null
                ? hour.getStudiesSeconds()
                : totals.getStudiesSeconds() + hour.getStudiesSeconds());
        totals.setRestoreSeconds(
            totals.getRestoreSeconds() == null
                ? hour.getRestoreSeconds()
                : totals.getRestoreSeconds() + hour.getRestoreSeconds());
        totals.setAccSeconds(
            totals.getAccSeconds() == null
                ? hour.getAccSeconds()
                : totals.getAccSeconds() + hour.getAccSeconds());
      }
    }

    return totals;
  }

  @RolesAllowed({"cc", "btm-admin"})
  public void editAccHours(
      Date[] hourArray,
      Short[] upArray,
      Short[] sadArray,
      Short[] downArray,
      Short[] studiesArray,
      Short[] restoreArray,
      Short[] accArray)
      throws UserFriendlyException {
    if (hourArray == null
        || upArray == null
        || sadArray == null
        || downArray == null
        || studiesArray == null
        || restoreArray == null
        || accArray == null) {
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
        || hourArray.length != sadArray.length
        || hourArray.length != downArray.length
        || hourArray.length != studiesArray.length
        || hourArray.length != restoreArray.length
        || hourArray.length != accArray.length) {
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

    List<CcAccHour> hourList = findInDatabase(hourArray[0], hourArray[hourArray.length - 1]);
    Map<Date, CcAccHour> hourMap = HourUtil.createHourMap(hourList);

    // We probably could consolidate with previous loop... (performance vs maintainability)
    for (int i = 0; i < hourArray.length; i++) {
      Date hour = hourArray[i];
      SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy HH:mm z");
      logger.log(Level.FINEST, "Editing hour: {0}", dateFormat.format(hour));
      CcAccHour accHour = hourMap.get(hour);

      int total =
          upArray[i] + sadArray[i] + downArray[i] + studiesArray[i] + restoreArray[i] + accArray[i];

      if (total != 3600) {
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
        throw new UserFriendlyException(
            "Hour " + hourFormat.format(hourArray[i]) + " availability must total 1 hour");
      }

      if (accHour == null) {
        accHour = new CcAccHour();
        accHour.setDayAndHour(hour);
      }

      accHour.setUpSeconds(upArray[i]);
      accHour.setSadSeconds(sadArray[i]);
      accHour.setDownSeconds(downArray[i]);
      accHour.setStudiesSeconds(studiesArray[i]);
      accHour.setRestoreSeconds(restoreArray[i]);
      accHour.setAccSeconds(accArray[i]);

      edit(accHour);
    }
  }

  @Override
  protected CcAccHour edit(CcAccHour hour) {
    if (hour.getCcAccHourId() == null) {
      /* We can't use JPA to do insert because JPA breaks startDayAndHour field on ambiguous wall clock hour
       * during daylight savings; JPA can't include EDT/EST qualifier in String literal SQL statement it creates
       * And we must support unique dates such as:
       * 2018-11-04 01 EDT
       * 2018-11-04 01 EST
       */
      hour = this.manualInsert(hour);
      manualAudit(hour, RevisionType.ADD);
    } else {
      /*
       * We can use JPA to modify records since
       * startDayAndHour @Column is marked as insertable = false and updatable = false as it's an alternate key
       * and can't be changed after the manual insert above.  However, the JPA Date limitation (and
       * @Column insertable = false above) breaks Envers Auditing, so we can't use @Audited to
       * automatically insert audit records and must do that manually.
       */
      hour = super.edit(hour);
      this.manualAudit(hour, RevisionType.MOD);
    }

    return hour;
  }

  @PermitAll
  public void manualAudit(CcAccHour hour, RevisionType type) {
    logger.log(Level.FINEST, "CcAccHourService.manualAudit");

    Query idq = em.createNativeQuery("select hibernate_sequence.nextval from dual");

    BigDecimal idDec = (BigDecimal) idq.getSingleResult();

    BigInteger id = idDec.toBigInteger();

    logger.log(Level.FINEST, "CcAccHourService.manualAudit; Got ID: {}", id);

    long timestamp = System.currentTimeMillis();

    Query revq =
        em.createNativeQuery(
            "insert into revision_info (ADDRESS, REVTSTMP, USERNAME, REV) values (:address, :revtstmp, :username, :rev)");

    AuditContext context = AuditContext.getCurrentInstance();

    String address = context.getIp();
    String username = context.getUsername();

    revq.setParameter("address", address);
    revq.setParameter("revtstmp", timestamp);
    revq.setParameter("username", username);
    revq.setParameter("rev", id);

    int count = revq.executeUpdate();

    if (count == 0) {
      logger.log(Level.WARNING, "manualAudit revision_info insert count is zero");
    }

    Query audq =
        em.createNativeQuery(
            "insert into cc_acc_hour_aud (REVTYPE, DAY_AND_HOUR, UP_SECONDS, SAD_SECONDS, DOWN_SECONDS, STUDIES_SECONDS, RESTORE_SECONDS, ACC_SECONDS, CC_ACC_HOUR_ID, REV) values (:revtype, to_timestamp_tz(:dayAndHour, 'YYYY-MM-DD HH24 TZD'), :up, :sad, :down, :studies, :restore, :acc, :hour_id, :rev)");

    String dayAndHourStr = TimeUtil.formatDatabaseDateTimeTZ(hour.getDayAndHour());

    logger.log(
        Level.FINEST,
        "manualAudit.dayAndHourStr: {}, dayAndHour: {}",
        new Object[] {dayAndHourStr, hour.getDayAndHour()});

    audq.setParameter("revtype", type.getRepresentation());
    audq.setParameter("dayAndHour", dayAndHourStr);
    audq.setParameter("up", hour.getUpSeconds());
    audq.setParameter("sad", hour.getSadSeconds());
    audq.setParameter("down", hour.getDownSeconds());
    audq.setParameter("studies", hour.getStudiesSeconds());
    audq.setParameter("restore", hour.getRestoreSeconds());
    audq.setParameter("acc", hour.getAccSeconds());
    audq.setParameter("hour_id", hour.getCcAccHourId());
    audq.setParameter("rev", id);

    count = audq.executeUpdate();

    if (count == 0) {
      logger.log(Level.WARNING, "manualAudit cc_acc_hour_aud insert count is zero");
    }
  }

  private CcAccHour manualInsert(CcAccHour hour) {

    String dayAndHourStr = TimeUtil.formatDatabaseDateTimeTZ(hour.getDayAndHour());

    Query idq = em.createNativeQuery("select CC_acc_hour_id.nextval from dual");

    BigDecimal idDec = (BigDecimal) idq.getSingleResult();

    BigInteger id = idDec.toBigInteger();

    Query q = em.createNamedQuery("CcAccHour.insertNATIVE");

    q.setParameter("id", id);
    q.setParameter("dayAndHour", dayAndHourStr);
    q.setParameter("up", hour.getUpSeconds());
    q.setParameter("sad", hour.getSadSeconds());
    q.setParameter("down", hour.getDownSeconds());
    q.setParameter("studies", hour.getStudiesSeconds());
    q.setParameter("restore", hour.getRestoreSeconds());
    q.setParameter("acc", hour.getAccSeconds());

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
  public AcceleratorShiftAvailability getAcceleratorAvailability(
      Date startHour, Date endHour, boolean queryEpics, PdShiftPlan plan) {
    List<CcAccHour> dbAccHourList = findInDatabase(startHour, endHour);
    List<CcAccHour> epicsAccHourList;
    if (queryEpics) {
      try {
        epicsAccHourList = findInEpics(startHour, endHour);
      } catch (UserFriendlyException e) {
        logger.log(Level.FINEST, "Unable to obtain EPICS acc hour data", e);
        epicsAccHourList = new ArrayList<>();
      }
    } else {
      epicsAccHourList = new ArrayList<>();
    }
    Map<Date, CcAccHour> dbAccHourMap = HourUtil.createHourMap(dbAccHourList);
    Map<Date, CcAccHour> epicsAccHourMap = HourUtil.createHourMap(epicsAccHourList);
    List<CcAccHour> accHourList =
        HourUtil.fillMissingHoursAndSetSource(
            dbAccHourMap, epicsAccHourMap, startHour, endHour, CcAccHour.class);

    CcAccShiftTotals accTotals = calculateTotals(accHourList);
    CcAccShiftTotals epicsAccTotals = calculateTotals(epicsAccHourList);
    CcAccShiftTotals pdShiftTotals = new CcAccShiftTotals();

    if (plan != null) {
      pdShiftTotals.setUpSeconds(plan.getPhysicsSeconds());
      pdShiftTotals.setStudiesSeconds(plan.getStudiesSeconds());
      pdShiftTotals.setRestoreSeconds(plan.getRestoreSeconds());
      pdShiftTotals.setAccSeconds(plan.getAccSeconds());
      pdShiftTotals.setDownSeconds(plan.getDownSeconds());
      pdShiftTotals.setSadSeconds(plan.getSadSeconds());
    }

    AcceleratorShiftAvailability availability = new AcceleratorShiftAvailability();

    availability.setHourList(accHourList);
    availability.setEpicsHourList(epicsAccHourList);
    availability.setShiftTotals(accTotals);
    availability.setEpicsShiftTotals(epicsAccTotals);
    availability.setPdShiftTotals(pdShiftTotals);
    availability.setDbHourList(dbAccHourList);

    return availability;
  }

  @PermitAll
  public List<DayTotals> dayTotals(Date start, Date end) {
    List<DayTotals> dayTotals = new ArrayList<>();

    Date startDayHourZero = TimeUtil.startOfDay(start, Calendar.getInstance());
    Date endDayHourZero = TimeUtil.startOfDay(end, Calendar.getInstance());

    // We have use an open end of interval so don't include final month if day one
    if (endDayHourZero.equals(end)) {
      endDayHourZero = TimeUtil.addDays(end, -1);
    }

    DateIterator iterator = new DateIterator(startDayHourZero, endDayHourZero, Calendar.DATE);

    while (iterator.hasNext()) {
      Date day = iterator.next();

      Date startOfDay = TimeUtil.startOfDay(day, Calendar.getInstance());
      Date startOfNextDay = TimeUtil.startOfNextDay(day, Calendar.getInstance());

      // System.out.println("Start of Month: " + formatter.format(startOfMonth));
      // System.out.println("End (start of next Month): " + formatter.format(startOfNextMonth));
      Date realStart = (startDayHourZero.getTime() == day.getTime()) ? start : startOfDay;
      Date realEnd = iterator.hasNext() ? startOfNextDay : end;

      CcAccSum totals = this.findSummary(realStart, realEnd);
      DayTotals mt = new DayTotals();
      mt.day = startOfDay;
      mt.totals = totals;
      dayTotals.add(mt);
    }

    return dayTotals;
  }

  @PermitAll
  public List<MonthTotals> monthTotals(Date start, Date end) {
    List<MonthTotals> monthTotals = new ArrayList<>();

    Date startMonthDayOne = TimeUtil.startOfMonth(start, Calendar.getInstance());
    Date endMonthDayOne = TimeUtil.startOfMonth(end, Calendar.getInstance());

    // We have use an open end of interval so don't include final month if day one
    if (endMonthDayOne.equals(end)) {
      endMonthDayOne = TimeUtil.addMonths(end, -1);
    }

    DateIterator iterator = new DateIterator(startMonthDayOne, endMonthDayOne, Calendar.MONTH);

    while (iterator.hasNext()) {
      Date month = iterator.next();

      Date startOfMonth = TimeUtil.startOfMonth(month, Calendar.getInstance());
      Date startOfNextMonth = TimeUtil.startOfNextMonth(month, Calendar.getInstance());

      // System.out.println("Start of Month: " + formatter.format(startOfMonth));
      // System.out.println("End (start of next Month): " + formatter.format(startOfNextMonth));
      Date realStart = (startMonthDayOne.getTime() == month.getTime()) ? start : startOfMonth;
      Date realEnd = iterator.hasNext() ? startOfNextMonth : end;

      CcAccSum totals = this.findSummary(realStart, realEnd);
      MonthTotals mt = new MonthTotals();
      mt.month = startOfMonth;
      mt.totals = totals;
      monthTotals.add(mt);
    }

    return monthTotals;
  }

  public class DayTotals {

    Date day;
    CcAccSum totals;

    public Date getDay() {
      return day;
    }

    public CcAccSum getTotals() {
      return totals;
    }
  }

  public class MonthTotals {

    Date month;
    CcAccSum totals;

    public Date getMonth() {
      return month;
    }

    public CcAccSum getTotals() {
      return totals;
    }
  }
}
