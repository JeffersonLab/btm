package org.jlab.btm.business.service.excel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jlab.btm.persistence.projection.CcAccSum;
import org.jlab.btm.persistence.projection.PacAccSum;
import org.jlab.btm.persistence.projection.PdAccSum;
import org.junit.jupiter.api.Test;

class ExcelBeamSummaryServiceTest {
  private static final double DELTA = 1e-9;
  private static final int CC = 1;
  private static final int PD = 2;

  // Row indexes below the "Bounded By" and header rows
  private static final int PHYSICS = 2;
  private static final int STUDIES = 3;
  private static final int RESTORE = 4;
  private static final int ACC = 5;
  private static final int DOWN = 6;
  private static final int TOTAL_PROGRAM = 7;
  private static final int PERIOD = 8;
  private static final int EXPLICIT_OFF = 9;
  private static final int IMPLICIT_OFF = 10;
  private static final int TOTAL_OFF = 11;

  @Test
  void halfHourOfPhysicsIsNotTruncated() throws IOException {
    Date start = new Date(0);
    Date end = hoursAfter(start, 1);
    CcAccSum cc = new CcAccSum(start, end, 1800, 1800, 0, 0, 0, 0);
    PdAccSum pd = new PdAccSum(start, end, 1800, 1800, 0, 0, 0);

    Sheet sheet = export(start, end, cc, pd);

    assertEquals(0.5, value(sheet, PHYSICS, CC), DELTA);
    assertEquals(0.5, value(sheet, PHYSICS, PD), DELTA);
    assertEquals(0.5, value(sheet, TOTAL_PROGRAM, CC), DELTA);
    assertEquals(0.5, value(sheet, TOTAL_PROGRAM, PD), DELTA);
    assertEquals(0.5, value(sheet, EXPLICIT_OFF, CC), DELTA);
    assertEquals(0.5, value(sheet, EXPLICIT_OFF, PD), DELTA);
    assertEquals(1, value(sheet, PERIOD, CC), DELTA);
    assertEquals(0, value(sheet, IMPLICIT_OFF, CC), DELTA);
    assertEquals(0.5, value(sheet, TOTAL_OFF, CC), DELTA);
  }

  @Test
  void everySecondsBasedCellKeepsItsFraction() throws IOException {
    Date start = new Date(0);
    Date end = hoursAfter(start, 10);
    // up 1.5, SAD 0.25, down 0.5, studies 0.75, restore 1.25, ACC 2.5 hours
    CcAccSum cc = new CcAccSum(start, end, 5400, 900, 1800, 2700, 4500, 9000);
    // physics 0.5, OFF 1.5, studies 0.25, restore 0.75, ACC 1.75 hours
    PdAccSum pd = new PdAccSum(start, end, 1800, 5400, 900, 2700, 6300);

    Sheet sheet = export(start, end, cc, pd);

    assertEquals(1.5, value(sheet, PHYSICS, CC), DELTA);
    assertEquals(0.75, value(sheet, STUDIES, CC), DELTA);
    assertEquals(1.25, value(sheet, RESTORE, CC), DELTA);
    assertEquals(2.5, value(sheet, ACC, CC), DELTA);
    assertEquals(0.5, value(sheet, DOWN, CC), DELTA);
    assertEquals(6.5, value(sheet, TOTAL_PROGRAM, CC), DELTA);
    assertEquals(0.25, value(sheet, EXPLICIT_OFF, CC), DELTA);
    assertEquals(3.25, value(sheet, IMPLICIT_OFF, CC), DELTA);
    assertEquals(3.5, value(sheet, TOTAL_OFF, CC), DELTA);

    assertEquals(0.5, value(sheet, PHYSICS, PD), DELTA);
    assertEquals(0.25, value(sheet, STUDIES, PD), DELTA);
    assertEquals(0.75, value(sheet, RESTORE, PD), DELTA);
    assertEquals(1.75, value(sheet, ACC, PD), DELTA);
    assertEquals(3.25, value(sheet, TOTAL_PROGRAM, PD), DELTA);
    assertEquals(1.5, value(sheet, EXPLICIT_OFF, PD), DELTA);
    assertEquals(5.25, value(sheet, IMPLICIT_OFF, PD), DELTA);
    assertEquals(6.75, value(sheet, TOTAL_OFF, PD), DELTA);
  }

  @Test
  void wholeHoursAndZeroKeepTheirValues() throws IOException {
    Date start = new Date(0);
    Date end = hoursAfter(start, 4);
    CcAccSum cc = new CcAccSum(start, end, 7200, 3600, 0, 0, 0, 0);
    PdAccSum pd = new PdAccSum(start, end, 7200, 0, 0, 0, 0);

    Sheet sheet = export(start, end, cc, pd);

    assertEquals(2, value(sheet, PHYSICS, CC), DELTA);
    assertEquals(2, value(sheet, PHYSICS, PD), DELTA);
    assertEquals(0, value(sheet, STUDIES, CC), DELTA);
    assertEquals(0, value(sheet, STUDIES, PD), DELTA);
    assertEquals(0, value(sheet, DOWN, CC), DELTA);
    assertEquals(2, value(sheet, TOTAL_PROGRAM, CC), DELTA);
    assertEquals(1, value(sheet, EXPLICIT_OFF, CC), DELTA);
    assertEquals(0, value(sheet, EXPLICIT_OFF, PD), DELTA);
    // PD has no DOWN program, so its cell stays blank
    assertEquals(CellType.BLANK, sheet.getRow(DOWN).getCell(PD).getCellType());
  }

  private static Sheet export(Date start, Date end, CcAccSum cc, PdAccSum pd) throws IOException {
    PacAccSum pac = new PacAccSum();
    pac.setRangeAndCalculateOff(start, end);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    new ExcelBeamSummaryService().export(out, cc, pd, pac, "test");

    Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
    return wb.getSheetAt(0);
  }

  private static double value(Sheet sheet, int rowIndex, int column) {
    Row row = sheet.getRow(rowIndex);
    return row.getCell(column).getNumericCellValue();
  }

  private static Date hoursAfter(Date start, int hours) {
    return new Date(start.getTime() + hours * 3_600_000L);
  }
}
