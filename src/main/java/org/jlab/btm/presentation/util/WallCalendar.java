package org.jlab.btm.presentation.util;

import org.jlab.btm.business.util.BtmTimeUtil;
import org.jlab.btm.business.util.DateRange;
import org.jlab.smoothness.business.util.DateIterator;

import java.time.MonthDay;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;

/**
 * A Wall Calendar is a monthly calendar formatted in a table layout with the
 * first day of the week starting Sunday and each day contains zero or more
 * ordered notes. Days from previous and next months necessary to create full
 * weeks are included.
 *
 * @param <E> The note element type
 * @author ryans
 */
public class WallCalendar<E> {

    private final List<List<MonthDay>> weekList;
    private final Map<MonthDay, List<E>> noteMap;

    public WallCalendar(YearMonth month) {
        this(Date.from(month.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    public WallCalendar(Date startOfMonth) {
        weekList = new ArrayList<>();
        noteMap = new HashMap<>();

        DateRange range = BtmTimeUtil.getMonthRangeWithFullWeeks(startOfMonth);
        DateIterator iterator = new DateIterator(range.getStart(), range.getEnd());

        List<MonthDay> currentWeek = new ArrayList<>();
        while (iterator.hasNext()) {
            Date d = iterator.next();
            MonthDay md = MonthDay.from(d.toInstant().atZone(ZoneId.systemDefault()));

            currentWeek.add(md);

            if (currentWeek.size() == 7) {
                weekList.add(currentWeek);
                currentWeek = new ArrayList<>();
            }
        }
    }

    public void addNote(MonthDay day, E note) {
        List<E> list = noteMap.get(day);

        if (list == null) {
            list = new ArrayList<>();
            noteMap.put(day, list);
        }

        list.add(note);
    }

    public E getNote(MonthDay day) {
        List<E> list = noteMap.get(day);

        E item = null;

        if (list != null && !list.isEmpty()) {
            item = list.get(0);
        }

        return item;
    }

    public List<E> getNotes(MonthDay day) {
        return noteMap.get(day);
    }

    public List<List<MonthDay>> getWeeks() {
        return weekList;
    }
}
