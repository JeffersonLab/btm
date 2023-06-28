package org.jlab.btm.business.util;

import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.enumeration.Shift;

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

    public static Shift calculateExperimenterShift(Date dayAndHour) {
        Calendar cal = Calendar.getInstance();
        int hour;
        Shift shift;

        cal.setTime(dayAndHour);
        hour = cal.get(Calendar.HOUR_OF_DAY);

        if (hour <= 7) {
            shift = Shift.OWL;
        } else if (hour <= 15) {
            shift = Shift.DAY;
        } else {
            shift = Shift.SWING;
        }

        return shift;
    }

    public static Date getCurrentExperimenterShiftDay(Date date) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(date);

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    public static boolean isFirstHourOfExperimenterShift(Date now) {
        boolean firstHour = false;

        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        if (hour == 0 || hour == 8 || hour == 16) {
            firstHour = true;
        }

        return firstHour;
    }

    public static Date getExperimenterStartDayAndHour(Date day, Shift shift) {
        int hour;
        switch (shift) {
            case OWL:
                hour = 0;
                break;
            case DAY:
                hour = 8;
                break;
            case SWING:
                hour = 16;
                break;
            default:
                throw new IllegalArgumentException("Unrecognized shift: " + shift);
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(day);
        cal.set(Calendar.HOUR_OF_DAY, hour);

        return cal.getTime();
    }

    public static Date getExperimenterEndDayAndHour(Date day, Shift shift) {
        int hour;
        switch (shift) {
            case OWL:
                hour = 7;
                break;
            case DAY:
                hour = 15;
                break;
            case SWING:
                hour = 23;
                break;
            default:
                throw new IllegalArgumentException("Unrecognized shift: " + shift);
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(day);
        cal.set(Calendar.HOUR_OF_DAY, hour);

        return cal.getTime();
    }

    public static Date getExpShiftStart(Date dateInShift) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateInShift);

        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        int hour = cal.get(Calendar.HOUR_OF_DAY);

        if (hour <= 7) {
            cal.set(Calendar.HOUR_OF_DAY, 0);
        } else if (hour <= 15) {
            cal.set(Calendar.HOUR_OF_DAY, 8);
        } else {
            cal.set(Calendar.HOUR_OF_DAY, 16);
        }

        return cal.getTime();
    }

    public static boolean isStartOfDay(Date date) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(date);

        int startHourOfDay = cal.get(Calendar.HOUR_OF_DAY);

        return (startHourOfDay == 0);
    }
}

