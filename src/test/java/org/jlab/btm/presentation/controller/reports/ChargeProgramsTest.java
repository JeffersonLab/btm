package org.jlab.btm.presentation.controller.reports;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.jlab.btm.persistence.entity.ExpProgram;
import org.jlab.btm.persistence.entity.ScheduleDay;
import org.jlab.btm.persistence.projection.DailyPrograms;
import org.junit.jupiter.api.Test;

/**
 * Checks the programs the Charge report shows on hover: each schedule day's hall programs, dated at
 * the day's start in the local milliseconds the report's charts plot.
 */
class ChargeProgramsTest {

  private static final ZoneId ZONE = ZoneId.of("America/New_York");

  @Test
  void namesEachHallsProgramByDay() {
    Map<Integer, ExpProgram> programs =
        Map.of(
            1, program("Alpha"), 2, program("Bravo"), 3, program("Charlie"), 4, program("Delta"));

    List<DailyPrograms> days =
        Charge.programsByDay(
            List.of(
                day(LocalDate.of(2026, 3, 7), 1, 2, 3, 4),
                day(LocalDate.of(2026, 3, 8), 4, 3, 2, 1)),
            programs);

    assertEquals(2, days.size());
    assertEquals(
        List.of("Alpha", "Bravo", "Charlie", "Delta"),
        List.of(
            days.get(0).getHallA(),
            days.get(0).getHallB(),
            days.get(0).getHallC(),
            days.get(0).getHallD()));
    assertEquals(
        List.of("Delta", "Charlie", "Bravo", "Alpha"),
        List.of(
            days.get(1).getHallA(),
            days.get(1).getHallB(),
            days.get(1).getHallC(),
            days.get(1).getHallD()));
  }

  @Test
  void datesEachDayAtItsLocalMidnight() {
    // 8 March 2026 is the start of daylight saving time in New York
    for (LocalDate date :
        List.of(
            LocalDate.of(2026, 1, 15),
            LocalDate.of(2026, 3, 8),
            LocalDate.of(2026, 3, 9),
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2026, 11, 1))) {
      DailyPrograms programs = new DailyPrograms(date(date), "A", "B", "C", "D");

      assertEquals(
          Instant.parse(date + "T00:00:00Z").toEpochMilli(),
          programs.getTimestamp(),
          date.toString());
    }
  }

  private static ExpProgram program(String name) {
    ExpProgram program = new ExpProgram();
    program.setName(name);
    return program;
  }

  private static ScheduleDay day(LocalDate date, int a, int b, int c, int d) {
    ScheduleDay day = new ScheduleDay();
    day.setDayMonthYear(date(date));
    day.setHallAProgramId(a);
    day.setHallBProgramId(b);
    day.setHallCProgramId(c);
    day.setHallDProgramId(d);
    return day;
  }

  private static Date date(LocalDate date) {
    return Date.from(date.atStartOfDay(ZONE).toInstant());
  }
}
