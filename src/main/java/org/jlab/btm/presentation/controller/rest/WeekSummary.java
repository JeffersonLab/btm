package org.jlab.btm.presentation.controller.rest;

import java.io.OutputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.jlab.btm.business.service.CcAccHourService;
import org.jlab.btm.business.service.ExpHourService;
import org.jlab.btm.business.service.PdShiftPlanService;
import org.jlab.btm.persistence.projection.CcAccSum;
import org.jlab.btm.persistence.projection.ExpHourTotals;
import org.jlab.btm.persistence.projection.PdAccSum;
import org.jlab.btm.presentation.util.BtmParamConverter;
import org.jlab.smoothness.business.util.ObjectUtil;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * @author ryans
 */
@Path("week-summary")
public class WeekSummary {

  private ExpHourService lookupHourService() {
    try {
      InitialContext ic = new InitialContext();
      return (ExpHourService) ic.lookup("java:global/btm/ExpHourService");
    } catch (NamingException e) {
      throw new RuntimeException("Unable to obtain EJB", e);
    }
  }

  private CcAccHourService lookupOpAccHourService() {
    try {
      InitialContext ic = new InitialContext();
      return (CcAccHourService) ic.lookup("java:global/btm/CcAccHourService");
    } catch (NamingException e) {
      throw new RuntimeException("Unable to obtain EJB", e);
    }
  }

  private PdShiftPlanService lookupPdShiftService() {
    try {
      InitialContext ic = new InitialContext();
      return (PdShiftPlanService) ic.lookup("java:global/btm/PdShiftPlanService");
    } catch (NamingException e) {
      throw new RuntimeException("Unable to obtain EJB", e);
    }
  }

  @GET
  @Produces("application/json")
  public Response getSummaryJson(@QueryParam("week-start") final String ccShiftStart) {
    StreamingOutput stream =
        new StreamingOutput() {
          @Override
          public void write(OutputStream out) {
            try (JsonGenerator gen = Json.createGenerator(out)) {
              ExpHourService expHourService = lookupHourService();
              CcAccHourService ccAccHourService = lookupOpAccHourService();
              PdShiftPlanService pdShiftService = lookupPdShiftService();

              Date ccStart;

              try {
                ccStart = BtmParamConverter.convertDayAndHour(ccShiftStart);
              } catch (ParseException e) {
                throw new JsonWebApplicationException(
                    Response.Status.BAD_REQUEST, "unrecognized date format");
              }

              if (ccStart == null) {
                throw new JsonWebApplicationException(
                    Response.Status.BAD_REQUEST,
                    "Parameter week-start (Crew Chief Shift Start) in format yyyy-mm-ddThh is missing");
              }

              if (!TimeUtil.isCrewChiefShiftStart(ccStart)) {
                throw new JsonWebApplicationException(
                    Response.Status.BAD_REQUEST,
                    "week start hour must be one of 23, 7, 15 to correspond to a crew chief shift start");
              }

              Date ccEnd = TimeUtil.addDays(ccStart, 7);
              Date expStart = TimeUtil.addHours(ccStart, 1);

              Long[] hallScheduledArray = pdShiftService.findScheduledHallTime(ccStart, ccEnd);

              PdAccSum pdSum = pdShiftService.findSummary(ccStart, ccEnd);

              List<ExpHourTotals> expHourTotals =
                  expHourService.findExpHallHourTotals(ccStart, ccEnd);

              CcAccSum accTotals = ccAccHourService.findSummary(ccStart, ccEnd);

              Map<Hall, Long> hallScheduledMap = new HashMap<>();

              if (hallScheduledArray != null && hallScheduledArray.length == 4) {
                hallScheduledMap.put(Hall.A, hallScheduledArray[0]);
                hallScheduledMap.put(Hall.B, hallScheduledArray[1]);
                hallScheduledMap.put(Hall.C, hallScheduledArray[2]);
                hallScheduledMap.put(Hall.D, hallScheduledArray[3]);
              }

              Map<String, Long> accScheduledMap = new HashMap<>();

              if (pdSum != null) {
                accScheduledMap.put("studies", pdSum.getStudiesSeconds());
                accScheduledMap.put("restore", pdSum.getRestoreSeconds());
                accScheduledMap.put("acc", pdSum.getAccSeconds());
                accScheduledMap.put("sad", pdSum.getOffSeconds());
              }

              gen.writeStartObject()
                  .write("acc-physics-seconds", ObjectUtil.coalesce(accTotals.getUpSeconds(), 0L))
                  .write(
                      "acc-studies-seconds", ObjectUtil.coalesce(accTotals.getStudiesSeconds(), 0L))
                  .write(
                      "acc-restore-seconds", ObjectUtil.coalesce(accTotals.getRestoreSeconds(), 0L))
                  .write("acc-acc-seconds", ObjectUtil.coalesce(accTotals.getAccSeconds(), 0L))
                  .write("acc-down-seconds", ObjectUtil.coalesce(accTotals.getDownSeconds(), 0L))
                  .write("acc-sad-seconds", ObjectUtil.coalesce(accTotals.getSadSeconds(), 0L))
                  .write(
                      "plan-studies-seconds",
                      ObjectUtil.coalesce(accScheduledMap.get("studies"), 0L))
                  .write(
                      "plan-restore-seconds",
                      ObjectUtil.coalesce(accScheduledMap.get("restore"), 0L))
                  .write("plan-acc-seconds", ObjectUtil.coalesce(accScheduledMap.get("acc"), 0L))
                  .write("plan-sad-seconds", ObjectUtil.coalesce(accScheduledMap.get("sad"), 0L));
              gen.writeStartArray("halls");
              for (ExpHourTotals totals : expHourTotals) {

                gen.writeStartObject()
                    .write("hall", totals.getHall().name().toLowerCase())
                    .write(
                        "pd-scheduled-seconds",
                        ObjectUtil.coalesce(hallScheduledMap.get(totals.getHall()), 0L))
                    .write("abu-seconds", ObjectUtil.coalesce(totals.getAbuSeconds(), 0))
                    .write("banu-seconds", ObjectUtil.coalesce(totals.getBanuSeconds(), 0))
                    .write("er-seconds", ObjectUtil.coalesce(totals.getErSeconds(), 0))
                    .write("pcc-seconds", ObjectUtil.coalesce(totals.getPccSeconds(), 0))
                    .write("ued-seconds", ObjectUtil.coalesce(totals.getUedSeconds(), 0))
                    .writeEnd();
              }
              gen.writeEnd(); // Close Array
              gen.writeEnd(); // Close Object
            }
          }
        };
    return Response.ok(stream).build();
  }
}
