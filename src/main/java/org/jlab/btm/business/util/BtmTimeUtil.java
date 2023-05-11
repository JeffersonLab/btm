package org.jlab.btm.business.util;

import org.jlab.smoothness.business.util.TimeUtil;

import java.util.Calendar;
import java.util.Date;

public class BtmTimeUtil {

    private BtmTimeUtil() {
        // private constructor
    }

    public static Date add(Date date, int amount, int field) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(field, amount);
        return c.getTime();
    }

    public static Date subtract(Date date, int amount, int field) {
        return BtmTimeUtil.add(date, -amount, field);
    }

    public static int getField(Date date, int field) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(field);
    }

    public static DateRange getMonthRange(Date yearMonth) {
        Date start = TimeUtil.startOfMonth(yearMonth, Calendar.getInstance());
        Date end = TimeUtil.endOfMonth(yearMonth, Calendar.getInstance());

        return new DateRange(start, end);
    }

    public static DateRange getMonthRangeWithFullWeeks(Date yearMonth) {
        DateRange monthRange = BtmTimeUtil.getMonthRange(yearMonth);

        int firstDayOfWeekInMonth = BtmTimeUtil.getField(monthRange.getStart(), Calendar.DAY_OF_WEEK);
        int lastDayOfWeekInMonth = BtmTimeUtil.getField(monthRange.getEnd(), Calendar.DAY_OF_WEEK);

        int daysToSubtractFromStart = firstDayOfWeekInMonth - 1;
        int daysToAddToEnd = 7 - lastDayOfWeekInMonth;

        Date start
                = BtmTimeUtil.subtract(monthRange.getStart(), daysToSubtractFromStart, Calendar.DATE);
        Date end = BtmTimeUtil.add(monthRange.getEnd(), daysToAddToEnd, Calendar.DATE);

        return new DateRange(start, end);
    }

    public static Date getCurrentYearMonthDay() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTime();
    }

    public static boolean isToday(Date date) {
        if(date == null) {
            return false;
        }

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime().equals(getCurrentYearMonthDay());
    }

    public static Date startOfWeek(Date today, int dayOfWeek) {
        Calendar c = Calendar.getInstance();
        c.setTime(today);
        int currentDayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        int distance = dayOfWeek - currentDayOfWeek;
        if(distance < 0) {
            distance = 7 + distance;
        }
        c.set(Calendar.DATE, c.get(Calendar.DATE) + distance - 7);

        return c.getTime();
    }
}
