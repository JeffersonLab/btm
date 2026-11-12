package org.jlab.btm.business.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jlab.btm.persistence.entity.ExpProgram;
import org.jlab.btm.persistence.entity.ScheduleDay;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.persistence.enumeration.Hall;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExcelScheduleParserTest {

  // Program IDs: OFF is 1x, Physics 2x, Commissioning 3x, where x is the hall's number
  private static final int A_OFF = 11;
  private static final int A_PHYSICS = 21;
  private static final int A_COMMISSIONING = 31;
  private static final int B_OFF = 12;
  private static final int C_OFF = 13;
  private static final int D_OFF = 14;
  private static final int D_PHYSICS = 24;

  private Workbook wb;
  private Sheet sheet;
  private CellStyle dateStyle;

  @BeforeEach
  void createSheet() {
    wb = new XSSFWorkbook();
    sheet = wb.createSheet("Schedule");
    sheet.createRow(0).createCell(0).setCellValue("Schedule");
    sheet.createRow(1).createCell(0).setCellValue("Date");
    dateStyle = wb.createCellStyle();
    dateStyle.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("dd MMM yyyy"));
  }

  @AfterEach
  void closeWorkbook() throws IOException {
    wb.close();
  }

  private static Map<Hall, Map<String, ExpProgram>> programs() {
    Map<Hall, Map<String, ExpProgram>> byHall = new EnumMap<>(Hall.class);
    int hallNumber = 1;
    for (Hall hall : new Hall[] {Hall.A, Hall.B, Hall.C, Hall.D}) {
      Map<String, ExpProgram> byName = new HashMap<>();
      add(byName, hall, 10 + hallNumber, "OFF");
      add(byName, hall, 20 + hallNumber, "Physics");
      add(byName, hall, 30 + hallNumber, "Commissioning");
      byHall.put(hall, byName);
      hallNumber++;
    }
    return byHall;
  }

  private static void add(Map<String, ExpProgram> byName, Hall hall, int id, String name) {
    ExpProgram program = new ExpProgram(BigInteger.valueOf(id));
    program.setHall(hall);
    program.setName(name);
    byName.put(name, program);
  }

  private static Date date(int year, int month, int day) {
    return Date.from(
        LocalDate.of(year, month, day).atStartOfDay(ZoneId.systemDefault()).toInstant());
  }

  /**
   * Adds a schedule row: date, accelerator program, then each hall's program and properties (A, B,
   * C, D); a null value leaves the cell out.
   */
  private Row row(Date date, String accProgram, String... hallCells) {
    Row row = sheet.createRow(sheet.getLastRowNum() + 1);
    Cell cell = row.createCell(0);
    cell.setCellValue(date);
    cell.setCellStyle(dateStyle);
    if (accProgram != null) {
      row.createCell(3).setCellValue(accProgram);
    }
    for (int i = 0; i < hallCells.length; i++) {
      if (hallCells[i] != null) {
        row.createCell(4 + i).setCellValue(hallCells[i]);
      }
    }
    return row;
  }

  private List<ExcelScheduleParser.ParsedMonth> parse() throws UserFriendlyException {
    return new ExcelScheduleParser(programs()).parse(sheet);
  }

  private static ScheduleDay onlyDay(List<ExcelScheduleParser.ParsedMonth> months) {
    assertEquals(1, months.size());
    assertEquals(1, months.get(0).getDays().size());
    return months.get(0).getDays().get(0);
  }

  @Test
  void matchesProgramNamesIgnoringSpacesAndCase() throws UserFriendlyException {
    row(
        date(2026, 3, 1),
        "  physics ",
        " PHYSICS  ",
        null,
        "off",
        null,
        null,
        null,
        "Physics",
        "11.0/150/-/250");

    ScheduleDay day = onlyDay(parse());

    assertEquals("PHYSICS", day.getAccProgram());
    assertEquals(A_PHYSICS, day.getHallAProgramId());
    assertEquals(B_OFF, day.getHallBProgramId());
    assertEquals(C_OFF, day.getHallCProgramId());
    assertEquals(D_PHYSICS, day.getHallDProgramId());
    assertEquals(11000000, day.getHallDKiloVolts());
    assertEquals(150, day.getHallDNanoAmps());
  }

  @Test
  void collapsesRepeatedInnerSpaces() throws UserFriendlyException {
    Map<Hall, Map<String, ExpProgram>> byHall = programs();
    add(byHall.get(Hall.A), Hall.A, 41, "Facility Development");
    row(date(2026, 3, 1), "OFF", "facility   development");

    ScheduleDay day = onlyDay(new ExcelScheduleParser(byHall).parse(sheet));

    assertEquals(41, day.getHallAProgramId());
  }

  @Test
  void blankProgramsAreOff() throws UserFriendlyException {
    row(date(2026, 3, 1), "   ", "  ");

    ScheduleDay day = onlyDay(parse());

    assertEquals("OFF", day.getAccProgram());
    assertEquals(A_OFF, day.getHallAProgramId());
  }

  @Test
  void exactNameWinsOverCaseInsensitiveMatches() throws UserFriendlyException {
    Map<Hall, Map<String, ExpProgram>> byHall = programs();
    add(byHall.get(Hall.A), Hall.A, 41, "PHYSICS");
    row(date(2026, 3, 1), "PHYSICS", "PHYSICS");
    row(date(2026, 3, 2), "PHYSICS", "Physics");

    List<ScheduleDay> days = new ExcelScheduleParser(byHall).parse(sheet).get(0).getDays();

    assertEquals(41, days.get(0).getHallAProgramId());
    assertEquals(A_PHYSICS, days.get(1).getHallAProgramId());
  }

  @Test
  void rejectsAmbiguousNames() {
    Map<Hall, Map<String, ExpProgram>> byHall = programs();
    add(byHall.get(Hall.A), Hall.A, 41, "PHYSICS");
    row(date(2026, 3, 1), "PHYSICS", "physics");

    UserFriendlyException e =
        assertThrows(
            UserFriendlyException.class, () -> new ExcelScheduleParser(byHall).parse(sheet));

    assertEquals(
        "Row 3: Hall A program 'physics' matches more than one program ('PHYSICS', 'Physics');"
            + " use the exact name",
        e.getUserMessage());
  }

  @Test
  void rejectsUnknownProgramWithItsRow() {
    row(date(2026, 3, 1), "PHYSICS", "Physics");
    row(date(2026, 3, 2), "PHYSICS", "Installation");

    UserFriendlyException e = assertThrows(UserFriendlyException.class, this::parse);

    assertEquals(
        "Row 4: Could not find hall A program: 'Installation', please update Program list",
        e.getUserMessage());
  }

  @Test
  void rejectsUnknownAcceleratorProgram() {
    row(date(2026, 3, 1), "Beam");

    UserFriendlyException e = assertThrows(UserFriendlyException.class, this::parse);

    assertEquals("Row 3: Unrecognized Accelerator Program, found: Beam", e.getUserMessage());
  }

  @Test
  void requiresAnOffProgramInEveryHall() {
    Map<Hall, Map<String, ExpProgram>> byHall = programs();
    byHall.get(Hall.C).remove("OFF");

    UserFriendlyException e =
        assertThrows(UserFriendlyException.class, () -> new ExcelScheduleParser(byHall));

    assertEquals(
        "Hall C must have a program named 'OFF', please update program list", e.getUserMessage());
  }

  @Test
  void fillsDaysBeforeAMonthsFirstRowAsOff() throws UserFriendlyException {
    row(date(2026, 4, 3), "PHYSICS", "Commissioning", "2.2/5/p/499");
    row(date(2026, 4, 4), "PHYSICS", "Commissioning", "2.2/5/p/499");

    List<ExcelScheduleParser.ParsedMonth> months = parse();

    assertEquals(1, months.size());
    assertEquals(date(2026, 4, 1), months.get(0).getStart());
    List<ScheduleDay> days = months.get(0).getDays();
    assertEquals(4, days.size());
    for (int i = 0; i < 2; i++) {
      ScheduleDay off = days.get(i);
      assertEquals(date(2026, 4, 1 + i), off.getDayMonthYear());
      assertEquals("OFF", off.getAccProgram());
      assertEquals(A_OFF, off.getHallAProgramId());
      assertEquals(B_OFF, off.getHallBProgramId());
      assertEquals(C_OFF, off.getHallCProgramId());
      assertEquals(D_OFF, off.getHallDProgramId());
      assertFalse(off.getHallAPolarized());
      assertNull(off.getHallAKiloVolts());
    }
    assertEquals(date(2026, 4, 3), days.get(2).getDayMonthYear());
    assertEquals(A_COMMISSIONING, days.get(2).getHallAProgramId());
    assertEquals(2200000, days.get(2).getHallAKiloVolts());
    assertEquals(5000, days.get(2).getHallANanoAmps());
    assertTrue(days.get(2).getHallAPolarized());
  }

  @Test
  void startsANewMonthWhenTheMonthChanges() throws UserFriendlyException {
    row(date(2026, 4, 30), "OFF");
    row(date(2026, 5, 2), "OFF");

    List<ExcelScheduleParser.ParsedMonth> months = parse();

    assertEquals(2, months.size());
    assertEquals(date(2026, 4, 1), months.get(0).getStart());
    assertEquals(30, months.get(0).getDays().size());
    assertEquals(date(2026, 5, 1), months.get(1).getStart());
    assertEquals(2, months.get(1).getDays().size());
  }

  @Test
  void rejectsDatesOutOfOrder() {
    row(date(2026, 5, 1), "OFF");
    row(date(2026, 4, 30), "OFF");

    UserFriendlyException e = assertThrows(UserFriendlyException.class, this::parse);

    assertEquals(
        "Row 4: dates must increase from row to row, found 30 Apr 2026 after 01 May 2026",
        e.getUserMessage());
  }

  @Test
  void readsFormulaDates() throws UserFriendlyException {
    row(date(2026, 6, 1), "OFF");
    Row second = sheet.createRow(3);
    Cell cell = second.createCell(0);
    cell.setCellFormula("A3+1");
    cell.setCellStyle(dateStyle);
    wb.getCreationHelper().createFormulaEvaluator().evaluateAll();

    List<ScheduleDay> days = parse().get(0).getDays();

    assertEquals(2, days.size());
    assertEquals(date(2026, 6, 2), days.get(1).getDayMonthYear());
  }

  @Test
  void ignoresUnreadablePropertiesOfAnOffHall() throws UserFriendlyException {
    row(date(2026, 3, 1), "PHYSICS", "OFF", "beam studies TBD", null, "see notes");

    ScheduleDay day = onlyDay(parse());

    assertEquals(A_OFF, day.getHallAProgramId());
    assertNull(day.getHallAKiloVolts());
    assertNull(day.getHallANanoAmps());
    assertFalse(day.getHallAPolarized());
    assertEquals(B_OFF, day.getHallBProgramId());
    assertNull(day.getHallBKiloVolts());
  }

  @Test
  void keepsReadablePropertiesOfAnOffHall() throws UserFriendlyException {
    row(date(2026, 3, 1), "PHYSICS", "OFF", "4.4/2/p/249");

    ScheduleDay day = onlyDay(parse());

    assertEquals(4400000, day.getHallAKiloVolts());
    assertEquals(2000, day.getHallANanoAmps());
    assertTrue(day.getHallAPolarized());
  }

  @Test
  void rejectsUnreadablePropertiesOfAScheduledHall() {
    row(date(2026, 3, 1), "PHYSICS", "Physics", "beam studies TBD");

    UserFriendlyException e = assertThrows(UserFriendlyException.class, this::parse);

    assertEquals(
        "Row 3: Hall A Properties must be formated 'GeV/mA/Pol/MHz', found: beam studies TBD",
        e.getUserMessage());
  }

  @Test
  void readsPassesAndPriority() throws UserFriendlyException {
    Row row = row(date(2026, 3, 1), "PHYSICS");
    row.createCell(12).setCellValue("A>C");
    row.createCell(13).setCellValue("5/ 2/5/5.5");

    ScheduleDay day = onlyDay(parse());

    assertEquals(5, day.getHallAPasses());
    assertEquals(2, day.getHallBPasses());
    assertEquals(5, day.getHallCPasses());
    assertEquals(6, day.getHallDPasses());
    assertEquals(0, day.getHallAPriority());
    assertNull(day.getHallBPriority());
    assertEquals(2, day.getHallCPriority());
  }

  @Test
  void skipsRowsWithoutADate() throws UserFriendlyException {
    sheet.createRow(2).createCell(0).setCellValue("Notes for the month");
    row(date(2026, 3, 1), "OFF");

    assertEquals(date(2026, 3, 1), onlyDay(parse()).getDayMonthYear());
  }
}
