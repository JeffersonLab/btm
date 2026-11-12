package org.jlab.btm.business.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.jlab.btm.persistence.entity.ExpProgram;
import org.jlab.btm.persistence.entity.ScheduleDay;
import org.jlab.btm.persistence.enumeration.AcceleratorProgram;
import org.jlab.btm.persistence.projection.HallPriority;
import org.jlab.btm.presentation.util.BtmFunctions;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * Reads an uploaded Excel schedule into monthly lists of schedule days without touching the
 * database.
 *
 * <p>The parser is lenient where the schedule committee's spreadsheets commonly vary: program names
 * match regardless of surrounding or repeated spaces and capitalization, a month may begin after
 * day 1 (the days before it are filled in as OFF), and unreadable beam properties are ignored for a
 * hall whose program is OFF.
 */
final class ExcelScheduleParser {

  private static final Logger LOGGER = Logger.getLogger(ExcelScheduleParser.class.getName());

  private static final String OFF = "OFF";

  private static final int FIRST_DATA_ROW = 2; // zero based; first two rows are headers

  private final Map<Hall, HallPrograms> programsByHall = new EnumMap<>(Hall.class);

  /** One month of parsed schedule days, starting on the first of the month. */
  static final class ParsedMonth {
    private final Date start;
    private final List<ScheduleDay> days = new ArrayList<>();

    private ParsedMonth(Date start) {
      this.start = start;
    }

    Date getStart() {
      return start;
    }

    List<ScheduleDay> getDays() {
      return Collections.unmodifiableList(days);
    }
  }

  /**
   * @param programsByHall each hall's programs, keyed by program name
   * @throws UserFriendlyException if a hall has no program named OFF
   */
  ExcelScheduleParser(Map<Hall, Map<String, ExpProgram>> programsByHall)
      throws UserFriendlyException {
    for (Hall hall : new Hall[] {Hall.A, Hall.B, Hall.C, Hall.D}) {
      Map<String, ExpProgram> byName = programsByHall.get(hall);
      this.programsByHall.put(
          hall, new HallPrograms(hall, byName == null ? Collections.emptyMap() : byName));
    }
  }

  /**
   * Parses the schedule sheet.
   *
   * @param sheet the schedule sheet
   * @return the months in the sheet, in sheet order
   * @throws UserFriendlyException if the sheet can't be read as a schedule
   */
  List<ParsedMonth> parse(Sheet sheet) throws UserFriendlyException {
    List<ParsedMonth> months = new ArrayList<>();
    ParsedMonth month = null;
    Date previous = null;

    for (Row row : sheet) {
      if (row.getRowNum() < FIRST_DATA_ROW) {
        continue;
      }

      ScheduleDay day;

      try {
        day = parseRow(row);
      } catch (UserFriendlyException e) {
        throw new UserFriendlyException(
            "Row " + (row.getRowNum() + 1) + ": " + e.getUserMessage(), e);
      }

      if (day == null) {
        continue; // Not a schedule row
      }

      Date date = day.getDayMonthYear();

      if (previous != null && !date.after(previous)) {
        throw new UserFriendlyException(
            "Row "
                + (row.getRowNum() + 1)
                + ": dates must increase from row to row, found "
                + format(date)
                + " after "
                + format(previous));
      }

      if (month == null || !TimeUtil.isSameMonth(month.start, date)) {
        month = new ParsedMonth(TimeUtil.startOfMonth(date, Calendar.getInstance()));
        months.add(month);
        fillOffDays(month, date);
      }

      month.days.add(day);
      previous = date;
    }

    return months;
  }

  /** Adds OFF days from the start of the month up to, but not including, the given day. */
  private void fillOffDays(ParsedMonth month, Date firstScheduledDay) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(month.start);

    while (cal.getTime().before(firstScheduledDay)) {
      month.days.add(offDay(cal.getTime()));
      cal.add(Calendar.DATE, 1);
    }

    if (!month.days.isEmpty()) {
      LOGGER.log(
          Level.INFO,
          "Schedule for {0} starts on {1}; filled {2} preceding day(s) as OFF",
          new Object[] {
            new SimpleDateFormat("MMMM yyyy").format(month.start),
            format(firstScheduledDay),
            month.days.size()
          });
    }
  }

  private ScheduleDay offDay(Date date) {
    ScheduleDay day = new ScheduleDay();
    day.setDayMonthYear(date);
    day.setAccProgram(OFF);
    day.setHallAProgramId(programsByHall.get(Hall.A).offId);
    day.setHallBProgramId(programsByHall.get(Hall.B).offId);
    day.setHallCProgramId(programsByHall.get(Hall.C).offId);
    day.setHallDProgramId(programsByHall.get(Hall.D).offId);
    day.setHallAPolarized(false);
    day.setHallBPolarized(false);
    day.setHallCPolarized(false);
    day.setHallDPolarized(false);
    return day;
  }

  /**
   * @return the row's schedule day, or null if the row doesn't start with a date
   */
  private ScheduleDay parseRow(Row row) throws UserFriendlyException {
    Cell cell = row.getCell(0); // Date

    if (!isDate(cell)) {
      return null;
    }

    ScheduleDay day = new ScheduleDay();
    day.setDayMonthYear(TimeUtil.startOfDay(cell.getDateCellValue(), Calendar.getInstance()));

    // cell = row.getCell(1); // Weekday, skip
    cell = row.getCell(2); // GeV/pass

    if (cell != null && cell.getCellType() == CellType.NUMERIC) {
      day.setKiloVoltsPerPass((int) gigaToKilo(cell.getNumericCellValue()));
    }

    day.setAccProgram(parseAcceleratorProgram(row.getCell(3)));

    HallSchedule hallA = parseHall(row, 4, Hall.A, "uA");
    day.setHallAProgramId(hallA.programId);
    day.setHallAPolarized(false); // Can't be null so init false
    if (hallA.properties != null) {
      day.setHallAKiloVolts(hallA.properties.keV);
      day.setHallANanoAmps(hallA.properties.nA);
      day.setHallAPolarized(hallA.properties.polarized);
      // TODO: set MHz?
    }

    HallSchedule hallB = parseHall(row, 6, Hall.B, "nA");
    day.setHallBProgramId(hallB.programId);
    day.setHallBPolarized(false);
    if (hallB.properties != null) {
      day.setHallBKiloVolts(hallB.properties.keV);
      day.setHallBNanoAmps(hallB.properties.nA);
      day.setHallBPolarized(hallB.properties.polarized);
    }

    HallSchedule hallC = parseHall(row, 8, Hall.C, "uA");
    day.setHallCProgramId(hallC.programId);
    day.setHallCPolarized(false);
    if (hallC.properties != null) {
      day.setHallCKiloVolts(hallC.properties.keV);
      day.setHallCNanoAmps(hallC.properties.nA);
      day.setHallCPolarized(hallC.properties.polarized);
    }

    HallSchedule hallD = parseHall(row, 10, Hall.D, "nA");
    day.setHallDProgramId(hallD.programId);
    day.setHallDPolarized(false);
    if (hallD.properties != null) {
      day.setHallDKiloVolts(hallD.properties.keV);
      day.setHallDNanoAmps(hallD.properties.nA);
      day.setHallDPolarized(hallD.properties.polarized);
    }

    cell = row.getCell(12); // Priority Hall

    if (cell != null && cell.getCellType() == CellType.STRING) {
      List<HallPriority> priorityList = BtmFunctions.parsePriorityString(cell.getStringCellValue());

      for (HallPriority priority : priorityList) {
        switch (priority.getHall()) {
          case A:
            day.setHallAPriority(priority.getPriority());
            break;
          case B:
            day.setHallBPriority(priority.getPriority());
            break;
          case C:
            day.setHallCPriority(priority.getPriority());
            break;
          case D:
            day.setHallDPriority(priority.getPriority());
            break;
        }
      }
    }

    cell = row.getCell(13); // Pass

    if (cell != null && cell.getCellType() == CellType.STRING) {
      String[] tokens = cell.getStringCellValue().split("/");
      Integer[] passes = new Integer[4];

      for (int i = 0; i < 4 && i < tokens.length; i++) {
        try {
          // Hall D might read 0.5 or 5.5, so round up and GUI will know what to do.
          passes[i] = Math.round(Float.parseFloat(tokens[i].trim()));
        } catch (NumberFormatException e) {
          passes[i] = null;
        }
      }

      day.setHallAPasses(passes[0]);
      day.setHallBPasses(passes[1]);
      day.setHallCPasses(passes[2]);
      day.setHallDPasses(passes[3]);
    }

    return day;
  }

  private String parseAcceleratorProgram(Cell cell) throws UserFriendlyException {
    if (cell == null || cell.getCellType() == CellType.BLANK) {
      return OFF;
    }

    if (cell.getCellType() != CellType.STRING) {
      throw new UserFriendlyException("Accelerator Program must be text, found: " + cell);
    }

    String programStr = normalize(cell.getStringCellValue());

    if (programStr.isEmpty()) {
      return OFF;
    }

    try {
      return AcceleratorProgram.valueOf(programStr).name();
    } catch (IllegalArgumentException e) {
      throw new UserFriendlyException(
          "Unrecognized Accelerator Program, found: " + cell.getStringCellValue().trim());
    }
  }

  /**
   * Reads a hall's program and its GeV/mA/Pol/MHz properties from the two adjacent cells starting
   * at programColumn.
   */
  private HallSchedule parseHall(Row row, int programColumn, Hall hall, String currentUnits)
      throws UserFriendlyException {
    HallPrograms programs = programsByHall.get(hall);
    HallSchedule result = new HallSchedule();

    Integer programId = programs.find(stringValue(row.getCell(programColumn)));
    result.programId = programId == null ? programs.offId : programId;

    Cell propertiesCell = row.getCell(programColumn + 1);

    if (result.programId.equals(programs.offId)) {
      try {
        result.properties = parseHallProperties(propertiesCell, hall, currentUnits);
      } catch (UserFriendlyException e) {
        // An OFF hall's properties column is normally empty; ignore notes left in it.
        LOGGER.log(
            Level.INFO,
            "Ignoring Hall {0} properties of row {1} because its program is OFF: {2}",
            new Object[] {hall, row.getRowNum() + 1, e.getUserMessage()});
        result.properties = null;
      }
    } else {
      result.properties = parseHallProperties(propertiesCell, hall, currentUnits);
    }

    return result;
  }

  /** True for a date cell, including a formula such as =A3+1 that results in a date. */
  private static boolean isDate(Cell cell) {
    if (cell == null) {
      return false;
    }

    CellType type = cell.getCellType();

    if (type == CellType.FORMULA) {
      type = cell.getCachedFormulaResultType();
    }

    return type == CellType.NUMERIC
        && DateUtil.isCellDateFormatted(cell)
        && cell.getDateCellValue() != null;
  }

  private static String stringValue(Cell cell) {
    if (cell != null && cell.getCellType() == CellType.STRING) {
      return cell.getStringCellValue();
    }

    return null;
  }

  private HallProperties parseHallProperties(Cell cell, Hall hall, String currentUnits)
      throws UserFriendlyException {
    String hallProperties = stringValue(cell);

    if (hallProperties == null || hallProperties.trim().isEmpty()) {
      return null;
    }

    String[] tokens = hallProperties.split("/");

    if (tokens.length != 4) {
      throw new UserFriendlyException(
          "Hall "
              + hall
              + " Properties must be formated 'GeV/mA/Pol/MHz', found: "
              + hallProperties);
    }

    for (int i = 0; i < tokens.length; i++) {
      tokens[i] = tokens[i].trim();
    }

    HallProperties properties = new HallProperties();

    double geV;
    double current;

    try {
      geV = Double.parseDouble(tokens[0]);
    } catch (NumberFormatException e) {
      throw new UserFriendlyException(
          "Hall " + hall + " GeV must be a number, found: " + tokens[0]);
    }
    properties.keV = ((int) gigaToKilo(geV));

    try {
      current = Double.parseDouble(tokens[1]);
    } catch (NumberFormatException e) {
      throw new UserFriendlyException(
          "Hall " + hall + " current must be a number, found: " + tokens[1]);
    }
    if (currentUnits.equals("uA")) {
      properties.nA = (int) microToNano(current);
    } else {
      properties.nA = (int) current;
    }

    if ("p".equalsIgnoreCase(tokens[2])) {
      properties.polarized = true;
    } else if ("-".equals(tokens[2])) {
      properties.polarized = false;
    } else {
      throw new UserFriendlyException(
          "Hall " + hall + " Pol must be one of 'p,-', found: " + tokens[2]);
    }

    try {
      properties.mHz = Integer.parseInt(tokens[3]);
    } catch (NumberFormatException e) {
      throw new UserFriendlyException(
          "Hall " + hall + " MHz must be a number, found: " + tokens[3]);
    }

    return properties;
  }

  /** Trims, collapses inner whitespace and upper-cases a name so that variants compare equal. */
  static String normalize(String name) {
    return name.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
  }

  private static String format(Date date) {
    return new SimpleDateFormat("dd MMM yyyy").format(date);
  }

  private static double gigaToKilo(double giga) {
    return giga * 1000000.0;
  }

  private static double microToNano(double micro) {
    return micro * 1000.0;
  }

  /** A hall's programs, looked up by exact or normalized name. */
  private static final class HallPrograms {
    private final Hall hall;
    private final Map<String, ExpProgram> byName;
    private final Map<String, List<ExpProgram>> byNormalizedName = new HashMap<>();
    private final Integer offId;

    private HallPrograms(Hall hall, Map<String, ExpProgram> byName) throws UserFriendlyException {
      this.hall = hall;
      this.byName = byName;

      for (ExpProgram program : byName.values()) {
        byNormalizedName
            .computeIfAbsent(normalize(program.getName()), k -> new ArrayList<>())
            .add(program);
      }

      ExpProgram off = byName.get(OFF);

      if (off == null) {
        List<ExpProgram> offs = byNormalizedName.get(OFF);
        if (offs != null && offs.size() == 1) {
          off = offs.get(0);
        }
      }

      if (off == null) {
        throw new UserFriendlyException(
            "Hall " + hall + " must have a program named 'OFF', please update program list");
      }

      offId = off.getExpProgramId().intValue();
    }

    /**
     * @return the program's ID, or null if the name is null or blank
     * @throws UserFriendlyException if no program, or more than one, matches the name
     */
    private Integer find(String name) throws UserFriendlyException {
      if (name == null || name.trim().isEmpty()) {
        return null;
      }

      ExpProgram program = byName.get(name);

      if (program == null) {
        program = byName.get(name.trim());
      }

      if (program == null) {
        List<ExpProgram> matches = byNormalizedName.get(normalize(name));

        if (matches == null) {
          throw new UserFriendlyException(
              "Could not find hall "
                  + hall
                  + " program: '"
                  + name.trim()
                  + "', please update Program list");
        }

        if (matches.size() > 1) {
          List<String> names = new ArrayList<>();
          for (ExpProgram match : matches) {
            names.add("'" + match.getName() + "'");
          }
          Collections.sort(names);
          throw new UserFriendlyException(
              "Hall "
                  + hall
                  + " program '"
                  + name.trim()
                  + "' matches more than one program ("
                  + String.join(", ", names)
                  + "); use the exact name");
        }

        program = matches.get(0);
      }

      return program.getExpProgramId().intValue();
    }
  }

  private static final class HallSchedule {
    private Integer programId;
    private HallProperties properties;
  }

  private static final class HallProperties {
    private int keV;
    private int nA;
    private boolean polarized;
    private int mHz;
  }
}
