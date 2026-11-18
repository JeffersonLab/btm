package org.jlab.btm.presentation.controller.reports;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.btm.business.service.CcMultiplicityHourService;
import org.jlab.btm.business.service.ExpHourService;
import org.jlab.btm.business.service.PdShiftPlanService;
import org.jlab.btm.persistence.projection.ExpHourTotals;
import org.jlab.btm.persistence.projection.MultiplicitySummaryTotals;
import org.jlab.btm.persistence.projection.PhysicsSummaryTotals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Runs the Hall Availability and Physics Time Accounting servlets with stubbed services and checks
 * the unknown hours they hand to their views. The totals are in seconds and the period in hours, so
 * a recorded total that is not a whole number of hours must not be truncated.
 */
class UnknownHoursTest {

  @ParameterizedTest(name = "{0} to {1} with {2} s recorded leaves {3} h unknown")
  @CsvSource({
    "01-Jan-2026 07:00, 01-Jan-2026 08:00, 1800, 0.5",
    "01-Jan-2026 07:00, 01-Jan-2026 08:00, 3600, 0.0",
    "01-Jan-2026 07:00, 01-Jan-2026 08:00, 0, 1.0",
    "01-Jan-2026 07:00, 01-Jan-2026 11:00, 10800, 1.0",
    "01-Jan-2026 07:00, 01-Jan-2026 07:30, 900, 0.25",
    "01-Jan-2026 07:00, 01-Jan-2026 07:30, 1800, 0.0",
    "01-Jan-2026 07:00, 03-Jan-2026 07:00, 109800, 17.5"
  })
  void hallAvailabilityKeepsFractionalHours(
      String start, String end, long recordedSeconds, double expectedUnknown) throws Exception {
    HallAvailability servlet = new HallAvailability();
    servlet.expHourService =
        new ExpHourService() {
          @Override
          public List<ExpHourTotals> findExpHallHourTotals(Date s, Date e) {
            return List.of(new ExpHourTotals('A', 0, recordedSeconds, 0, 0, 0, 0, 0, 0));
          }
        };
    servlet.pdShiftService =
        new PdShiftPlanService() {
          @Override
          public Long[] findScheduledHallTime(Date s, Date e) {
            return new Long[] {0L, 0L, 0L, 0L};
          }
        };

    Map<String, Object> attributes =
        get(servlet, Map.of("start", start, "end", end, "availability-chart", "bar"));

    assertEquals(List.of(expectedUnknown), attributes.get("expUnknownList"));
  }

  @ParameterizedTest(name = "{0} to {1} with {2} s recorded leaves {3} h unknown")
  @CsvSource({
    "01-Jan-2026 07:00, 01-Jan-2026 08:00, 1800, 0.5",
    "01-Jan-2026 07:00, 01-Jan-2026 08:00, 3600, 0.0",
    "01-Jan-2026 07:00, 01-Jan-2026 08:00, 0, 1.0",
    "01-Jan-2026 07:00, 01-Jan-2026 11:00, 10800, 1.0",
    "01-Jan-2026 07:00, 01-Jan-2026 07:30, 900, 0.25",
    "01-Jan-2026 07:00, 01-Jan-2026 07:30, 1800, 0.0",
    "01-Jan-2026 07:00, 03-Jan-2026 07:00, 109800, 17.5"
  })
  void physicsSummaryKeepsFractionalHours(
      String start, String end, long recordedSeconds, double expectedUnknown) throws Exception {
    PhysicsSummary servlet = new PhysicsSummary();
    // Experimenter time is recorded as ABU and Crew Chief time as UP
    servlet.expHourService =
        new ExpHourService() {
          @Override
          public List<PhysicsSummaryTotals> reportTotals(Date s, Date e) {
            return List.of(
                new PhysicsSummaryTotals(
                    'A', recordedSeconds, 0, 0, 0, recordedSeconds, 0, 0, 0, 0));
          }
        };
    servlet.multiplicityService =
        new CcMultiplicityHourService() {
          @Override
          public MultiplicitySummaryTotals reportTotals(Date s, Date e) {
            return new MultiplicitySummaryTotals(0, 0, 0, 0, 0, 0, 0);
          }
        };
    servlet.planService =
        new PdShiftPlanService() {
          @Override
          public Long[] findScheduledHallTime(Date s, Date e) {
            return new Long[] {0L, 0L, 0L, 0L};
          }
        };

    Map<String, Object> attributes =
        get(servlet, Map.of("start", start, "end", end, "physics-data", "available"));

    assertEquals(List.of(expectedUnknown), attributes.get("expUnknownList"));
    assertEquals(List.of(expectedUnknown), attributes.get("opUnknownList"));
  }

  @ParameterizedTest(name = "an hour of {0} leaves no Crew Chief Implicit OFF")
  @CsvSource({"UP", "TUNE", "BNR", "DOWN", "OFF"})
  void physicsSummaryCountsEveryCrewChiefCategory(String category) throws Exception {
    long up = category.equals("UP") ? 3600 : 0;
    long tune = category.equals("TUNE") ? 3600 : 0;
    long bnr = category.equals("BNR") ? 3600 : 0;
    long down = category.equals("DOWN") ? 3600 : 0;
    long off = category.equals("OFF") ? 3600 : 0;

    PhysicsSummary servlet = new PhysicsSummary();
    servlet.expHourService =
        new ExpHourService() {
          @Override
          public List<PhysicsSummaryTotals> reportTotals(Date s, Date e) {
            return List.of(new PhysicsSummaryTotals('A', 3600, 0, 0, 0, up, tune, bnr, down, off));
          }
        };
    servlet.multiplicityService =
        new CcMultiplicityHourService() {
          @Override
          public MultiplicitySummaryTotals reportTotals(Date s, Date e) {
            return new MultiplicitySummaryTotals(0, 0, 0, 0, 0, 0, 0);
          }
        };
    servlet.planService =
        new PdShiftPlanService() {
          @Override
          public Long[] findScheduledHallTime(Date s, Date e) {
            return new Long[] {0L, 0L, 0L, 0L};
          }
        };

    Map<String, Object> attributes =
        get(
            servlet,
            Map.of(
                "start", "01-Jan-2026 07:00", "end", "01-Jan-2026 08:00", "physics-data", "table"));

    assertEquals(List.of(0.0), attributes.get("opUnknownList"));
  }

  /** Calls the servlet's GET handler and returns the request attributes it set for its view. */
  private static Map<String, Object> get(
      jakarta.servlet.http.HttpServlet servlet, Map<String, String> parameters) throws Exception {
    Map<String, Object> attributes = new HashMap<>();
    Map<String, Object> sessionAttributes = new HashMap<>();
    boolean[] forwarded = {false};

    HttpSession session =
        stub(
            HttpSession.class,
            (method, args) ->
                switch (method) {
                  case "getAttribute" -> sessionAttributes.get((String) args[0]);
                  case "setAttribute" -> sessionAttributes.put((String) args[0], args[1]);
                  default -> null;
                });
    RequestDispatcher dispatcher =
        stub(
            RequestDispatcher.class,
            (method, args) -> {
              forwarded[0] = forwarded[0] || method.equals("forward");
              return null;
            });
    HttpServletRequest request =
        stub(
            HttpServletRequest.class,
            (method, args) ->
                switch (method) {
                  case "getParameter" -> parameters.get((String) args[0]);
                  case "getSession" -> session;
                  case "getAttribute" -> attributes.get((String) args[0]);
                  case "setAttribute" -> attributes.put((String) args[0], args[1]);
                  case "getRequestDispatcher" -> dispatcher;
                  case "getContextPath" -> "/btm";
                  default -> null;
                });
    HttpServletResponse response =
        stub(
            HttpServletResponse.class,
            (method, args) -> {
              throw new AssertionError("Unexpected response call: " + method);
            });

    if (servlet instanceof HallAvailability hallAvailability) {
      hallAvailability.doGet(request, response);
    } else {
      ((PhysicsSummary) servlet).doGet(request, response);
    }

    assertTrue(forwarded[0], "The servlet should forward to its view");
    return attributes;
  }

  private interface Handler {
    Object handle(String method, Object[] args);
  }

  private static <T> T stub(Class<T> type, Handler handler) {
    return type.cast(
        Proxy.newProxyInstance(
            type.getClassLoader(),
            new Class<?>[] {type},
            (proxy, method, args) -> handler.handle(method.getName(), args)));
  }
}
