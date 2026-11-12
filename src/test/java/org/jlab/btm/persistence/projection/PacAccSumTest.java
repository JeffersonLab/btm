package org.jlab.btm.persistence.projection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.junit.jupiter.api.Test;

/** Tests run in America/New_York, where the schedule's days are local calendar days. */
class PacAccSumTest {
  private static final double DELTA = 1e-9;

  @Test
  void fullyScheduledSpringForwardDayHasNoOff() {
    PacAccSum sum = new PacAccSum();
    sum.addDay(date("2026-03-08"), "PHYSICS");
    sum.setRangeAndCalculateOff(date("2026-03-08"), date("2026-03-09"));

    assertEquals(23, sum.getPeriodHours(), DELTA);
    assertEquals(1, sum.getPhysicsDays());
    assertEquals(1, sum.getProgramDays());
    assertEquals(23, sum.getPhysicsHours(), DELTA);
    assertEquals(23, sum.getProgramHours(), DELTA);
    assertEquals(0, sum.getOffHours(), DELTA);
    assertEquals(0, sum.getImplicitOffHours(), DELTA);
    assertEquals(0, sum.getTotalOffHours(), DELTA);
  }

  @Test
  void fullyScheduledFallBackDayHasNoOff() {
    PacAccSum sum = new PacAccSum();
    sum.addDay(date("2026-11-01"), "PHYSICS");
    sum.setRangeAndCalculateOff(date("2026-11-01"), date("2026-11-02"));

    assertEquals(25, sum.getPeriodHours(), DELTA);
    assertEquals(1, sum.getPhysicsDays());
    assertEquals(25, sum.getPhysicsHours(), DELTA);
    assertEquals(25, sum.getProgramHours(), DELTA);
    assertEquals(0, sum.getImplicitOffHours(), DELTA);
    assertEquals(0, sum.getTotalOffHours(), DELTA);
  }

  @Test
  void springMonthWithExplicitOffOnTheChangeAndUnscheduledDays() {
    PacAccSum sum = new PacAccSum();
    addDays(sum, "2026-03-01", 7, "PHYSICS");
    sum.addDay(date("2026-03-08"), "OFF"); // 23 hours
    addDays(sum, "2026-03-09", 12, "RESTORE");
    // 21-31 March are not scheduled
    sum.setRangeAndCalculateOff(date("2026-03-01"), date("2026-04-01"));

    assertEquals(31 * 24 - 1, sum.getPeriodHours(), DELTA);
    assertEquals(19, sum.getProgramDays());
    assertEquals(1, sum.getOffDays());
    assertEquals(7 * 24, sum.getPhysicsHours(), DELTA);
    assertEquals(12 * 24, sum.getRestoreHours(), DELTA);
    assertEquals(19 * 24, sum.getProgramHours(), DELTA);
    assertEquals(23, sum.getOffHours(), DELTA);
    assertEquals(11 * 24, sum.getImplicitOffHours(), DELTA);
    assertEquals(23 + 11 * 24, sum.getTotalOffHours(), DELTA);
    assertConsistent(sum);
  }

  @Test
  void autumnMonthWithProgramOnTheChangeExplicitOffAndUnscheduledDays() {
    PacAccSum sum = new PacAccSum();
    addDays(sum, "2026-11-01", 10, "STUDIES"); // 1 November has 25 hours
    sum.addDay(date("2026-11-11"), "ACC");
    sum.addDay(date("2026-11-12"), "OFF");
    // 13-30 November are not scheduled
    sum.setRangeAndCalculateOff(date("2026-11-01"), date("2026-12-01"));

    assertEquals(30 * 24 + 1, sum.getPeriodHours(), DELTA);
    assertEquals(11, sum.getProgramDays());
    assertEquals(10, sum.getStudiesDays());
    assertEquals(1, sum.getAccDays());
    assertEquals(1, sum.getOffDays());
    assertEquals(10 * 24 + 1, sum.getStudiesHours(), DELTA);
    assertEquals(24, sum.getAccHours(), DELTA);
    assertEquals(11 * 24 + 1, sum.getProgramHours(), DELTA);
    assertEquals(24, sum.getOffHours(), DELTA);
    assertEquals(18 * 24, sum.getImplicitOffHours(), DELTA);
    assertEquals(19 * 24, sum.getTotalOffHours(), DELTA);
    assertConsistent(sum);
  }

  @Test
  void unscheduledChangeDayIsImplicitOffForItsRealLength() {
    PacAccSum sum = new PacAccSum();
    sum.addDay(date("2026-03-07"), "PHYSICS");
    sum.addDay(date("2026-03-09"), "PHYSICS");
    sum.setRangeAndCalculateOff(date("2026-03-07"), date("2026-03-10"));

    assertEquals(2 * 24, sum.getProgramHours(), DELTA);
    assertEquals(23, sum.getImplicitOffHours(), DELTA);
    assertEquals(23, sum.getTotalOffHours(), DELTA);
    assertConsistent(sum);
  }

  @Test
  void ordinaryDaysKeepTwentyFourHoursEach() {
    PacAccSum sum = new PacAccSum();
    addDays(sum, "2026-01-05", 3, "PHYSICS");
    sum.addDay(date("2026-01-08"), "RESTORE");
    sum.addDay(date("2026-01-09"), "STUDIES");
    sum.addDay(date("2026-01-10"), "ACC");
    sum.addDay(date("2026-01-11"), "OFF");
    // 12 January is not scheduled
    sum.setRangeAndCalculateOff(date("2026-01-05"), date("2026-01-13"));

    assertEquals(8 * 24, sum.getPeriodHours(), DELTA);
    assertEquals(6, sum.getProgramDays());
    assertEquals(3 * 24, sum.getPhysicsHours(), DELTA);
    assertEquals(24, sum.getRestoreHours(), DELTA);
    assertEquals(24, sum.getStudiesHours(), DELTA);
    assertEquals(24, sum.getAccHours(), DELTA);
    assertEquals(6 * 24, sum.getProgramHours(), DELTA);
    assertEquals(24, sum.getOffHours(), DELTA);
    // The same as the previous calculation from day counts
    assertEquals(
        sum.getPeriodHours() - (sum.getProgramDays() + sum.getOffDays()) * 24,
        sum.getImplicitOffHours(),
        DELTA);
    assertEquals(24, sum.getImplicitOffHours(), DELTA);
    assertEquals(48, sum.getTotalOffHours(), DELTA);
    assertConsistent(sum);
  }

  @Test
  void otherProgramsCountAsProgramOnly() {
    PacAccSum sum = new PacAccSum();
    sum.addDay(date("2026-01-05"), "OTHER");
    sum.setRangeAndCalculateOff(date("2026-01-05"), date("2026-01-06"));

    assertEquals(1, sum.getProgramDays());
    assertEquals(24, sum.getProgramHours(), DELTA);
    assertEquals(0, sum.getPhysicsHours() + sum.getRestoreHours() + sum.getStudiesHours(), DELTA);
    assertEquals(0, sum.getAccHours(), DELTA);
    assertEquals(0, sum.getTotalOffHours(), DELTA);
  }

  private static void assertConsistent(PacAccSum sum) {
    assertEquals(
        sum.getPeriodHours(),
        sum.getProgramHours() + sum.getOffHours() + sum.getImplicitOffHours(),
        DELTA);
    assertEquals(sum.getOffHours() + sum.getImplicitOffHours(), sum.getTotalOffHours(), DELTA);
  }

  private static void addDays(PacAccSum sum, String first, int count, String program) {
    LocalDate day = LocalDate.parse(first);
    for (int i = 0; i < count; i++) {
      sum.addDay(date(day.plusDays(i)), program);
    }
  }

  private static Date date(String date) {
    return date(LocalDate.parse(date));
  }

  private static Date date(LocalDate date) {
    return Date.from(date.atStartOfDay(ZoneId.of("America/New_York")).toInstant());
  }
}
