package org.jlab.btm.presentation.controller.rest;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.StringReader;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jboss.resteasy.spi.Dispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Dispatches requests through RESTEasy without an application server. Invalid parameters must be
 * rejected before the streamed body runs, so these requests never need the EJB lookups.
 */
class RestDateValidationTest {
  private static final String BAD_FORMAT = "unrecognized date format";
  private static final String RANGE_REQUIRED = "start and end dates in ISO8601 format are required";

  private Dispatcher dispatcher;

  @BeforeEach
  void setUp() {
    dispatcher = MockDispatcherFactory.createDispatcher();
    dispatcher.getRegistry().addPerRequestResource(ScheduleEndpoint.class);
    dispatcher.getRegistry().addPerRequestResource(ScheduledCountEndpoint.class);
    dispatcher.getRegistry().addPerRequestResource(MonthlyScheduleEndpoint.class);
    dispatcher.getRegistry().addPerRequestResource(ShiftSummary.class);
    dispatcher.getRegistry().addPerRequestResource(WeekSummary.class);
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "/schedule?start=not-a-date&end=2026-01-02 | " + BAD_FORMAT,
        "/schedule?start=2026-01-01&end=2026-13-01 | " + BAD_FORMAT,
        "/schedule?start=2026-01-01 | " + RANGE_REQUIRED,
        "/schedule | " + RANGE_REQUIRED,
        "/scheduled-count?start=not-a-date&end=2026-01-02 | " + BAD_FORMAT,
        "/scheduled-count?start=2026-01-01&end=not-a-date | " + BAD_FORMAT,
        "/scheduled-count?end=2026-01-02 | " + RANGE_REQUIRED,
        "/monthly-schedule?month=not-a-date | " + BAD_FORMAT,
        "/shift-summary?shift-start=not-a-date | " + BAD_FORMAT,
        "/shift-summary?shift-start=2026-01-02 | " + BAD_FORMAT,
        "/shift-summary | Parameter shift-start (Crew Chief Shift Start) in format yyyy-mm-ddThh"
            + " is missing",
        "/shift-summary?shift-start=2026-01-02T08 | shift start hour must be one of 23, 7, 15",
        "/week-summary?week-start=not-a-date | " + BAD_FORMAT,
        "/week-summary | Parameter week-start (Crew Chief Shift Start) in format yyyy-mm-ddThh is"
            + " missing",
        "/week-summary?week-start=2026-01-02T08 | week start hour must be one of 23, 7, 15 to"
            + " correspond to a crew chief shift start"
      })
  void rejectsInvalidDatesWithJsonBadRequest(String uri, String message) throws Exception {
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(MockHttpRequest.get(uri), response);

    assertEquals(400, response.getStatus());
    try (JsonReader reader = Json.createReader(new StringReader(response.getContentAsString()))) {
      JsonObject body = reader.readObject();
      assertEquals(message, body.getString("message"));
    }
  }

  @Test
  void acceptsValidDatesAndDefersWorkToTheStream() {
    assertStreams(new ScheduleEndpoint().getSchedulesJson("2026-01-01", "2026-01-02"));
    assertStreams(new ScheduledCountEndpoint().getSchedulesJson("2026-01-01", "2026-01-02"));
    assertStreams(new MonthlyScheduleEndpoint().getSchedulesJson("january-2026"));
    assertStreams(new ShiftSummary().getSummaryJson("2026-01-02T07"));
    assertStreams(new WeekSummary().getSummaryJson("2026-01-01T23"));
  }

  private static void assertStreams(Response response) {
    assertEquals(200, response.getStatus());
    assertInstanceOf(StreamingOutput.class, response.getEntity());
  }
}
