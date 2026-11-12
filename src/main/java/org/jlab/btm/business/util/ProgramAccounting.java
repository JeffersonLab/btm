package org.jlab.btm.business.util;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.jlab.btm.persistence.entity.CcAccHour;
import org.jlab.btm.persistence.entity.CcProgramInterval;
import org.jlab.btm.persistence.enumeration.TimelineProgram;

/**
 * Turns the State Timeline's program intervals and DTM's downtime into accelerator program time per
 * hour.
 *
 * <p>An hour's time in each program is how long the timeline had that program. Downtime during
 * Studies, Restore or ACC is taken out of that program and counted as Internal Down; downtime
 * during Physics or Off is left where it is, since it isn't internal.
 */
public final class ProgramAccounting {

  private static final int SECONDS_PER_HOUR = 3600;

  /** Crew Chiefs work in hundredths of an hour, as the measurements from BOOM are rounded to. */
  private static final int SECONDS_PER_HUNDREDTH_OF_HOUR = 36;

  private ProgramAccounting() {
    // Can't instantiate publicly
  }

  /**
   * A span of time.
   *
   * @param start The start
   * @param end The end, exclusive; null if it hasn't ended yet
   */
  public record Span(Instant start, Instant end) {}

  /**
   * Calculates the hours that the timeline covers.
   *
   * <p>An hour that has started but not ended is calculated up to now. Hours that haven't started,
   * and hours the timeline doesn't cover from their start to their end (or to now), are left out.
   *
   * @param intervals The program intervals that overlap the hours, earliest first; an interval
   *     without an end runs on
   * @param downtime When the machine was down; spans may overlap each other
   * @param startHour The first hour
   * @param endHour The last hour, inclusive
   * @param now The present; nothing after it is counted
   * @return The hours, earliest first, in seconds rounded to hundredths of an hour
   */
  public static List<CcAccHour> measuredHours(
      List<CcProgramInterval> intervals,
      List<Span> downtime,
      Date startHour,
      Date endHour,
      Instant now) {
    List<CcAccHour> hours = new ArrayList<>();
    List<Span> down = merge(downtime, now);

    Instant last = endHour.toInstant();

    for (Instant hourStart = startHour.toInstant();
        !hourStart.isAfter(last) && hourStart.isBefore(now);
        hourStart = hourStart.plusSeconds(SECONDS_PER_HOUR)) {
      Instant hourEnd = earlier(hourStart.plusSeconds(SECONDS_PER_HOUR), now);

      int[] programSeconds = new int[TimelineProgram.values().length];
      int[] downSeconds = new int[TimelineProgram.values().length];
      long covered = 0;

      for (CcProgramInterval interval : intervals) {
        Instant from = later(interval.getStartTime(), hourStart);
        Instant to =
            interval.getEndTime() == null ? hourEnd : earlier(interval.getEndTime(), hourEnd);

        if (!from.isBefore(to)) {
          continue;
        }

        int program = interval.getProgram().ordinal();

        programSeconds[program] += (int) seconds(from, to);
        covered += seconds(from, to);

        if (interval.getProgram().isInternal()) {
          downSeconds[program] += (int) overlap(down, from, to);
        }
      }

      if (covered != seconds(hourStart, hourEnd)) {
        continue; // The timeline starts after this hour does
      }

      int studies = TimelineProgram.STUDIES.ordinal();
      int restore = TimelineProgram.RESTORE.ordinal();
      int acc = TimelineProgram.ACC.ordinal();

      int[] statuses = {
        programSeconds[TimelineProgram.PHYSICS.ordinal()],
        programSeconds[studies] - downSeconds[studies],
        programSeconds[restore] - downSeconds[restore],
        programSeconds[acc] - downSeconds[acc],
        downSeconds[studies] + downSeconds[restore] + downSeconds[acc],
        programSeconds[TimelineProgram.OFF.ordinal()]
      };

      roundToHundredthsOfAnHour(statuses);

      CcAccHour hour = new CcAccHour();
      hour.setDayAndHour(Date.from(hourStart));
      hour.setUpSeconds((short) statuses[0]);
      hour.setStudiesSeconds((short) statuses[1]);
      hour.setRestoreSeconds((short) statuses[2]);
      hour.setAccSeconds((short) statuses[3]);
      hour.setDownSeconds((short) statuses[4]);
      hour.setSadSeconds((short) statuses[5]);

      hours.add(hour);
    }

    return hours;
  }

  /**
   * Rounds each status to a hundredth of an hour, keeping their total: a whole hour stays a whole
   * hour. The hundredths that rounding down leaves over go to the statuses that lost the most.
   *
   * @param statuses Seconds per status; modified in place
   */
  static void roundToHundredthsOfAnHour(int[] statuses) {
    int total = 0;
    int[] remainders = new int[statuses.length];

    for (int i = 0; i < statuses.length; i++) {
      total += statuses[i];
      remainders[i] = statuses[i] % SECONDS_PER_HUNDREDTH_OF_HOUR;
      statuses[i] -= remainders[i];
    }

    int rounded = 0;

    for (int status : statuses) {
      rounded += status;
    }

    int leftOver =
        (total / SECONDS_PER_HUNDREDTH_OF_HOUR) - (rounded / SECONDS_PER_HUNDREDTH_OF_HOUR);

    for (; leftOver > 0; leftOver--) {
      int largest = 0;

      for (int i = 1; i < remainders.length; i++) {
        if (remainders[i] > remainders[largest]) {
          largest = i;
        }
      }

      statuses[largest] += SECONDS_PER_HUNDREDTH_OF_HOUR;
      remainders[largest] = 0;
    }
  }

  /**
   * @param spans Spans in any order, which may overlap; one without an end runs until now
   * @param now The present; spans are cut off there
   * @return The same time as spans that don't overlap, earliest first
   */
  static List<Span> merge(List<Span> spans, Instant now) {
    List<Span> sorted = new ArrayList<>();

    for (Span span : spans) {
      Instant end = span.end() == null ? now : earlier(span.end(), now);

      if (span.start().isBefore(end)) {
        sorted.add(new Span(span.start(), end));
      }
    }

    sorted.sort(Comparator.comparing(Span::start));

    List<Span> merged = new ArrayList<>();

    for (Span span : sorted) {
      Span previous = merged.isEmpty() ? null : merged.get(merged.size() - 1);

      if (previous != null && !span.start().isAfter(previous.end())) {
        merged.set(
            merged.size() - 1, new Span(previous.start(), later(previous.end(), span.end())));
      } else {
        merged.add(span);
      }
    }

    return merged;
  }

  /**
   * @param spans Spans that don't overlap each other
   * @return How many seconds of the spans fall between from and to
   */
  private static long overlap(List<Span> spans, Instant from, Instant to) {
    long seconds = 0;

    for (Span span : spans) {
      Instant start = later(span.start(), from);
      Instant end = earlier(span.end(), to);

      if (start.isBefore(end)) {
        seconds += seconds(start, end);
      }
    }

    return seconds;
  }

  private static long seconds(Instant from, Instant to) {
    return to.getEpochSecond() - from.getEpochSecond();
  }

  private static Instant earlier(Instant a, Instant b) {
    return a.isBefore(b) ? a : b;
  }

  private static Instant later(Instant a, Instant b) {
    return a.isAfter(b) ? a : b;
  }
}
