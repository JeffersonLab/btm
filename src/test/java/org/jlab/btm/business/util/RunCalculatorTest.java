package org.jlab.btm.business.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.btm.business.util.RunCalculator.Bounds;
import org.junit.jupiter.api.Test;

class RunCalculatorTest {

  @Test
  void findsNoRunsWithoutSchedules() {
    assertEquals(List.of(), RunCalculator.findRuns(Map.of()));
  }

  @Test
  void findsNoRunsWhenEverythingIsOff() {
    Map<LocalDate, String> days = new HashMap<>();
    put(days, "2026-01-01", 31, "OFF");

    assertEquals(List.of(), RunCalculator.findRuns(days));
  }

  @Test
  void aRunIsTheDaysBetweenOffDaysWhateverTheirProgram() {
    Map<LocalDate, String> days = new HashMap<>();
    put(days, "2026-01-01", 10, "OFF");
    put(days, "2026-01-11", 3, "RESTORE");
    put(days, "2026-01-14", 1, "STUDIES");
    put(days, "2026-01-15", 5, "PHYSICS");
    put(days, "2026-01-20", 1, "DOWN");
    put(days, "2026-01-21", 1, "ACC");
    put(days, "2026-01-22", 1, "FACDEV");
    put(days, "2026-01-23", 1, "TBD");
    put(days, "2026-01-24", 8, "OFF");

    assertEquals(List.of(bounds("2026-01-11", "2026-01-24")), RunCalculator.findRuns(days));
  }

  @Test
  void anOffDaySplitsARun() {
    Map<LocalDate, String> days = new HashMap<>();
    put(days, "2026-03-01", 10, "PHYSICS");
    put(days, "2026-03-11", 1, "OFF");
    put(days, "2026-03-12", 20, "PHYSICS");

    assertEquals(
        List.of(bounds("2026-03-01", "2026-03-11"), bounds("2026-03-12", "2026-04-01")),
        RunCalculator.findRuns(days));
  }

  /** Before, a run was found only if every month had its days, with OFF ones explicit. */
  @Test
  void daysWithoutAScheduleCountAsOff() {
    Map<LocalDate, String> days = new HashMap<>();
    put(days, "2026-01-20", 12, "PHYSICS"); // February has no schedule
    put(days, "2026-03-01", 5, "PHYSICS");
    put(days, "2026-03-10", 2, "PHYSICS"); // 6th to 9th left out

    assertEquals(
        List.of(
            bounds("2026-01-20", "2026-02-01"),
            bounds("2026-03-01", "2026-03-06"),
            bounds("2026-03-10", "2026-03-12")),
        RunCalculator.findRuns(days));
  }

  @Test
  void aRunContinuesAcrossMonthsAndYears() {
    Map<LocalDate, String> days = new HashMap<>();
    put(days, "2025-12-01", 15, "OFF");
    put(days, "2025-12-16", 47, "PHYSICS");
    put(days, "2026-02-01", 28, "OFF");

    assertEquals(List.of(bounds("2025-12-16", "2026-02-01")), RunCalculator.findRuns(days));
  }

  @Test
  void aOneDayRunEndsTheNextDay() {
    Map<LocalDate, String> days = new HashMap<>();
    put(days, "2026-05-01", 1, "STUDIES");

    List<Bounds> runs = RunCalculator.findRuns(days);

    assertEquals(List.of(bounds("2026-05-01", "2026-05-02")), runs);
    assertEquals(LocalDate.parse("2026-05-01"), runs.get(0).lastDay());
  }

  @Test
  void runsOverlapOnlyWhenTheyShareADay() {
    Bounds january = bounds("2026-01-01", "2026-02-01");

    assertTrue(january.overlaps(bounds("2026-01-31", "2026-02-10")));
    assertTrue(january.overlaps(bounds("2025-12-01", "2026-01-02")));
    assertTrue(january.overlaps(bounds("2026-01-10", "2026-01-11")));
    assertFalse(january.overlaps(bounds("2026-02-01", "2026-02-10")));
    assertFalse(january.overlaps(bounds("2025-12-01", "2026-01-01")));
  }

  @Test
  void aRunMustEndAfterItStarts() {
    assertThrows(IllegalArgumentException.class, () -> bounds("2026-01-01", "2026-01-01"));
    assertThrows(IllegalArgumentException.class, () -> bounds("2026-01-02", "2026-01-01"));
  }

  /** The night daylight saving time ends is 25 hours long, and its start is still midnight. */
  @Test
  void convertsDaysAcrossDaylightSavingChanges() {
    LocalDate day = LocalDate.parse("2026-11-01");
    Date start = RunCalculator.toDate(day);

    assertEquals(day, RunCalculator.toLocalDate(start));
    assertEquals(day, RunCalculator.toLocalDate(new java.sql.Date(start.getTime())));
    assertEquals(
        25L * 60 * 60 * 1000, RunCalculator.toDate(day.plusDays(1)).getTime() - start.getTime());
  }

  private static void put(Map<LocalDate, String> days, String first, int count, String program) {
    LocalDate day = LocalDate.parse(first);

    for (int i = 0; i < count; i++) {
      days.put(day.plusDays(i), program);
    }
  }

  private static Bounds bounds(String start, String end) {
    return new Bounds(LocalDate.parse(start), LocalDate.parse(end));
  }
}
