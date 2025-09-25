package org.jlab.btm.business.service.excel;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jlab.btm.persistence.projection.CcAccSum;
import org.jlab.btm.persistence.projection.PacAccSum;
import org.jlab.btm.persistence.projection.PdAccSum;
import org.jlab.smoothness.business.util.TimeUtil;

/**
 * @author ryans
 */
@Stateless
public class ExcelBeamSummaryService {

  @PermitAll
  public void export(
      OutputStream out, CcAccSum ccSum, PdAccSum pdSum, PacAccSum pacSum, String filters)
      throws IOException {
    Workbook wb = new XSSFWorkbook();
    Sheet sheet1 = wb.createSheet("BTM Beam Summary");

    int rownum = 0;
    Row row0 = sheet1.createRow(rownum++);
    // row0.createCell(0).setCellValue("Bounded By: " + filters);

    Row row1 = sheet1.createRow(rownum++);
    row1.createCell(0).setCellValue("PROGRAM");
    row1.createCell(1).setCellValue("CC HOURS");
    row1.createCell(2).setCellValue("PD HOURS");
    row1.createCell(3).setCellValue("NPES HOURS");

    CreationHelper createHelper = wb.getCreationHelper();
    CellStyle numberStyle = wb.createCellStyle();
    numberStyle.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.0"));
    CellStyle dateStyle = wb.createCellStyle();
    dateStyle.setDataFormat(
        createHelper.createDataFormat().getFormat(TimeUtil.getFriendlyDateTimePattern()));

    Cell c;

    // Physics Program
    Row row = sheet1.createRow(rownum++);
    row.createCell(0).setCellValue("PHYSICS");
    c = row.createCell(1);
    c.setCellStyle(numberStyle);
    c.setCellValue(ccSum.getUpSeconds() / 3600);
    c = row.createCell(2);
    c.setCellStyle(numberStyle);
    c.setCellValue(pdSum.getPhysicsSeconds() / 3600);
    c = row.createCell(3);
    c.setCellStyle(numberStyle);
    c.setCellValue(pacSum.getPhysicsDays() * 24);

    // Studies Program
    row = sheet1.createRow(rownum++);
    row.createCell(0).setCellValue("STUDIES");
    c = row.createCell(1);
    c.setCellStyle(numberStyle);
    c.setCellValue(ccSum.getStudiesSeconds() / 3600);
    c = row.createCell(2);
    c.setCellStyle(numberStyle);
    c.setCellValue(pdSum.getStudiesSeconds() / 3600);
    c = row.createCell(3);
    c.setCellStyle(numberStyle);
    c.setCellValue(pacSum.getStudiesDays() * 24);

    // Restore Program
    row = sheet1.createRow(rownum++);
    row.createCell(0).setCellValue("RESTORE");
    c = row.createCell(1);
    c.setCellStyle(numberStyle);
    c.setCellValue(ccSum.getRestoreSeconds() / 3600);
    c = row.createCell(2);
    c.setCellStyle(numberStyle);
    c.setCellValue(pdSum.getRestoreSeconds() / 3600);
    c = row.createCell(3);
    c.setCellStyle(numberStyle);
    c.setCellValue(pacSum.getRestoreDays() * 24);

    // ACC Program
    row = sheet1.createRow(rownum++);
    row.createCell(0).setCellValue("ACC");
    c = row.createCell(1);
    c.setCellStyle(numberStyle);
    c.setCellValue(ccSum.getAccSeconds() / 3600);
    c = row.createCell(2);
    c.setCellStyle(numberStyle);
    c.setCellValue(pdSum.getAccSeconds() / 3600);
    c = row.createCell(3);
    c.setCellStyle(numberStyle);
    c.setCellValue(pacSum.getAccDays() * 24);

    // Down Program
    row = sheet1.createRow(rownum++);
    row.createCell(0).setCellValue("DOWN");
    c = row.createCell(1);
    c.setCellStyle(numberStyle);
    c.setCellValue(ccSum.getDownSeconds() / 3600);
    c = row.createCell(2);
    c.setCellStyle(numberStyle);
    // c.setCellValue();
    c = row.createCell(3);
    c.setCellStyle(numberStyle);
    // c.setCellValue();

    // Total Program Time
    row = sheet1.createRow(rownum++);
    row.createCell(0).setCellValue("Total Program");
    c = row.createCell(1);
    c.setCellStyle(numberStyle);
    c.setCellValue(ccSum.getProgramSeconds() / 3600);
    c = row.createCell(2);
    c.setCellStyle(numberStyle);
    c.setCellValue(pdSum.getProgramSeconds() / 3600);
    c = row.createCell(3);
    c.setCellStyle(numberStyle);
    c.setCellValue(pacSum.getProgramDays() * 24);

    // Period
    row = sheet1.createRow(rownum++);
    row.createCell(0).setCellValue("Period");
    c = row.createCell(1);
    c.setCellStyle(numberStyle);
    c.setCellValue(ccSum.getPeriodHours());
    c = row.createCell(2);
    c.setCellStyle(numberStyle);
    c.setCellValue(pdSum.getPeriodHours());
    c = row.createCell(3);
    c.setCellStyle(numberStyle);
    c.setCellValue(pacSum.getPeriodHours());

    // Explicit OFF
    row = sheet1.createRow(rownum++);
    row.createCell(0).setCellValue("Explicit Off (SAM)");
    c = row.createCell(1);
    c.setCellStyle(numberStyle);
    c.setCellValue(ccSum.getSadSeconds() / 3600);
    c = row.createCell(2);
    c.setCellStyle(numberStyle);
    c.setCellValue(pdSum.getOffSeconds() / 3600);
    c = row.createCell(3);
    c.setCellStyle(numberStyle);
    c.setCellValue(pacSum.getOffDays() * 24);

    // Implicit OFF
    row = sheet1.createRow(rownum++);
    row.createCell(0).setCellValue("Implicit Off");
    c = row.createCell(1);
    c.setCellStyle(numberStyle);
    c.setCellValue(ccSum.getImplicitOffHours());
    c = row.createCell(2);
    c.setCellStyle(numberStyle);
    c.setCellValue(pdSum.getImplicitOffHours());
    c = row.createCell(3);
    c.setCellStyle(numberStyle);
    c.setCellValue(pacSum.getImplicitOffHours());

    // Total OFF
    row = sheet1.createRow(rownum++);
    row.createCell(0).setCellValue("Total Off");
    c = row.createCell(1);
    c.setCellStyle(numberStyle);
    c.setCellValue(ccSum.getTotalOffHours());
    c = row.createCell(2);
    c.setCellStyle(numberStyle);
    c.setCellValue(pdSum.getTotalOffHours());
    c = row.createCell(3);
    c.setCellStyle(numberStyle);
    c.setCellValue(pacSum.getTotalOffHours());

    sheet1.autoSizeColumn(0);
    row0.createCell(0).setCellValue("Bounded By: " + filters);

    sheet1.autoSizeColumn(1);
    sheet1.autoSizeColumn(2);
    sheet1.autoSizeColumn(3);

    wb.write(out);
  }
}
