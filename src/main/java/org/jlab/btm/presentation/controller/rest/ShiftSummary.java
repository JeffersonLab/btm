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
import org.jlab.btm.business.service.*;
import org.jlab.btm.persistence.entity.CcShift;
import org.jlab.btm.persistence.entity.ExpShift;
import org.jlab.btm.persistence.entity.PdShiftPlan;
import org.jlab.btm.persistence.projection.AcceleratorShiftAvailability;
import org.jlab.btm.persistence.projection.CcHallShiftAvailability;
import org.jlab.btm.persistence.projection.CcHallShiftTotals;
import org.jlab.btm.persistence.projection.ExpShiftTotals;
import org.jlab.btm.presentation.util.BtmParamConverter;
import org.jlab.smoothness.business.util.ObjectUtil;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * @author ryans
 */
@Path("shift-summary")
public class ShiftSummary {

  private ExpHourService lookupHourService() {
    try {
      InitialContext ic = new InitialContext();
      return (ExpHourService) ic.lookup("java:global/btm/ExpHourService");
    } catch (NamingException e) {
      throw new RuntimeException("Unable to obtain EJB", e);
    }
  }

  private ExpShiftService lookupExpShiftService() {
    try {
      InitialContext ic = new InitialContext();
      return (ExpShiftService) ic.lookup("java:global/btm/ExpShiftService");
    } catch (NamingException e) {
      throw new RuntimeException("Unable to obtain EJB", e);
    }
  }

  private CcShiftService lookupOpShiftService() {
    try {
      InitialContext ic = new InitialContext();
      return (CcShiftService) ic.lookup("java:global/btm/CcShiftService");
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

  private CcHallHourService lookupOpHallHourService() {
    try {
      InitialContext ic = new InitialContext();
      return (CcHallHourService) ic.lookup("java:global/btm/CcHallHourService");
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
  public Response getSummaryJson(@QueryParam("shift-start") final String ccShiftStart) {
    StreamingOutput stream =
        new StreamingOutput() {
          @Override
          public void write(OutputStream out) {
            try (JsonGenerator gen = Json.createGenerator(out)) {
              ExpHourService expHourService = lookupHourService();
              ExpShiftService expShiftService = lookupExpShiftService();
              CcShiftService ccShiftService = lookupOpShiftService();
              CcAccHourService ccAccHourService = lookupOpAccHourService();
              CcHallHourService hallHourService = lookupOpHallHourService();
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
                    "Parameter shift-start (Crew Chief Shift Start) in format yyyy-mm-ddThh is missing");
              }

              if (!TimeUtil.isCrewChiefShiftStart(ccStart)) {
                throw new JsonWebApplicationException(
                    Response.Status.BAD_REQUEST, "shift start hour must be one of 23, 7, 15");
              }

              PdShiftPlan plan = pdShiftService.findInDatabase(ccStart);

              Date ccEnd = TimeUtil.calculateCrewChiefShiftEndDayAndHour(ccStart);
              Date expStart = TimeUtil.addHours(ccStart, 1);

              List<ExpShiftTotals> expHourList =
                  expHourService.findExpHallShiftTotals(ccStart, ccEnd);
              List<ExpShift> expShiftList =
                  expShiftService.findByShiftStartAndLoadPurpose(expStart);
              Map<Hall, ExpShift> shiftMap = expShiftService.getMap(expShiftList);
              CcShift ccShift = ccShiftService.findInDatabase(ccStart);
              AcceleratorShiftAvailability accAvail =
                  ccAccHourService.getAcceleratorAvailability(ccStart, ccEnd, false, plan);
              List<CcHallShiftAvailability> hallAvailabilityList =
                  hallHourService.getHallAvailablilityList(ccStart, ccEnd, false, plan);

              String crewChief = null;
              String operators = null;
              String accProgram = null;
              String pd = null;
              String comments = null;

              Map<Hall, Integer> scheduleMap = new HashMap<>();

              if (plan != null) {
                Integer scheduled;
                scheduled = plan.getHallAUpSeconds();
                scheduleMap.put(Hall.A, scheduled);
                scheduled = plan.getHallBUpSeconds();
                scheduleMap.put(Hall.B, scheduled);
                scheduled = plan.getHallCUpSeconds();
                scheduleMap.put(Hall.C, scheduled);
                scheduled = plan.getHallDUpSeconds();
                scheduleMap.put(Hall.D, scheduled);
              }

              if (ccShift != null) {
                crewChief = ccShift.getCrewChief();
                operators = ccShift.getOperators();
                accProgram = ccShift.getProgram();
                pd = ccShift.getProgramDeputy();
                comments = ccShift.getRemark();
              }

              gen.writeStartObject()
                  .write("crew-chief", ObjectUtil.coalesce(crewChief, ""))
                  .write("operators", ObjectUtil.coalesce(operators, ""))
                  .write("accelerator-program", ObjectUtil.coalesce(accProgram, ""))
                  .write("program-deputy", ObjectUtil.coalesce(pd, ""))
                  .write("comments", ObjectUtil.coalesce(comments, ""))
                  .write(
                      "acc-physics-seconds",
                      ObjectUtil.coalesce(accAvail.getShiftTotals().getUpSeconds(), 0))
                  .write(
                      "acc-studies-seconds",
                      ObjectUtil.coalesce(accAvail.getShiftTotals().getStudiesSeconds(), 0))
                  .write(
                      "acc-restore-seconds",
                      ObjectUtil.coalesce(accAvail.getShiftTotals().getRestoreSeconds(), 0))
                  .write(
                      "acc-acc-seconds",
                      ObjectUtil.coalesce(accAvail.getShiftTotals().getAccSeconds(), 0))
                  .write(
                      "acc-down-seconds",
                      ObjectUtil.coalesce(accAvail.getShiftTotals().getDownSeconds(), 0))
                  .write(
                      "acc-sad-seconds",
                      ObjectUtil.coalesce(accAvail.getShiftTotals().getSadSeconds(), 0))
                  .write(
                      "plan-physics-seconds",
                      ObjectUtil.coalesce(accAvail.getPdShiftTotals().getUpSeconds(), 0))
                  .write(
                      "plan-studies-seconds",
                      ObjectUtil.coalesce(accAvail.getPdShiftTotals().getStudiesSeconds(), 0))
                  .write(
                      "plan-restore-seconds",
                      ObjectUtil.coalesce(accAvail.getPdShiftTotals().getRestoreSeconds(), 0))
                  .write(
                      "plan-acc-seconds",
                      ObjectUtil.coalesce(accAvail.getPdShiftTotals().getAccSeconds(), 0))
                  .write(
                      "plan-down-seconds",
                      ObjectUtil.coalesce(accAvail.getPdShiftTotals().getDownSeconds(), 0))
                  .write(
                      "plan-sad-seconds",
                      ObjectUtil.coalesce(accAvail.getPdShiftTotals().getSadSeconds(), 0));
              gen.writeStartArray("halls");
              int i = 0;
              for (ExpShiftTotals totals : expHourList) {
                ExpShift expShift = shiftMap.get(totals.getHall());
                CcHallShiftAvailability opAvail = hallAvailabilityList.get(i++);
                CcHallShiftTotals opTotals = opAvail.getShiftTotals();

                String hallProgram = null;

                if (expShift != null) {
                  hallProgram = expShift.getExpProgram().getName();
                }

                gen.writeStartObject()
                    .write("hall", totals.getHall().name().toLowerCase())
                    .write("hall-program", ObjectUtil.coalesce(hallProgram, ""))
                    .write(
                        "pd-scheduled-seconds",
                        ObjectUtil.coalesce(scheduleMap.get(totals.getHall()), 0))
                    .write("abu-seconds", ObjectUtil.coalesce(totals.getAbuSeconds(), 0))
                    .write("banu-seconds", ObjectUtil.coalesce(totals.getBanuSeconds(), 0))
                    .write("up-seconds", ObjectUtil.coalesce(opTotals.getUpSeconds(), 0))
                    .write("tune-seconds", ObjectUtil.coalesce(opTotals.getTuneSeconds(), 0))
                    .write("bnr-seconds", ObjectUtil.coalesce(opTotals.getBnrSeconds(), 0))
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
