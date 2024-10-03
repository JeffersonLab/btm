package org.jlab.btm.business.util;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import org.jlab.btm.persistence.enumeration.DataSource;
import org.jlab.smoothness.business.util.DateIterator;
import org.jlab.smoothness.business.util.TimeUtil;

/**
 * @author ryans
 */
public class HourUtil {

  private HourUtil() {
    // Can't instantiate publicly
  }

  /**
   * Determines whether or not the specified range in the past overlaps the EPICS window of cached
   * time accounting data.
   *
   * <p>The EPICS window is currently the previous week as of the present.
   *
   * @param endDayAndHour The end day and hour of the range.
   * @return true if the range overlaps, false otherwise.
   */
  public static boolean isInEpicsWindow(Date endDayAndHour) {
    return TimeUtil.withinLastWeek(endDayAndHour);
  }

  public static <T extends HourEntity> Map<Date, T> createHourMap(List<T> hours) {
    Iterator<T> iterator = hours.iterator();
    Map<Date, T> map = new HashMap<>();

    while (iterator.hasNext()) {
      T h = iterator.next();
      Date hour = h.getDayAndHour();
      map.put(hour, h);
    }

    return map;
  }

  public static <T extends HourEntity> List<T> subset(
      Date startDayAndHour, Date endDayAndHour, List<T> hours) {
    List<T> result = new ArrayList<>();

    Map<Date, T> hourMap = createHourMap(hours);

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

  public static <T extends HourEntity> List<T> fillMissingHoursAndSetSource(
      Map<Date, T> dbHourMap, Map<Date, T> epicsHourMap, Date start, Date end, Class<T> clazz) {
    List<T> filledList = new ArrayList<>();

    DateIterator iterator = new DateIterator(start, end, Calendar.HOUR_OF_DAY);

    T hourEntity;

    for (Date hour : iterator) {
      if (dbHourMap.containsKey(hour)) {
        hourEntity = dbHourMap.get(hour);
        hourEntity.setSource(DataSource.DATABASE);
      } else if (epicsHourMap.containsKey(hour)) {
        hourEntity = epicsHourMap.get(hour);
        hourEntity.setSource(DataSource.EPICS);
      } else {
        try {
          hourEntity = clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException
            | IllegalAccessException
            | NoSuchMethodException
            | InvocationTargetException e) {
          throw new RuntimeException("Unable to instantiate class", e);
        }
        hourEntity.setDayAndHour(hour);
        hourEntity.setSource(DataSource.NONE);
      }

      filledList.add(hourEntity);
    }

    return filledList;
  }
}
