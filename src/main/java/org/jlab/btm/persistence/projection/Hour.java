package org.jlab.btm.persistence.projection;

import org.jlab.btm.business.util.HourEntity;
import org.jlab.btm.persistence.enumeration.DataSource;
import org.jlab.smoothness.business.util.DateIterator;

import java.io.Serializable;
import java.util.*;

/**
 * A generic hour.
 *
 * @author ryans
 */
public abstract class Hour implements
        Comparable<Hour>, Serializable, HourEntity {
    public static final int SECONDS_PER_HOUR = 3600;

    protected DataSource source = DataSource.NONE;

    /**
     * Creates a Map of Date (dayAndHour) to Hour objects.
     *
     * @param <T>
     * @param list A collection of Hour objects.
     * @return A Map of Date to Hour objects.
     */
    public static <T extends Hour> Map<Date, T> getHourMap(List<T> list) {
        Iterator<T> iterator = list.iterator();
        Map<Date, T> map = new HashMap<>();

        while (iterator.hasNext()) {
            T h = iterator.next();
            Date hour = h.getDayAndHour();
            map.put(hour, h);
        }

        return map;
    }

    public static <T extends Hour> List<T> subset(Date startDayAndHour, Date endDayAndHour, List<T> hours) {
        List<T> result = new ArrayList<>();

        Map<Date, T> hourMap = Hour.getHourMap(hours);

        // We iterator over the range of hours
        DateIterator iterator = new DateIterator(startDayAndHour, endDayAndHour, Calendar.HOUR_OF_DAY);

        while (iterator.hasNext()) {
            Date hour = iterator.next();
            T h = hourMap.get(hour);

            if (h != null) {
                result.add(h);
            }
        }

        return result;
    }

    @Override
    public abstract Date getDayAndHour();

    @Override
    public abstract void setDayAndHour(Date dayAndhour);

    public DataSource getSource() {
        return source;
    }

    @Override
    public void setSource(DataSource source) {
        this.source = source;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.getDayAndHour() != null ?
                this.getDayAndHour().hashCode() : 0);
        return hash;
    }

    /**
     * Compares this object with another for meaningful equality.
     *
     * @param object The Hour to compare to.
     * @return true if meaningfully equivalent, false otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Hour)) {
            return false;
        }
        Hour other = (Hour) object;
        return !((this.getDayAndHour() == null && other.getDayAndHour() != null) ||
                (this.getDayAndHour() != null &&
                        !this.getDayAndHour().equals(other.getDayAndHour())));
    }

    /*
     * Comparison is done using the alternate key (also a natural key) hour.
     *
     * The primary key id is not used because it isn't available on newly
     * created objects (it is a surrogate key) and because it wouldn't provide a
     * natural ordering (just insertion order).
     *
     * Fields are accessed via get methods to prompt lazy initializer if needed.
     *
     * @param o The Hour to compare to.
     * @return -1 if less than, 0 if equal, and 1 if greater than.
     */
    @Override
    public int compareTo(Hour o) {
        return this.getDayAndHour().compareTo(o.getDayAndHour());
    }
}
