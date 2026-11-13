package org.jlab.btm.business.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Finds runs in a schedule: a run is a stretch of consecutive scheduled days whose accelerator
 * program is not OFF. A day without a schedule counts as OFF, so a run ends where the schedule ends
 * even if the days after aren't marked OFF.
 */
public final class RunCalculator {

  /** The accelerator program of days the machine is scheduled off. */
  public static final String OFF = "OFF";

  private RunCalculator() {}

  /**
   * The days of a run.
   *
   * @param start The first day of the run
   * @param end The day after the last day of the run
   */
  public record Bounds(LocalDate start, LocalDate end) {

    public Bounds {
      if (start == null || end == null) {
        throw new IllegalArgumentException("A run needs a start and an end");
      }

      if (!start.isBefore(end)) {
        throw new IllegalArgumentException("A run must end after it starts: " + start + " " + end);
      }
    }

    /**
     * @param other Another run
     * @return Whether the two runs share a day
     */
    public boolean overlaps(Bounds other) {
      return start.isBefore(other.end) && other.start.isBefore(end);
    }

    /**
     * @return The last day of the run
     */
    public LocalDate lastDay() {
      return end.minusDays(1);
    }
  }

  /**
   * @param programByDay The accelerator program of each scheduled day, in any order
   * @return The runs, earliest first
   */
  public static List<Bounds> findRuns(Map<LocalDate, String> programByDay) {
    SortedMap<LocalDate, String> days = new TreeMap<>(programByDay);
    List<Bounds> runs = new ArrayList<>();

    LocalDate start = null;
    LocalDate end = null;

    for (Map.Entry<LocalDate, String> day : days.entrySet()) {
      boolean on = !OFF.equals(day.getValue());

      if (start != null && (!on || !day.getKey().equals(end))) {
        runs.add(new Bounds(start, end)); // An OFF day or a day without a schedule ends it
        start = null;
      }

      if (on) {
        if (start == null) {
          start = day.getKey();
        }

        end = day.getKey().plusDays(1);
      }
    }

    if (start != null) {
      runs.add(new Bounds(start, end));
    }

    return runs;
  }

  /**
   * @param date The start of a day in the server's time zone, as the schedule's days are kept
   * @return The day
   */
  public static LocalDate toLocalDate(Date date) {
    // A java.sql.Date, as JDBC may return, has no toInstant()
    return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
  }

  /**
   * @param day A day
   * @return The start of the day in the server's time zone
   */
  public static Date toDate(LocalDate day) {
    return Date.from(day.atStartOfDay(ZoneId.systemDefault()).toInstant());
  }
}
