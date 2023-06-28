package org.jlab.btm.business.service.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jlab.btm.persistence.projection.CcAccSum;
import org.jlab.btm.persistence.projection.PacAccSum;
import org.jlab.btm.persistence.projection.PdAccSum;
import org.jlab.smoothness.business.util.TimeUtil;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;

/**
 *
 * @author ryans
 */
@Stateless
public class ExcelBeamSummaryService {

    @PermitAll
    public void export(OutputStream out, CcAccSum ccSum, PdAccSum pdSum, PacAccSum pacSum,
                       String filters) throws
            IOException {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet1 = wb.createSheet("BTM Beam Summary");

        int rownum = 0;
        Row row0 = sheet1.createRow(rownum++);
        //row0.createCell(0).setCellValue("Bounded By: " + filters);

        Row row1 = sheet1.createRow(rownum++);
        row1.createCell(0).setCellValue("PROGRAM");
        row1.createCell(1).setCellValue("CC HOURS");
        row1.createCell(2).setCellValue("PD HOURS");
        row1.createCell(3).setCellValue("PAC HOURS");

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

        sheet1.autoSizeColumn(0);
        row0.createCell(0).setCellValue("Bounded By: " + filters);

        sheet1.autoSizeColumn(1);
        sheet1.autoSizeColumn(2);
        sheet1.autoSizeColumn(3);

        wb.write(out);
    }
}
