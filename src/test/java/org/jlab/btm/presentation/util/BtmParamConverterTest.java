package org.jlab.btm.presentation.util;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Proxy;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BtmParamConverterTest {
  @Test
  void convertsIso8601Date() throws ParseException {
    assertEquals(date(2026, 1, 2, 0), BtmParamConverter.convertISO8601Date("2026-01-02"));
  }

  @Test
  void convertsDayAndHour() throws ParseException {
    assertEquals(date(2026, 1, 2, 7), BtmParamConverter.convertDayAndHour("2026-01-02T07"));
  }

  @Test
  void convertsMonthAndYearIgnoringCase() throws ParseException {
    assertEquals(date(2026, 3, 1, 0), BtmParamConverter.convertMonthAndYear("march-2026"));
  }

  @Test
  void convertsMissingStringDatesToNull() throws ParseException {
    assertNull(BtmParamConverter.convertISO8601Date((String) null));
    assertNull(BtmParamConverter.convertISO8601Date(""));
    assertNull(BtmParamConverter.convertDayAndHour((String) null));
    assertNull(BtmParamConverter.convertDayAndHour(""));
    assertNull(BtmParamConverter.convertMonthAndYear((String) null));
    assertNull(BtmParamConverter.convertMonthAndYear(""));
  }

  @ParameterizedTest
  @ValueSource(strings = {"not-a-date", "2026-13-01", "01-Jan-2026"})
  void rejectsMalformedIso8601DateWithParseException(String value) {
    assertWrapsDateTimeParseException(() -> BtmParamConverter.convertISO8601Date(value));
  }

  @ParameterizedTest
  @ValueSource(strings = {"not-a-date", "2026-01-02", "2026-01-02T7", "2026-01-02 07"})
  void rejectsMalformedDayAndHourWithParseException(String value) {
    assertWrapsDateTimeParseException(() -> BtmParamConverter.convertDayAndHour(value));
  }

  @ParameterizedTest
  @ValueSource(strings = {"not-a-date", "Smarch-2026", "2026-03", "March 2026"})
  void rejectsMalformedMonthAndYearWithParseException(String value) {
    assertWrapsDateTimeParseException(() -> BtmParamConverter.convertMonthAndYear(value));
  }

  @Test
  void rejectsMalformedRequestParametersWithParseException() {
    HttpServletRequest request =
        request(
            Map.of(
                "date", "not-a-date",
                "datetime", "not-a-date",
                "day-hour", "not-a-date",
                "month", "not-a-date"));

    assertWrapsDateTimeParseException(
        () -> BtmParamConverter.convertISO8601Date(request, "date", null));
    assertWrapsDateTimeParseException(() -> BtmParamConverter.convertJLabDate(request, "date"));
    assertWrapsDateTimeParseException(
        () -> BtmParamConverter.convertDayMonthAndYear(request, "date"));
    assertWrapsDateTimeParseException(
        () -> BtmParamConverter.convertJLabDateTime(request, "datetime"));
    assertWrapsDateTimeParseException(
        () -> BtmParamConverter.convertDayAndHour(request, "day-hour"));
    assertWrapsDateTimeParseException(
        () -> BtmParamConverter.convertMonthAndYear(request, "month"));
    assertWrapsDateTimeParseException(
        () -> BtmParamConverter.convertMonthAndYear2(request, "month"));
  }

  @Test
  void convertsValidRequestParametersAndDefaults() throws ParseException {
    Date fallback = new Date(0);
    HttpServletRequest request =
        request(
            Map.of(
                "date", "02-jan-2026",
                "datetime", "02-Jan-2026 07:30",
                "day-hour", "2026-01-02 15",
                "month", "March 2026"));

    assertEquals(
        date(2026, 1, 2, 0), BtmParamConverter.convertISO8601Date(request, "date", fallback));
    assertSame(fallback, BtmParamConverter.convertISO8601Date(request, "missing", fallback));
    assertEquals(
        Date.from(
            LocalDateTime.of(2026, 1, 2, 7, 30).atZone(ZoneId.of("America/New_York")).toInstant()),
        BtmParamConverter.convertJLabDateTime(request, "datetime"));
    assertEquals(date(2026, 1, 2, 15), BtmParamConverter.convertDayAndHour(request, "day-hour"));
    assertEquals(date(2026, 3, 1, 0), BtmParamConverter.convertMonthAndYear2(request, "month"));
  }

  private interface Conversion {
    Object convert() throws ParseException;
  }

  private static void assertWrapsDateTimeParseException(Conversion conversion) {
    ParseException e = assertThrows(ParseException.class, conversion::convert);
    assertInstanceOf(DateTimeParseException.class, e.getCause());
  }

  private static Date date(int year, int month, int day, int hour) {
    return Date.from(
        LocalDateTime.of(year, month, day, hour, 0)
            .atZone(ZoneId.of("America/New_York"))
            .toInstant());
  }

  private static HttpServletRequest request(Map<String, String> parameters) {
    return (HttpServletRequest)
        Proxy.newProxyInstance(
            BtmParamConverterTest.class.getClassLoader(),
            new Class<?>[] {HttpServletRequest.class},
            (proxy, method, args) -> {
              if (method.getName().equals("getParameter")) {
                return parameters.get((String) args[0]);
              }
              throw new UnsupportedOperationException(method.getName());
            });
  }
}
