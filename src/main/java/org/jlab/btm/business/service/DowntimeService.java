package org.jlab.btm.business.service;

import org.jlab.btm.persistence.entity.OpAccHour;
import org.jlab.btm.persistence.projection.DowntimeSummaryTotals;
import org.jlab.smoothness.business.util.DateIterator;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.util.JPAUtil;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author ryans
 */
@Stateless
public class DowntimeService extends AbstractService<OpAccHour> {

    private final static Logger LOGGER = Logger.getLogger(DowntimeService.class.getName());

    @PersistenceContext(unitName = "jbtaPU")
    private EntityManager em;

    public DowntimeService() {
        super(OpAccHour.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @PermitAll
    public DowntimeSummaryTotals reportTotals(Date start, Date end) {
        Query q = em.createNativeQuery(
                "select sum(downtime_seconds) as downtime_seconds " + "from (select "
                        + "dtm_owner.interval_to_seconds(least(nvl(a.time_up, sysdate), :end) - greatest(a.time_down, :start)) as downtime_seconds "
                        + "from dtm_owner.event_first_incident a " + "where a.event_type_id = 1 "
                        + "and a.time_down < :end " + "and nvl(a.time_up, sysdate) >= :start "
                        + "union all (select 0 from dual))"
        );

        q.setParameter("start", start);
        q.setParameter("end", end);

        List<DowntimeSummaryTotals> totalsList = JPAUtil.getResultList(q,
                DowntimeSummaryTotals.class);

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

            //System.out.println("Start of Month: " + formatter.format(startOfMonth));
            //System.out.println("End (start of next Month): " + formatter.format(startOfNextMonth));
            Date realStart = (startDayHourZero.getTime() == day.getTime()) ? start : startOfDay;
            Date realEnd = iterator.hasNext() ? startOfNextDay : end;

            DowntimeSummaryTotals totals = this.reportTotals(realStart, realEnd);
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

            //System.out.println("Start of Month: " + formatter.format(startOfMonth));
            //System.out.println("End (start of next Month): " + formatter.format(startOfNextMonth));
            Date realStart = (startMonthDayOne.getTime() == month.getTime()) ? start : startOfMonth;
            Date realEnd = iterator.hasNext() ? startOfNextMonth : end;

            DowntimeSummaryTotals totals = this.reportTotals(realStart, realEnd);
            MonthTotals mt = new MonthTotals();
            mt.month = startOfMonth;
            mt.totals = totals;
            monthTotals.add(mt);
        }

        return monthTotals;
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
