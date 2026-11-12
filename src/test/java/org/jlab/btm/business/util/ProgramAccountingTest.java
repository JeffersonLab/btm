package org.jlab.btm.business.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jlab.btm.business.util.ProgramAccounting.Span;
import org.jlab.btm.persistence.entity.CcAccHour;
import org.jlab.btm.persistence.entity.CcProgramInterval;
import org.jlab.btm.persistence.enumeration.TimelineProgram;
import org.junit.jupiter.api.Test;

class ProgramAccountingTest {
  /** Long after every time in these tests, so that nothing is cut off at the present. */
  private static final Instant LATER = at("2027-01-01T00:00:00Z");

  @Test
  void countsAWholeHourOfOneProgram() {
    List<CcAccHour> hours =
        measured(
            timeline(interval(TimelineProgram.PHYSICS, "2026-01-01T00:00:00Z")),
            List.of(),
            "2026-01-01T05:00:00Z",
            "2026-01-01T06:00:00Z",
            LATER);

    assertEquals(2, hours.size());
    assertHour(hours.get(0), "2026-01-01T05:00:00Z", 3600, 0, 0, 0, 0, 0);
    assertHour(hours.get(1), "2026-01-01T06:00:00Z", 3600, 0, 0, 0, 0, 0);
  }

  @Test
  void splitsAnHourWhereTheProgramChanges() {
    List<CcAccHour> hours =
        measured(
            timeline(
                interval(TimelineProgram.OFF, "2026-01-01T00:00:00Z"),
                interval(TimelineProgram.RESTORE, "2026-01-01T05:15:00Z"),
                interval(TimelineProgram.ACC, "2026-01-01T05:30:00Z"),
                interval(TimelineProgram.PHYSICS, "2026-01-01T05:48:00Z")),
            List.of(),
            "2026-01-01T05:00:00Z",
            "2026-01-01T05:00:00Z",
            LATER);

    assertEquals(1, hours.size());
    assertHour(hours.get(0), "2026-01-01T05:00:00Z", 720, 0, 900, 1080, 0, 900);
  }

  @Test
  void countsDowntimeDuringInternalProgramsAsInternalDown() {
    List<CcAccHour> hours =
        measured(
            timeline(
                interval(TimelineProgram.STUDIES, "2026-01-01T00:00:00Z"),
                interval(TimelineProgram.RESTORE, "2026-01-01T05:21:00Z"),
                interval(TimelineProgram.ACC, "2026-01-01T05:42:00Z")),
            List.of(
                span("2026-01-01T05:09:00Z", "2026-01-01T05:27:00Z"),
                span("2026-01-01T05:51:00Z", "2026-01-01T06:09:00Z")),
            "2026-01-01T05:00:00Z",
            "2026-01-01T06:00:00Z",
            LATER);

    // Studies 21 min with 12 down, Restore 21 min with 6 down, ACC 18 min with 9 down
    assertHour(hours.get(0), "2026-01-01T05:00:00Z", 0, 540, 900, 540, 1620, 0);
    assertHour(hours.get(1), "2026-01-01T06:00:00Z", 0, 0, 0, 3060, 540, 0);
  }

  @Test
  void leavesDowntimeDuringPhysicsAndOffWhereItIs() {
    List<CcAccHour> hours =
        measured(
            timeline(
                interval(TimelineProgram.PHYSICS, "2026-01-01T00:00:00Z"),
                interval(TimelineProgram.OFF, "2026-01-01T05:30:00Z")),
            List.of(span("2026-01-01T05:00:00Z", "2026-01-01T06:00:00Z")),
            "2026-01-01T05:00:00Z",
            "2026-01-01T05:00:00Z",
            LATER);

    assertHour(hours.get(0), "2026-01-01T05:00:00Z", 1800, 0, 0, 0, 0, 1800);
  }

  @Test
  void countsOverlappingDowntimeOnce() {
    List<CcAccHour> hours =
        measured(
            timeline(interval(TimelineProgram.STUDIES, "2026-01-01T00:00:00Z")),
            List.of(
                span("2026-01-01T05:30:00Z", "2026-01-01T05:51:00Z"),
                span("2026-01-01T05:00:00Z", "2026-01-01T05:42:00Z"),
                span("2026-01-01T05:09:00Z", "2026-01-01T05:21:00Z")),
            "2026-01-01T05:00:00Z",
            "2026-01-01T05:00:00Z",
            LATER);

    assertHour(hours.get(0), "2026-01-01T05:00:00Z", 0, 540, 0, 0, 3060, 0);
  }

  @Test
  void countsAnOpenDowntimeUntilNow() {
    List<CcAccHour> hours =
        measured(
            timeline(interval(TimelineProgram.STUDIES, "2026-01-01T00:00:00Z")),
            List.of(new Span(at("2026-01-01T05:30:00Z"), null)),
            "2026-01-01T05:00:00Z",
            "2026-01-01T07:00:00Z",
            at("2026-01-01T06:15:00Z"));

    assertEquals(2, hours.size(), "the hour that hasn't started is left out");
    assertHour(hours.get(0), "2026-01-01T05:00:00Z", 0, 1800, 0, 0, 1800, 0);
    // The hour in progress: 15 minutes so far, all of it down
    assertHour(hours.get(1), "2026-01-01T06:00:00Z", 0, 0, 0, 0, 900, 0);
  }

  @Test
  void leavesOutHoursTheTimelineDoesNotCover() {
    List<CcAccHour> hours =
        measured(
            timeline(interval(TimelineProgram.PHYSICS, "2026-01-01T05:30:00Z")),
            List.of(),
            "2026-01-01T04:00:00Z",
            "2026-01-01T06:00:00Z",
            LATER);

    // 04 is before the timeline starts, and 05 is only half covered
    assertEquals(1, hours.size());
    assertHour(hours.get(0), "2026-01-01T06:00:00Z", 3600, 0, 0, 0, 0, 0);

    assertTrue(
        measured(
                new ArrayList<>(), List.of(), "2026-01-01T04:00:00Z", "2026-01-01T06:00:00Z", LATER)
            .isEmpty());
  }

  /** The night daylight saving time ends has two hours that both read 01:00 on the clock. */
  @Test
  void countsBothHoursOfARepeatedClockHour() {
    List<CcAccHour> hours =
        measured(
            timeline(
                interval(TimelineProgram.PHYSICS, "2026-11-01T00:00:00Z"),
                interval(TimelineProgram.STUDIES, "2026-11-01T06:00:00Z")),
            List.of(span("2026-11-01T06:30:00Z", "2026-11-01T06:45:00Z")),
            "2026-11-01T05:00:00Z", // 01:00 EDT
            "2026-11-01T06:00:00Z", // 01:00 EST
            LATER);

    assertEquals(2, hours.size());
    assertHour(hours.get(0), "2026-11-01T05:00:00Z", 3600, 0, 0, 0, 0, 0);
    assertHour(hours.get(1), "2026-11-01T06:00:00Z", 0, 2700, 0, 0, 900, 0);
  }

  @Test
  void roundsToHundredthsOfAnHourKeepingTheHourWhole() {
    List<CcAccHour> hours =
        measured(
            timeline(
                interval(TimelineProgram.PHYSICS, "2026-01-01T00:00:00Z"),
                interval(TimelineProgram.STUDIES, "2026-01-01T05:20:17Z")),
            List.of(span("2026-01-01T05:40:05Z", "2026-01-01T05:50:55Z")),
            "2026-01-01T05:00:00Z",
            "2026-01-01T05:00:00Z",
            LATER);

    // Exactly: physics 1217 s, studies 1733 s, internal down 650 s
    CcAccHour hour = hours.get(0);

    assertHour(hour, "2026-01-01T05:00:00Z", 1224, 1728, 0, 0, 648, 0);
    assertEquals(3600, hour.getUpSeconds() + hour.getStudiesSeconds() + hour.getDownSeconds());
  }

  @Test
  void roundingGivesLeftOverHundredthsToTheLargestRemainders() {
    int[] statuses = {1217, 1733, 0, 0, 650, 0};

    ProgramAccounting.roundToHundredthsOfAnHour(statuses);

    assertArrayEquals(new int[] {1224, 1728, 0, 0, 648, 0}, statuses);

    int[] exact = {1800, 0, 0, 0, 0, 1800};

    ProgramAccounting.roundToHundredthsOfAnHour(exact);

    assertArrayEquals(new int[] {1800, 0, 0, 0, 0, 1800}, exact);
  }

  @Test
  void mergesDowntimeAndCutsItOffAtNow() {
    Instant now = at("2026-01-01T06:00:00Z");

    assertEquals(
        List.of(
            span("2026-01-01T01:00:00Z", "2026-01-01T03:00:00Z"),
            span("2026-01-01T04:00:00Z", "2026-01-01T06:00:00Z")),
        ProgramAccounting.merge(
            List.of(
                span("2026-01-01T02:00:00Z", "2026-01-01T03:00:00Z"),
                span("2026-01-01T05:00:00Z", "2026-01-01T09:00:00Z"),
                new Span(at("2026-01-01T04:00:00Z"), null),
                span("2026-01-01T01:00:00Z", "2026-01-01T02:00:00Z"),
                span("2026-01-01T07:00:00Z", "2026-01-01T08:00:00Z")),
            now));
  }

  private static List<CcAccHour> measured(
      List<CcProgramInterval> intervals,
      List<Span> downtime,
      String startHour,
      String endHour,
      Instant now) {
    return ProgramAccounting.measuredHours(
        intervals, downtime, Date.from(at(startHour)), Date.from(at(endHour)), now);
  }

  /** As the service returns them: each interval ends where the next starts, the last has no end. */
  private static List<CcProgramInterval> timeline(CcProgramInterval... intervals) {
    for (int i = 0; i < intervals.length - 1; i++) {
      intervals[i].setEndTime(intervals[i + 1].getStartTime());
    }

    return List.of(intervals);
  }

  private static CcProgramInterval interval(TimelineProgram program, String start) {
    CcProgramInterval interval = new CcProgramInterval();
    interval.setProgram(program);
    interval.setStartTime(at(start));
    return interval;
  }

  private static Span span(String start, String end) {
    return new Span(at(start), at(end));
  }

  private static Instant at(String instant) {
    return Instant.parse(instant);
  }

  private static void assertHour(
      CcAccHour hour,
      String start,
      int physics,
      int studies,
      int restore,
      int acc,
      int internalDown,
      int off) {
    assertEquals(Date.from(at(start)), hour.getDayAndHour());
    assertArrayEquals(
        new int[] {physics, studies, restore, acc, internalDown, off},
        new int[] {
          hour.getUpSeconds(),
          hour.getStudiesSeconds(),
          hour.getRestoreSeconds(),
          hour.getAccSeconds(),
          hour.getDownSeconds(),
          hour.getSadSeconds()
        },
        "physics, studies, restore, acc, internal down, off");
  }
}
