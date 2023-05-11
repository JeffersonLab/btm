package org.jlab.btm.presentation.controller.rest;

import org.jlab.btm.business.service.ExpHallShiftPurposeService;
import org.jlab.btm.business.service.MonthlyScheduleService;
import org.jlab.btm.persistence.entity.ExpHallShiftPurpose;
import org.jlab.btm.persistence.entity.MonthlySchedule;
import org.jlab.btm.persistence.entity.ScheduleDay;
import org.jlab.btm.presentation.util.BtmParamConverter;

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
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author ryans
 */
@Path("schedule")
public class ScheduleEndpoint {

    private MonthlyScheduleService lookupScheduleService() {
        try {
            InitialContext ic = new InitialContext();
            return (MonthlyScheduleService) ic.lookup("java:global/btm/MonthlyScheduleService");
        } catch (NamingException e) {
            throw new RuntimeException("Unable to obtain EJB", e);
        }
    }

    private ExpHallShiftPurposeService lookupPurposeService() {
        try {
            InitialContext ic = new InitialContext();
            return (ExpHallShiftPurposeService) ic.lookup("java:global/btm/ExpHallShiftPurposeService");
        } catch (NamingException e) {
            throw new RuntimeException("Unable to obtain EJB", e);
        }
    }

    @GET
    @Produces("application/json")
    public Response getSchedulesJson(
            @QueryParam("start") final String startStr,
            @QueryParam("end") final String endStr
    ) {
        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream out) {
                try (JsonGenerator gen = Json.createGenerator(out)) {
                    MonthlyScheduleService scheduleService = lookupScheduleService();
                    ExpHallShiftPurposeService purposeService = lookupPurposeService();

                    Date start;
                    Date end;

                    try {
                        start = BtmParamConverter.convertISO8601Date(startStr);
                        end = BtmParamConverter.convertISO8601Date(endStr);
                    } catch (ParseException e) {
                        throw new JsonWebApplicationException(Response.Status.BAD_REQUEST, "unrecognized date format");
                    }

                    if(start == null || end == null)  {
                        throw new JsonWebApplicationException(Response.Status.BAD_REQUEST, "start and end dates in ISO8601 format are required");
                    }

                    List<MonthlySchedule> monthlySchedules = scheduleService.findMostRecentPublishedInDateRange(start, end);
                    List<ScheduleDay> scheduleDays = scheduleService.filterScheduleDaysFromRange(monthlySchedules, start, end);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                    Map<Integer, ExpHallShiftPurpose> purposeMap = purposeService.findPurposeByIdMap();

                    gen.writeStartArray();
                    for (ScheduleDay day : scheduleDays) {

                        String hallAProgram = "OFF";
                        String hallBProgram = "OFF";
                        String hallCProgram = "OFF";
                        String hallDProgram = "OFF";

                        if(day.getHallAProgramId() != null) {
                            ExpHallShiftPurpose purpose = purposeMap.get(day.getHallAProgramId());
                            if(purpose != null) {
                                hallAProgram = purpose.getName();
                            }
                        }

                        if(day.getHallBProgramId() != null) {
                            ExpHallShiftPurpose purpose = purposeMap.get(day.getHallBProgramId());
                            if(purpose != null) {
                                hallBProgram = purpose.getName();
                            }
                        }

                        if(day.getHallCProgramId() != null) {
                            ExpHallShiftPurpose purpose = purposeMap.get(day.getHallCProgramId());
                            if(purpose != null) {
                                hallCProgram = purpose.getName();
                            }
                        }

                        if(day.getHallDProgramId() != null) {
                            ExpHallShiftPurpose purpose = purposeMap.get(day.getHallDProgramId());
                            if(purpose != null) {
                                hallDProgram = purpose.getName();
                            }
                        }

                        gen.writeStartObject()
                                .write("date", dateFormat.format(day.getDayMonthYear()))
                                .write("acc-program", day.getAccProgram())
                                .write("halla-program", hallAProgram)
                                .write("hallb-program", hallBProgram)
                                .write("hallc-program", hallCProgram)
                                .write("halld-program", hallDProgram)
                                .writeEnd();
                    }
                    gen.writeEnd();
                }
            }
        };
        return Response.ok(stream).build();
    }
}
