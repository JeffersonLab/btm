package org.jlab.btm.business.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.jlab.smoothness.persistence.enumeration.Shift;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class BtmTimeUtilTest {
  private static final ZoneId ZONE = ZoneId.of("America/New_York");

  @ParameterizedTest
  @CsvSource({"0, OWL", "7, OWL", "8, DAY", "15, DAY", "16, SWING", "23, SWING"})
  void classifiesExperimenterShiftAtBoundaries(int hour, Shift expected) {
    assertEquals(expected, BtmTimeUtil.calculateExperimenterShift(date(2026, 10, 27, hour, 59)));
  }

  @ParameterizedTest
  @CsvSource({"0, 0", "7, 0", "8, 8", "15, 8", "16, 16", "23, 16"})
  void shiftStartClearsMinutesSecondsAndMilliseconds(int hour, int expectedHour) {
    Date input = new Date(date(2026, 10, 27, hour, 37).getTime() + 12_345);
    Date original = new Date(input.getTime());

    assertEquals(date(2026, 10, 27, expectedHour, 0), BtmTimeUtil.getExpShiftStart(input));
    assertEquals(original, input);
  }

  @ParameterizedTest
  @ValueSource(
      ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23})
  void identifiesOnlyFirstHoursOfExperimenterShifts(int hour) {
    assertEquals(
        hour == 0 || hour == 8 || hour == 16,
        BtmTimeUtil.isFirstHourOfExperimenterShift(date(2026, 10, 27, hour, 30)));
  }

  @ParameterizedTest
  @CsvSource({"OWL, 0, 7", "DAY, 8, 15", "SWING, 16, 23"})
  void derivesFirstAndLastHourForEachShift(Shift shift, int start, int end) {
    Date day = date(2026, 10, 27, 0, 0);
    assertEquals(
        date(2026, 10, 27, start, 0), BtmTimeUtil.getExperimenterStartDayAndHour(day, shift));
    assertEquals(date(2026, 10, 27, end, 0), BtmTimeUtil.getExperimenterEndDayAndHour(day, shift));
  }

  @ParameterizedTest
  @CsvSource({"2026-03-08T07:45, 2026-03-08T00:00", "2026-11-01T07:45, 2026-11-01T00:00"})
  void findsLocalShiftStartAcrossDaylightSavingChanges(String input, String expected) {
    Date date = Date.from(LocalDateTime.parse(input).atZone(ZONE).toInstant());
    Date start = Date.from(LocalDateTime.parse(expected).atZone(ZONE).toInstant());
    assertEquals(start, BtmTimeUtil.getExpShiftStart(date));
    assertEquals(start, BtmTimeUtil.getCurrentExperimenterShiftDay(date));
  }

  private static Date date(int year, int month, int day, int hour, int minute) {
    return Date.from(LocalDateTime.of(year, month, day, hour, minute).atZone(ZONE).toInstant());
  }
}
