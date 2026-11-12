package org.jlab.btm.business.service;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jlab.btm.persistence.entity.ExpProgram;
import org.jlab.btm.persistence.entity.MonthlySchedule;
import org.jlab.btm.persistence.entity.ScheduleDay;
import org.jlab.btm.presentation.util.BtmFunctions;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * @author ryans
 */
@Stateless
public class ExcelScheduleService {

  private static final Logger LOGGER = Logger.getLogger(ExpHourService.class.getName());

  @EJB ExpProgramService purposeService;
  @EJB MonthlyScheduleService scheduleService;

  @RolesAllowed({"schcom", "btm-admin"})
  public List<MonthlySchedule> upload(InputStream in)
      throws IOException, InvalidFormatException, UserFriendlyException {
    List<MonthlySchedule> resultList = new ArrayList<>();

    Workbook wb =
        WorkbookFactory.create(
            in); // Wrap in try with resources doesn't work as close method results in
    // NoSuchMethodError?

    if (wb.getNumberOfSheets() != 1) {
      throw new IOException("Exactly one sheet is expected");
    }

    Map<Hall, Map<String, ExpProgram>> programsByHall = new EnumMap<>(Hall.class);

    for (Hall hall : new Hall[] {Hall.A, Hall.B, Hall.C, Hall.D}) {
      programsByHall.put(hall, purposeService.findProgramByHallNameMap(hall));
    }

    // Parse the whole sheet before creating any schedule so a bad row creates nothing
    List<ExcelScheduleParser.ParsedMonth> months =
        new ExcelScheduleParser(programsByHall).parse(wb.getSheetAt(0));

    for (ExcelScheduleParser.ParsedMonth month : months) {
      MonthlySchedule schedule = scheduleService.create(month.getStart());
      resultList.add(schedule);

      for (ScheduleDay day : month.getDays()) {
        scheduleService.setDay(schedule, day);
      }
    }

    return resultList;
  }

  @PermitAll
  public void export(
      OutputStream out, MonthlySchedule schedule, Map<Integer, ExpProgram> purposeMap)
      throws IOException {
    Workbook wb = new XSSFWorkbook();
    Sheet sheet1 = wb.createSheet("Schedule");

    SimpleDateFormat monthDateFormat = new SimpleDateFormat("MMMM yyyy");
    SimpleDateFormat monthAndDayDateFormat = new SimpleDateFormat("dd MMM yyyy");

    String headerValue =
        monthDateFormat.format(schedule.getStartDay()) + "; Version " + schedule.getVersion();

    if (schedule.getPublishedDate() != null) {
      headerValue =
          headerValue
              + " (Published "
              + monthAndDayDateFormat.format(schedule.getPublishedDate())
              + ")";
    } else {
      headerValue = headerValue + " (Tentative)";
    }

    int rownum = 0;
    Row row0 = sheet1.createRow(rownum++);
    row0.createCell(0).setCellValue(headerValue);

    Row row1 = sheet1.createRow(rownum++);
    row1.createCell(0).setCellValue("");
    row1.createCell(1).setCellValue("Program");
    row1.createCell(2).setCellValue("GeV/Pass");
    row1.createCell(3).setCellValue("Min Hall Count");
    row1.createCell(4).setCellValue("Hall A Program");
    row1.createCell(5).setCellValue("Hall B Program");
    row1.createCell(6).setCellValue("Hall C Program");
    row1.createCell(7).setCellValue("Hall D Program");
    row1.createCell(8).setCellValue("Hall A GeV");
    row1.createCell(9).setCellValue("Hall B GeV");
    row1.createCell(10).setCellValue("Hall C GeV");
    row1.createCell(11).setCellValue("Hall D GeV");
    row1.createCell(12).setCellValue("Hall A μA");
    row1.createCell(13).setCellValue("Hall B μA");
    row1.createCell(14).setCellValue("Hall C μA");
    row1.createCell(15).setCellValue("Hall D μA");
    row1.createCell(16).setCellValue("Hall A Polarized");
    row1.createCell(17).setCellValue("Hall B Polarized");
    row1.createCell(18).setCellValue("Hall C Polarized");
    row1.createCell(19).setCellValue("Hall D Polarized");
    row1.createCell(20).setCellValue("Hall A Passes");
    row1.createCell(21).setCellValue("Hall B Passes");
    row1.createCell(22).setCellValue("Hall C Passes");
    row1.createCell(23).setCellValue("Hall D Passes");
    row1.createCell(24).setCellValue("Priority");
    row1.createCell(25).setCellValue("Hall A Notes");
    row1.createCell(26).setCellValue("Hall B Notes");
    row1.createCell(27).setCellValue("Hall C Notes");
    row1.createCell(28).setCellValue("Hall D Notes");
    row1.createCell(29).setCellValue("Notes");

    CreationHelper createHelper = wb.getCreationHelper();
    CellStyle floatStyle = wb.createCellStyle();
    floatStyle.setDataFormat(createHelper.createDataFormat().getFormat("##0.000"));
    CellStyle hallDFloatStyle = wb.createCellStyle();
    hallDFloatStyle.setDataFormat(createHelper.createDataFormat().getFormat("0.0"));
    CellStyle intStyle = wb.createCellStyle();
    intStyle.setDataFormat(createHelper.createDataFormat().getFormat("##0"));
    CellStyle dateStyle = wb.createCellStyle();
    dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd MMM yyyy"));

    Cell c;

    for (ScheduleDay day : schedule.getScheduleDayList()) {
      Row row = sheet1.createRow(rownum++);

      c = row.createCell(0);
      c.setCellStyle(dateStyle);
      c.setCellValue(day.getDayMonthYear());
      row.createCell(1).setCellValue(day.getAccProgram());
      c = row.createCell(2);
      c.setCellStyle(floatStyle);
      if (day.getKiloVoltsPerPass() != null) {
        c.setCellValue(kiloToGiga(day.getKiloVoltsPerPass()));
      }
      c = row.createCell(3);
      c.setCellStyle(intStyle);
      if (day.getMinHallCount() != null) {
        c.setCellValue(day.getMinHallCount());
      }
      row.createCell(4).setCellValue(purposeName(purposeMap.get(day.getHallAProgramId())));
      row.createCell(5).setCellValue(purposeName(purposeMap.get(day.getHallBProgramId())));
      row.createCell(6).setCellValue(purposeName(purposeMap.get(day.getHallCProgramId())));
      row.createCell(7).setCellValue(purposeName(purposeMap.get(day.getHallDProgramId())));
      c = row.createCell(8);
      c.setCellStyle(floatStyle);
      if (day.getHallAKiloVolts() != null) {
        c.setCellValue(kiloToGiga(day.getHallAKiloVolts()));
      }
      c = row.createCell(9);
      c.setCellStyle(floatStyle);
      if (day.getHallBKiloVolts() != null) {
        c.setCellValue(kiloToGiga(day.getHallBKiloVolts()));
      }
      c = row.createCell(10);
      c.setCellStyle(floatStyle);
      if (day.getHallCKiloVolts() != null) {
        c.setCellValue(kiloToGiga(day.getHallCKiloVolts()));
      }
      c = row.createCell(11);
      c.setCellStyle(floatStyle);
      if (day.getHallDKiloVolts() != null) {
        c.setCellValue(kiloToGiga(day.getHallDKiloVolts()));
      }
      c = row.createCell(12);
      c.setCellStyle(floatStyle);
      if (day.getHallANanoAmps() != null) {
        c.setCellValue(nanoToMicro(day.getHallANanoAmps()));
      }
      c = row.createCell(13);
      c.setCellStyle(floatStyle);
      if (day.getHallBNanoAmps() != null) {
        c.setCellValue(nanoToMicro(day.getHallBNanoAmps()));
      }
      c = row.createCell(14);
      c.setCellStyle(floatStyle);
      if (day.getHallCNanoAmps() != null) {
        c.setCellValue(nanoToMicro(day.getHallCNanoAmps()));
      }
      c = row.createCell(15);
      c.setCellStyle(floatStyle);
      if (day.getHallDNanoAmps() != null) {
        c.setCellValue(nanoToMicro(day.getHallDNanoAmps()));
      }
      row.createCell(16)
          .setCellValue(day.getHallAPolarized() == null ? "" : day.getHallAPolarized() ? "✔" : "");
      row.createCell(17)
          .setCellValue(day.getHallBPolarized() == null ? "" : day.getHallBPolarized() ? "✔" : "");
      row.createCell(18)
          .setCellValue(day.getHallCPolarized() == null ? "" : day.getHallCPolarized() ? "✔" : "");
      row.createCell(19)
          .setCellValue(day.getHallDPolarized() == null ? "" : day.getHallDPolarized() ? "✔" : "");
      c = row.createCell(20);
      c.setCellStyle(intStyle);
      if (day.getHallAPasses() != null) {
        c.setCellValue(day.getHallAPasses());
      }
      c = row.createCell(21);
      c.setCellStyle(intStyle);
      if (day.getHallBPasses() != null) {
        c.setCellValue(day.getHallBPasses());
      }
      c = row.createCell(22);
      c.setCellStyle(intStyle);
      if (day.getHallCPasses() != null) {
        c.setCellValue(day.getHallCPasses());
      }
      c = row.createCell(23);
      c.setCellStyle(hallDFloatStyle);
      if (day.getHallDPasses() != null) {
        c.setCellValue(day.getHallDPasses() - 0.5);
      }
      row.createCell(24)
          .setCellValue(
              BtmFunctions.formatPriority(
                  day.getHallAPriority(),
                  day.getHallBPriority(),
                  day.getHallCPriority(),
                  day.getHallDPriority(),
                  day.getHallAKiloVolts(),
                  day.getHallBKiloVolts(),
                  day.getHallCKiloVolts(),
                  day.getHallDKiloVolts()));
      row.createCell(25).setCellValue(day.getHallANote());
      row.createCell(26).setCellValue(day.getHallBNote());
      row.createCell(27).setCellValue(day.getHallCNote());
      row.createCell(28).setCellValue(day.getHallDNote());
      row.createCell(29).setCellValue(day.getNote());
    }

    sheet1.autoSizeColumn(0);
    sheet1.autoSizeColumn(1);
    sheet1.autoSizeColumn(2);
    sheet1.autoSizeColumn(3);
    sheet1.autoSizeColumn(4);
    sheet1.autoSizeColumn(5);
    sheet1.autoSizeColumn(6);
    sheet1.autoSizeColumn(7);
    sheet1.autoSizeColumn(8);
    sheet1.autoSizeColumn(9);
    sheet1.autoSizeColumn(10);
    sheet1.autoSizeColumn(11);
    sheet1.autoSizeColumn(12);
    sheet1.autoSizeColumn(13);
    sheet1.autoSizeColumn(14);
    sheet1.autoSizeColumn(15);
    sheet1.autoSizeColumn(16);
    sheet1.autoSizeColumn(17);
    sheet1.autoSizeColumn(18);
    sheet1.autoSizeColumn(19);
    sheet1.autoSizeColumn(20);
    sheet1.autoSizeColumn(21);
    sheet1.autoSizeColumn(22);
    sheet1.autoSizeColumn(23);
    sheet1.autoSizeColumn(24);
    sheet1.autoSizeColumn(25);
    sheet1.autoSizeColumn(26);
    sheet1.autoSizeColumn(27);
    sheet1.autoSizeColumn(28);
    sheet1.autoSizeColumn(29);

    wb.write(out);
  }

  private double kiloToGiga(int kilo) {
    return kilo / 1000000.0;
  }

  private double nanoToMicro(int nano) {
    return nano / 1000.0;
  }

  private String purposeName(ExpProgram purpose) {
    String name = "";

    if (purpose != null) {
      name = purpose.getName();
    }

    return name;
  }

  /*private String nullToEmpty(Integer input) {
  String output = "";

  if(input != null) {
  output = String.valueOf(input);
  }

  return output;
  }*/
  /*private String nullToEmpty(String value) {
  if (value == null) {
  value = "";
  }

  return value;
  }*/
}
