package org.jlab.btm.presentation.controller.rest;

import org.jlab.btm.business.service.MonthlyScheduleService;
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
import java.util.Date;
import java.util.List;

/**
 * @author ryans
 */
@Path("scheduled-count")
public class ScheduledCountEndpoint {

    private MonthlyScheduleService lookupScheduleService() {
        try {
            InitialContext ic = new InitialContext();
            return (MonthlyScheduleService) ic.lookup("java:global/btm/MonthlyScheduleService");
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

                    long count = 0;

                    for (ScheduleDay day : scheduleDays) {
                        if(!"OFF".equals(day.getAccProgram()) ) {
                                //&& !"UNKNOWN".equals(day.getAccProgram())) {
                            count++;
                        }
                    }

                    gen.writeStartObject()
                            .write("count", count)
                            .writeEnd();
                }
            }
        };
        return Response.ok(stream).build();
    }
}
