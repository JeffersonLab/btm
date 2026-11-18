package org.jlab.btm.presentation.controller.excel;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.jlab.btm.business.service.CcAccHourService;
import org.jlab.btm.business.service.MonthlyScheduleService;
import org.jlab.btm.business.service.PdShiftPlanService;
import org.jlab.btm.business.service.excel.ExcelBeamSummaryService;
import org.jlab.btm.persistence.projection.CcAccSum;
import org.jlab.btm.persistence.projection.PacAccSum;
import org.jlab.btm.persistence.projection.PdAccSum;
import org.jlab.smoothness.business.util.TimeUtil;
import org.junit.jupiter.api.Test;

/**
 * Runs the Beam Summary's Excel export with stubbed services and checks the range it summarizes.
 * The Beam Summary page sends its range with times, in Smoothness's friendly date-time pattern.
 */
class ExcelBeamSummaryTest {

  private static final Date START = Date.from(Instant.parse("2026-11-08T12:00:00Z"));
  private static final Date END = Date.from(Instant.parse("2026-11-15T12:00:00Z"));

  @Test
  void exportsTheRangeWithTheTimesThePageSends() throws Exception {
    List<Date> ranges = export(Map.of("start", "08-Nov-2026 07:00", "end", "15-Nov-2026 07:00"));

    assertEquals(List.of(START, END, START, END, START, END), ranges);
  }

  @Test
  void acceptsThePagesFriendlyDateTimePattern() throws Exception {
    SimpleDateFormat format = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

    List<Date> ranges = export(Map.of("start", format.format(START), "end", format.format(END)));

    assertEquals(List.of(START, END, START, END, START, END), ranges);
  }

  @Test
  void rejectsADateWithoutATime() {
    assertThrows(
        ServletException.class, () -> export(Map.of("start", "08-Nov-2026", "end", "15-Nov-2026")));
  }

  @Test
  void rejectsAStartAfterTheEnd() {
    assertThrows(
        ServletException.class,
        () -> export(Map.of("start", "15-Nov-2026 07:00", "end", "08-Nov-2026 07:00")));
  }

  /**
   * Calls the servlet's GET handler and returns the start and end each summary service was asked
   * for, in the order CC, PD and NPES, after checking that the workbook was exported.
   */
  private static List<Date> export(Map<String, String> parameters) throws Exception {
    List<Date> ranges = new ArrayList<>();
    boolean[] exported = {false};

    ExcelBeamSummary servlet = new ExcelBeamSummary();
    servlet.ccService =
        new CcAccHourService() {
          @Override
          public CcAccSum findSummary(Date start, Date end) {
            ranges.addAll(List.of(start, end));
            return null;
          }
        };
    servlet.pdService =
        new PdShiftPlanService() {
          @Override
          public PdAccSum findSummary(Date start, Date end) {
            ranges.addAll(List.of(start, end));
            return null;
          }
        };
    servlet.pacService =
        new MonthlyScheduleService() {
          @Override
          public PacAccSum findSummary(Date start, Date end) {
            ranges.addAll(List.of(start, end));
            return null;
          }
        };
    servlet.excelService =
        new ExcelBeamSummaryService() {
          @Override
          public void export(
              OutputStream out, CcAccSum ccSum, PdAccSum pdSum, PacAccSum pacSum, String filters) {
            exported[0] = true;
          }
        };

    HttpServletRequest request =
        stub(
            HttpServletRequest.class,
            (method, args) ->
                method.equals("getParameter") ? parameters.get((String) args[0]) : null);
    HttpServletResponse response = stub(HttpServletResponse.class, (method, args) -> null);

    servlet.doGet(request, response);

    assertTrue(exported[0], "workbook exported");
    return ranges;
  }

  private interface Handler {
    Object handle(String method, Object[] args);
  }

  private static <T> T stub(Class<T> type, Handler handler) {
    return type.cast(
        Proxy.newProxyInstance(
            ExcelBeamSummaryTest.class.getClassLoader(),
            new Class<?>[] {type},
            (proxy, method, args) -> handler.handle(method.getName(), args)));
  }
}
