package org.jlab.btm.business.service;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import org.jlab.btm.persistence.entity.CcAccHour;
import org.jlab.btm.persistence.projection.*;
import org.jlab.smoothness.business.util.DateIterator;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.util.JPAUtil;

/**
 * @author ryans
 */
@Stateless
public class DowntimeService extends AbstractService<CcAccHour> {

  private static final Logger LOGGER = Logger.getLogger(DowntimeService.class.getName());

  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  public DowntimeService() {
    super(CcAccHour.class);
  }

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  @PermitAll
  public DowntimeSummaryTotals reportTotals(Date start, Date end, BigInteger eventTypeId) {
    Query q =
        em.createNativeQuery(
            "select sum(downtime_seconds) as downtime_seconds "
                + "from (select "
                + "btm_owner.interval_to_seconds(least(nvl(a.time_up, sysdate), :end) - greatest(a.time_down, :start)) as downtime_seconds "
                + "from btm_owner.event_first_incident a "
                + "where a.event_type_id = :typeId "
                + "and a.time_down < :end "
                + "and nvl(a.time_up, sysdate) >= :start "
                + "union all (select 0 from dual))");

    q.setParameter("typeId", eventTypeId);
    q.setParameter("start", start);
    q.setParameter("end", end);

    List<DowntimeSummaryTotals> totalsList = JPAUtil.getResultList(q, DowntimeSummaryTotals.class);

    DowntimeSummaryTotals totals = null;

    if (totalsList != null && !totalsList.isEmpty()) {
      totals = totalsList.get(0);
    }

    return totals;
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

      DowntimeSummaryTotals totals = this.reportTotals(realStart, realEnd, BigInteger.ONE);
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

      DowntimeSummaryTotals totals = this.reportTotals(realStart, realEnd, BigInteger.ONE);
      MonthTotals mt = new MonthTotals();
      mt.month = startOfMonth;
      mt.totals = totals;
      monthTotals.add(mt);
    }

    return monthTotals;
  }

  @PermitAll
  public List<DowntimeHourCrossCheck> getCrossCheckHourList(
      List<CcAccHour> ccAccHourList, List<DtmHour> dtmHourList) {
    List<DowntimeHourCrossCheck> checkList = new ArrayList<>();

    for (int i = 0; i < ccAccHourList.size(); i++) {
      CcAccHour ccAccHour = ccAccHourList.get(i);
      DtmHour dtmHour = dtmHourList.get(i);

      DowntimeHourCrossCheck checkHour =
          new DowntimeHourCrossCheck(ccAccHour.getDayAndHour(), ccAccHour, dtmHour);
      checkList.add(checkHour);
    }

    return checkList;
  }

  @PermitAll
  public List<DtmHour> getDtmHourList(Date startHour, Date endHour) {
    List<DtmHour> dtmHourList = new ArrayList<>();
    DateIterator iterator = new DateIterator(startHour, endHour, Calendar.HOUR_OF_DAY);

    while (iterator.hasNext()) {
      Date hour = iterator.next();

      Date startOfNextHour = TimeUtil.addHours(hour, Calendar.HOUR_OF_DAY);

      short blockedSeconds = 0;
      short tuningSeconds = 0;

      DowntimeSummaryTotals blockedTotals =
          this.reportTotals(hour, startOfNextHour, BigInteger.ONE);

      DowntimeSummaryTotals tuningTotals =
          this.reportTotals(hour, startOfNextHour, BigInteger.valueOf(9L));

      blockedSeconds = (short) blockedTotals.getEventSeconds();
      tuningSeconds = (short) tuningTotals.getEventSeconds();

      DtmHour dtmHour = new DtmHour(hour, blockedSeconds, tuningSeconds);

      dtmHourList.add(dtmHour);
    }

    return dtmHourList;
  }

  public class DayTotals {

    Date day;
    DowntimeSummaryTotals totals;

    public Date getDay() {
      return day;
    }

    public DowntimeSummaryTotals getTotals() {
      return totals;
    }
  }

  public class MonthTotals {

    Date month;
    DowntimeSummaryTotals totals;

    public Date getMonth() {
      return month;
    }

    public DowntimeSummaryTotals getTotals() {
      return totals;
    }
  }
}
