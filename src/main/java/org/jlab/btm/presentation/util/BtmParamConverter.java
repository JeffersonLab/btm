package org.jlab.btm.presentation.util;

import jakarta.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.TemporalQuery;
import java.util.Date;
import java.util.Locale;
import org.jlab.btm.persistence.enumeration.DurationUnits;
import org.jlab.btm.persistence.enumeration.TimesheetType;
import org.jlab.smoothness.persistence.enumeration.Hall;
import org.jlab.smoothness.persistence.enumeration.Shift;

/**
 * @author ryans
 */
public final class BtmParamConverter {

  private BtmParamConverter() {
    // No one can instantiate due to private visibility
  }

  /**
   * Parses with a DateTimeFormatter, reporting failure as the checked ParseException these
   * converters declare, so callers' ParseException handlers see malformed input.
   */
  private static <T> T parse(DateTimeFormatter formatter, String valueStr, TemporalQuery<T> query)
      throws ParseException {
    try {
      return formatter.parse(valueStr, query);
    } catch (DateTimeParseException e) {
      ParseException pe = new ParseException(e.getMessage(), e.getErrorIndex());
      pe.initCause(e);
      throw pe;
    }
  }

  public static Date convertJLabDateTime(HttpServletRequest request, String name)
      throws ParseException {
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    Date value = null;

    String valueStr = request.getParameter(name);

    if (valueStr != null && !valueStr.isEmpty()) {
      final LocalDateTime ldt = parse(formatter, valueStr, LocalDateTime::from);
      final ZonedDateTime zdt = ldt.atZone(ZoneId.of("America/New_York"));
      value = Date.from(zdt.toInstant());
    }

    return value;
  }

  /**
   * Converts a date and time like 31-Jan-2026 13:45:59, in JLab's time zone; the seconds are
   * optional, and the month's case doesn't matter. A time in the hour that repeats when daylight
   * saving time ends is taken as the first of the two.
   *
   * @param request The request
   * @param name The parameter's name
   * @return The instant, or null if the parameter is missing or empty
   * @throws ParseException If the value isn't such a date and time
   */
  public static Instant convertJLabDateTimeWithSeconds(HttpServletRequest request, String name)
      throws ParseException {
    final DateTimeFormatter formatter =
        new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("dd-MMM-uuuu HH:mm[:ss]")
            .toFormatter(Locale.US)
            .withResolverStyle(ResolverStyle.STRICT);

    Instant value = null;

    String valueStr = request.getParameter(name);

    if (valueStr != null && !valueStr.isBlank()) {
      final LocalDateTime ldt = parse(formatter, valueStr.trim(), LocalDateTime::from);
      value = ldt.atZone(ZoneId.of("America/New_York")).toInstant();
    }

    return value;
  }

  public static Date convertJLabDate(HttpServletRequest request, String name)
      throws ParseException {
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    Date value = null;

    String valueStr = request.getParameter(name);

    if (valueStr != null && !valueStr.isEmpty()) {
      final LocalDate ldt = parse(formatter, valueStr, LocalDate::from);
      value = Date.from(ldt.atStartOfDay(ZoneId.of("America/New_York")).toInstant());
    }

    return value;
  }

  public static Date convertDayMonthAndYear(HttpServletRequest request, String name)
      throws ParseException {
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    Date value = null;

    String valueStr = request.getParameter(name);

    if (valueStr != null && !valueStr.isEmpty()) {
      final LocalDate ld = parse(formatter, valueStr, LocalDate::from);
      final ZonedDateTime zdt = ld.atStartOfDay(ZoneId.of("America/New_York"));
      value = Date.from(zdt.toInstant());
    }

    return value;
  }

  public static Date convertMonthAndYear(HttpServletRequest request, String name)
      throws ParseException {
    final DateTimeFormatter formatter =
        new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("MMMM-yyyy")
            .toFormatter();

    Date value = null;

    String valueStr = request.getParameter(name);

    if (valueStr != null && !valueStr.isEmpty()) {
      final YearMonth ym = parse(formatter, valueStr, YearMonth::from);
      final LocalDate ld = ym.atDay(1);
      final ZonedDateTime zdt = ld.atStartOfDay(ZoneId.of("America/New_York"));
      value = Date.from(zdt.toInstant());
    }

    return value;
  }

  public static Date convertMonthAndYear2(HttpServletRequest request, String name)
      throws ParseException {
    final DateTimeFormatter formatter =
        new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("MMMM yyyy")
            .toFormatter();

    Date value = null;

    String valueStr = request.getParameter(name);

    if (valueStr != null && !valueStr.isEmpty()) {
      final YearMonth ym = parse(formatter, valueStr, YearMonth::from);
      final LocalDate ld = ym.atDay(1);
      final ZonedDateTime zdt = ld.atStartOfDay(ZoneId.of("America/New_York"));
      value = Date.from(zdt.toInstant());
    }

    return value;
  }

  public static Date convertMonthAndYear(String valueStr) throws ParseException {
    final DateTimeFormatter formatter =
        new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("MMMM-yyyy")
            .toFormatter();

    Date value = null;

    if (valueStr != null && !valueStr.isEmpty()) {
      final YearMonth ym = parse(formatter, valueStr, YearMonth::from);
      final LocalDate ld = ym.atDay(1);
      final ZonedDateTime zdt = ld.atStartOfDay(ZoneId.of("America/New_York"));
      value = Date.from(zdt.toInstant());
    }

    return value;
  }

  public static Date convertISO8601Date(HttpServletRequest request, String name, Date defaultValue)
      throws ParseException {
    final DateTimeFormatter formatter =
        new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("dd-MMM-yyyy")
            .toFormatter();

    Date value;

    String valueStr = request.getParameter(name);

    if (valueStr != null && !valueStr.isEmpty()) {
      final LocalDate ld = parse(formatter, valueStr, LocalDate::from);
      final ZonedDateTime zdt = ld.atStartOfDay(ZoneId.of("America/New_York"));
      value = Date.from(zdt.toInstant());
    } else {
      value = defaultValue;
    }

    return value;
  }

  public static Date convertISO8601Date(String valueStr) throws ParseException {
    final DateTimeFormatter formatter =
        new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("yyyy-MM-dd")
            .toFormatter();

    Date value = null;

    if (valueStr != null && !valueStr.isEmpty()) {
      final LocalDate ld = parse(formatter, valueStr, LocalDate::from);
      final ZonedDateTime zdt = ld.atStartOfDay(ZoneId.of("America/New_York"));
      value = Date.from(zdt.toInstant());
    }

    return value;
  }

  public static Date convertDayAndHour(String valueStr) throws ParseException {
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH");

    Date value = null;

    if (valueStr != null && !valueStr.isEmpty()) {
      final LocalDateTime ldt = parse(formatter, valueStr, LocalDateTime::from);
      final ZonedDateTime zdt = ldt.atZone(ZoneId.of("America/New_York"));
      value = Date.from(zdt.toInstant());
    }

    return value;
  }

  public static Date convertDayAndHour(HttpServletRequest request, String name)
      throws ParseException {
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");

    Date value = null;

    String valueStr = request.getParameter(name);

    if (valueStr != null && !valueStr.isEmpty()) {
      final LocalDateTime ldt = parse(formatter, valueStr, LocalDateTime::from);
      final ZonedDateTime zdt = ldt.atZone(ZoneId.of("America/New_York"));
      value = Date.from(zdt.toInstant());
    }

    return value;
  }

  public static Date[] convertDayHourArray(HttpServletRequest request, String name)
      throws ParseException {
    String[] valueStrArray = request.getParameterValues(name);
    Date[] valueArray = null;
    SimpleDateFormat format =
        new SimpleDateFormat(
            "yyyy-MM-dd-HH-z"); // DateTimeFormatter does not handle EST/EDT the same so we keep old
    // code

    if (valueStrArray != null && valueStrArray.length > 0) {
      valueArray = new Date[valueStrArray.length];

      for (int i = 0; i < valueStrArray.length; i++) {
        Date value = null;

        if (valueStrArray[i] != null && !valueStrArray[i].isEmpty()) {
          value = format.parse(valueStrArray[i]);
        }

        valueArray[i] = value;
      }
    }

    return valueArray;
  }

  public static Shift convertShift(HttpServletRequest request, String name, Shift defaultValue) {
    String valueStr = request.getParameter(name);
    Shift value;

    if (valueStr == null || valueStr.isEmpty()) {
      value = defaultValue;
    } else {
      value = Shift.valueOf(valueStr.toUpperCase());
    }

    return value;
  }

  public static DurationUnits convertDurationUnits(
      HttpServletRequest request, String name, DurationUnits defaultValue) {
    String valueStr = request.getParameter(name);
    DurationUnits value;

    if (valueStr == null || valueStr.isEmpty()) {
      value = defaultValue;
    } else {
      value = DurationUnits.valueOf(valueStr.toUpperCase());
    }

    return value;
  }

  public static Hall convertHall(HttpServletRequest request, String name) {
    String valueStr = request.getParameter(name);
    Hall value = null;

    if (valueStr != null && !valueStr.isEmpty()) {
      value = Hall.valueOf(valueStr.toUpperCase());
    }

    return value;
  }

  public static TimesheetType convertTimesheetType(HttpServletRequest request, String name) {
    String valueStr = request.getParameter(name);
    TimesheetType value = null;

    if (valueStr != null && !valueStr.isEmpty()) {
      value = TimesheetType.valueOf(valueStr.toUpperCase());
    }

    return value;
  }

  public static Short convertShort(HttpServletRequest request, String name) {
    String valueStr = request.getParameter(name);
    Short value = null;

    if (valueStr != null && !valueStr.isEmpty()) {
      value = Short.valueOf(valueStr);
    }

    return value;
  }
}
