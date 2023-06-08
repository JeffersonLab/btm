package org.jlab.btm.business.service;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jlab.btm.persistence.entity.ExpShiftPurpose;
import org.jlab.btm.persistence.entity.MonthlySchedule;
import org.jlab.btm.persistence.entity.ScheduleDay;
import org.jlab.btm.persistence.enumeration.AcceleratorProgram;
import org.jlab.btm.persistence.projection.HallPriority;
import org.jlab.btm.presentation.util.BtmFunctions;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.enumeration.Hall;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author ryans
 */
@Stateless
public class ExcelScheduleService {

    private final static Logger LOGGER = Logger.getLogger(ExpHourService.class.getName());

    @EJB
    ExpShiftPurposeService purposeService;
    @EJB
    MonthlyScheduleService scheduleService;

    @RolesAllowed({"schcom"})
    public List<MonthlySchedule> upload(InputStream in) throws IOException, InvalidFormatException, UserFriendlyException {
        List<MonthlySchedule> resultList = new ArrayList<>();

        Workbook wb = WorkbookFactory.create(in); // Wrap in try with resources doesn't work as close method results in NoSuchMethodError?

        if (wb.getNumberOfSheets() != 1) {
            throw new IOException("Exactly one sheet is expected");
        }

        Sheet sheet = wb.getSheetAt(0);

        MonthlySchedule schedule = null;

        Map<String, ExpShiftPurpose> hallAPurposeMap = purposeService.findPurposeByHallNameMap(Hall.A);
        Map<String, ExpShiftPurpose> hallBPurposeMap = purposeService.findPurposeByHallNameMap(Hall.B);
        Map<String, ExpShiftPurpose> hallCPurposeMap = purposeService.findPurposeByHallNameMap(Hall.C);
        Map<String, ExpShiftPurpose> hallDPurposeMap = purposeService.findPurposeByHallNameMap(Hall.D);

        if (hallAPurposeMap.get("OFF") == null) {
            throw new UserFriendlyException("Hall A must have a program named 'OFF', please update program list");
        }

        if (hallBPurposeMap.get("OFF") == null) {
            throw new UserFriendlyException("Hall B must have a program named 'OFF', please update program list");
        }

        if (hallCPurposeMap.get("OFF") == null) {
            throw new UserFriendlyException("Hall C must have a program named 'OFF', please update program list");
        }

        if (hallDPurposeMap.get("OFF") == null) {
            throw new UserFriendlyException("Hall D must have a program named 'OFF', please update program list");
        }

        Integer hallAOffId = hallAPurposeMap.get("OFF").getExpHallShiftPurposeId().intValue();
        Integer hallBOffId = hallBPurposeMap.get("OFF").getExpHallShiftPurposeId().intValue();
        Integer hallCOffId = hallCPurposeMap.get("OFF").getExpHallShiftPurposeId().intValue();
        Integer hallDOffId = hallDPurposeMap.get("OFF").getExpHallShiftPurposeId().intValue();

        for (Row row : sheet) {

            if (row.getRowNum() < 2) { // zero based
                continue; // Skip first two rows as they are headers
            }

            ScheduleDay day = new ScheduleDay();

            Cell cell = row.getCell(0); // Date

            if (DateUtil.isCellDateFormatted(cell) && cell.getDateCellValue() != null) {
                day.setDayMonthYear(cell.getDateCellValue());
            } else {
                continue;
                // Skip row if first cell is null or blank or otherwise not a date
            }

            //cell = row.getCell(1); // Weekday, skip
            cell = row.getCell(2); // GeV/pass

            if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                day.setKiloVoltsPerPass((int) gigaToKilo(cell.getNumericCellValue()));
            }

            cell = row.getCell(3); // Program

            if (cell == null || cell.getCellType() == CellType.BLANK) {
                day.setAccProgram("OFF");
            } else {
                String programStr = cell.getStringCellValue().toUpperCase();
                try {
                    AcceleratorProgram accProgram = AcceleratorProgram.valueOf(programStr);
                } catch (IllegalArgumentException e) {
                    throw new UserFriendlyException("Unrecognized Accelerator Program, found: " + programStr);
                }
                day.setAccProgram(programStr);
            }

            // Hall A
            cell = row.getCell(4); // Hall Program

            Integer programId = this.parseHallProgram(cell, hallAPurposeMap, Hall.A);
            if (programId == null) {
                programId = hallAOffId;
            }
            day.setHallAProgramId(programId);

            cell = row.getCell(5); // GeV/mA/Pol/MHz

            HallProperties hallProperties = parseHallProperties(cell, Hall.A, "uA");

            day.setHallAPolarized(false); // Can't be null so init false            

            if (hallProperties != null) {
                day.setHallAKiloVolts(hallProperties.keV);
                day.setHallANanoAmps(hallProperties.nA);
                day.setHallAPolarized(hallProperties.polarized);
                //TODO: set MHz?
            }

            // Hall B
            cell = row.getCell(6); // Hall Program

            programId = this.parseHallProgram(cell, hallBPurposeMap, Hall.B);
            if (programId == null) {
                programId = hallBOffId;
            }
            day.setHallBProgramId(programId);

            cell = row.getCell(7); // GeV/mA/Pol/MHz

            hallProperties = parseHallProperties(cell, Hall.B, "nA");

            day.setHallBPolarized(false); // Can't be null so init false            

            if (hallProperties != null) {
                day.setHallBKiloVolts(hallProperties.keV);
                day.setHallBNanoAmps(hallProperties.nA);
                day.setHallBPolarized(hallProperties.polarized);
                //TODO: set MHz?
            }

            // Hall C
            cell = row.getCell(8); // Hall Program

            programId = this.parseHallProgram(cell, hallCPurposeMap, Hall.C);
            if (programId == null) {
                programId = hallCOffId;
            }
            day.setHallCProgramId(programId);

            cell = row.getCell(9); // GeV/mA/Pol/MHz

            hallProperties = parseHallProperties(cell, Hall.C, "uA");

            day.setHallCPolarized(false); // Can't be null so init false            

            if (hallProperties != null) {
                day.setHallCKiloVolts(hallProperties.keV);
                day.setHallCNanoAmps(hallProperties.nA);
                day.setHallCPolarized(hallProperties.polarized);
                //TODO: set MHz?
            }

            // Hall D
            cell = row.getCell(10); // Hall Program

            programId = this.parseHallProgram(cell, hallDPurposeMap, Hall.D);
            if (programId == null) {
                programId = hallDOffId;
            }
            day.setHallDProgramId(programId);

            cell = row.getCell(11); // GeV/mA/Pol/MHz

            hallProperties = parseHallProperties(cell, Hall.D, "nA");

            day.setHallDPolarized(false); // Can't be null so init false

            if (hallProperties != null) {
                day.setHallDKiloVolts(hallProperties.keV);
                day.setHallDNanoAmps(hallProperties.nA);
                day.setHallDPolarized(hallProperties.polarized);
                //TODO: set MHz?
            }

            cell = row.getCell(12); // Priority Hall

            String priorityHall = null;

            if (cell != null && cell.getCellType() == CellType.STRING) {
                priorityHall = cell.getStringCellValue();

                List<HallPriority> priorityList = BtmFunctions.parsePriorityString(priorityHall);

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

            if(cell != null && cell.getCellType() == CellType.STRING) {
                String passString = cell.getStringCellValue();

                String[] tokens = passString.split("/");
                Integer[] passes = new Integer[4];

                for(int i = 0; i < 4; i++) {
                    try {
                        passes[i] = Math.round(Float.valueOf(tokens[i])); // Hall D might read 0.5 or 5.5, so round up and GUI will know what to do.
                    } catch (NumberFormatException e) {
                        passes[i] = null;
                    }
                }

                day.setHallAPasses(passes[0]);
                day.setHallBPasses(passes[1]);
                day.setHallCPasses(passes[2]);
                day.setHallDPasses(passes[3]);
            }

            if (TimeUtil.isFirstOfMonth(day.getDayMonthYear(), Calendar.getInstance())) {
                schedule = scheduleService.create(day.getDayMonthYear());
                resultList.add(schedule);
            }

            if (schedule == null) {
                throw new UserFriendlyException("Excel schedule must start on first of month");
            } else if (!TimeUtil.isSameMonth(schedule.getStartDay(), day.getDayMonthYear())) {
                throw new UserFriendlyException("Each month must start with day 1");
            } else {
                day = scheduleService.setDay(schedule, day);
            }
        }

        return resultList;
    }

    private HallProperties parseHallProperties(Cell cell, Hall hall, String currentUnits) throws UserFriendlyException {
        HallProperties properties = null;

        if (cell != null && cell.getCellType() == CellType.STRING) {
            String hallProperties = cell.getStringCellValue();

            if (hallProperties != null && !hallProperties.trim().isEmpty()) {
                String[] tokens = hallProperties.split("/");

                if (tokens.length != 4) {
                    throw new UserFriendlyException("Hall " + hall + " Properties must be formated 'GeV/mA/Pol/MHz', found: " + hallProperties);
                }

                properties = new HallProperties();

                double geV;
                double current;

                try {
                    geV = Double.parseDouble(tokens[0]);
                } catch (NumberFormatException e) {
                    throw new UserFriendlyException("Hall " + hall + " GeV must be a number, found: " + tokens[0]);
                }
                properties.keV = ((int) gigaToKilo(geV));

                try {
                    current = Double.parseDouble(tokens[1]);
                } catch (NumberFormatException e) {
                    throw new UserFriendlyException("Hall " + hall + " current must be a number, found: " + tokens[1]);
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
                    throw new UserFriendlyException("Hall " + hall + " Pol must be one of 'p,-', found: " + tokens[2]);
                }

                try {
                    properties.mHz = Integer.parseInt(tokens[3]);
                } catch (NumberFormatException e) {
                    throw new UserFriendlyException("Hall " + hall + " MHz must be a number, found: " + tokens[3]);
                }
            }
        }

        return properties;
    }

    private Integer parseHallProgram(Cell cell, Map<String, ExpShiftPurpose> purposeMap, Hall hall) throws UserFriendlyException {
        String purposeName = null;
        Integer programId = null;

        if (cell != null && cell.getCellType() == CellType.STRING) {
            purposeName = cell.getStringCellValue();
        }

        if (purposeName != null) {
            ExpShiftPurpose hallProgram = purposeMap.get(purposeName);

            if (hallProgram == null) {
                throw new UserFriendlyException("Could not find hall " + hall + " program: '" + purposeName + "', please update Program list");
            }

            programId = hallProgram.getExpHallShiftPurposeId().intValue();
        }

        return programId;
    }

    @PermitAll
    public void export(OutputStream out, MonthlySchedule schedule,
                       Map<Integer, ExpShiftPurpose> purposeMap) throws IOException {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet1 = wb.createSheet("Schedule");

        SimpleDateFormat monthDateFormat = new SimpleDateFormat("MMMM yyyy");
        SimpleDateFormat monthAndDayDateFormat = new SimpleDateFormat("dd MMM yyyy");

        String headerValue = monthDateFormat.format(schedule.getStartDay()) + "; Version "
                + schedule.getVersion();

        if (schedule.getPublishedDate() != null) {
            headerValue = headerValue + " (Published " + monthAndDayDateFormat.format(
                    schedule.getPublishedDate()) + ")";
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
        dateStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("dd MMM yyyy"));

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
            row.createCell(16).setCellValue(
                    day.getHallAPolarized() == null ? "" : day.getHallAPolarized() ? "✔" : "");
            row.createCell(17).setCellValue(
                    day.getHallBPolarized() == null ? "" : day.getHallBPolarized() ? "✔" : "");
            row.createCell(18).setCellValue(
                    day.getHallCPolarized() == null ? "" : day.getHallCPolarized() ? "✔" : "");
            row.createCell(19).setCellValue(
                    day.getHallDPolarized() == null ? "" : day.getHallDPolarized() ? "✔" : "");
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
            row.createCell(24).setCellValue(BtmFunctions.formatPriority(day.getHallAPriority(),
                    day.getHallBPriority(), day.getHallCPriority(),
                    day.getHallDPriority(), day.getHallAKiloVolts(), day.getHallBKiloVolts(), day.getHallCKiloVolts(), day.getHallDKiloVolts()));
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

    private double gigaToKilo(double giga) {
        return giga * 1000000.0;
    }

    private double nanoToMicro(int nano) {
        return nano / 1000.0;
    }

    private double microToNano(double micro) {
        return micro * 1000.0;
    }

    private String purposeName(ExpShiftPurpose purpose) {
        String name = "";

        if (purpose != null) {
            name = purpose.getName();
        }

        return name;
    }

    private class HallProperties {

        public int keV;
        public int nA;
        public boolean polarized;
        public int mHz;

        public String toString() {
            return "keV: " + keV + ", nA: " + nA;
        }
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
