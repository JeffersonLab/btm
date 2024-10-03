package org.jlab.btm.business.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.jlab.btm.business.util.BtmTimeUtil;
import org.jlab.btm.business.util.DateRange;
import org.jlab.btm.persistence.projection.CcAccSum;
import org.jlab.btm.persistence.projection.DowntimeSummaryTotals;
import org.jlab.btm.persistence.projection.ReviewDay;
import org.jlab.btm.persistence.projection.ReviewWeek;
import org.jlab.smoothness.business.util.DateIterator;
import org.jlab.smoothness.business.util.TimeUtil;

/**
 * @author ryans
 */
@Stateless
public class ReviewCalendarService {

  @EJB CcAccHourService accHourService;
  @EJB DowntimeService downService;

  @PermitAll
  public List<ReviewWeek> getMonth(Date month) {
    List<ReviewDay> dayList = new ArrayList<>();
    DateRange range = BtmTimeUtil.getMonthRangeWithFullWeeks(month);
    DateIterator iterator = new DateIterator(range.getStart(), range.getEnd());

    // We want to actually include the end of the range (instead of typical open ended interval) so
    // we add one day!
    Date inclusiveEnd = TimeUtil.addDays(range.getEnd(), 1);

    List<CcAccHourService.DayTotals> accTotals =
        accHourService.dayTotals(range.getStart(), inclusiveEnd);
    List<DowntimeService.DayTotals> downTotals =
        downService.dayTotals(range.getStart(), inclusiveEnd);

    int i = 0;
    for (Date d : iterator) {
      ReviewDay review = new ReviewDay();

      review.setDay(d);

      CcAccHourService.DayTotals accTotal = accTotals.get(i);
      CcAccSum at = accTotal.totals;
      review.setAccTotal(at);

      DowntimeService.DayTotals downTotal = downTotals.get(i);
      DowntimeSummaryTotals dt = downTotal.totals;
      review.setDownTotals(dt);

      dayList.add(review);
      i++;
    }

    List<ReviewWeek> weekList = breakIntoWeeks(dayList);

    return weekList;
  }

  private List<ReviewWeek> breakIntoWeeks(List<ReviewDay> dayList) {
    List<ReviewWeek> weeks = new ArrayList<>();

    Iterator<ReviewDay> dayIterator = dayList.iterator();

    int numberOfWeeks = dayList.size() / 7;

    if (dayList.size() % 7 != 0) {
      throw new IllegalArgumentException("Days must fill full weeks");
    }

    for (int i = 1; i <= numberOfWeeks; i++) {
      ReviewWeek week = new ReviewWeek();

      for (int j = 0; j < 7; j++) {
        week.getDayList().add(dayIterator.next());
      }

      // Date firstDayOfWeek = week.getDays().get(0).getYearMonthDay();
      Date lastDayOfWeek =
          week.getDayList()
              .get(6)
              .getDay(); // Use last day of week to capture week 1 which may overlap into previous
      // year!

      // week.setWeek(TimeUtil.getField(lastDayOfWeek, Calendar.WEEK_OF_YEAR));
      // week.setYear(TimeUtil.getField(lastDayOfWeek, Calendar.YEAR));

      weeks.add(week);
    }

    return weeks;
  }
}
