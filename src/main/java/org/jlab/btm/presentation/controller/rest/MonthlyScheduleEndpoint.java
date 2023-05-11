package org.jlab.btm.presentation.controller.rest;

import org.jlab.btm.business.service.MonthlyScheduleService;
import org.jlab.btm.persistence.entity.MonthlySchedule;
import org.jlab.btm.persistence.entity.ScheduleDay;
import org.jlab.btm.presentation.util.BtmParamConverter;
import org.jlab.smoothness.presentation.util.ParamConverter;

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

/**
 * @author ryans
 */
@Path("monthly-schedule")
public class MonthlyScheduleEndpoint {

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
            @QueryParam("month") final String month
    ) {
        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream out) {
                try (JsonGenerator gen = Json.createGenerator(out)) {
                    MonthlyScheduleService scheduleService = lookupScheduleService();

                    Date monthDate;

                    try {
                        monthDate = BtmParamConverter.convertMonthAndYear(month);
                    } catch (ParseException e) {
                        throw new JsonWebApplicationException(Response.Status.BAD_REQUEST, "unrecognized date format");
                    }

                    List<MonthlySchedule> scheduleList = scheduleService.findAll(monthDate);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

                    gen.writeStartArray();
                    for (MonthlySchedule schedule : scheduleList) {
                        String publishedDate = "";

                        if (schedule.getPublishedDate() != null) {
                            publishedDate = dateFormat.format(schedule.getPublishedDate());
                        }

                        gen.writeStartObject()
                                //.write("id", schedule.getMonthlyScheduleId())
                                //.write("month", month)
                                .write("version", schedule.getVersion())
                                .write("published", publishedDate).writeEnd();
                    }
                    gen.writeEnd();
                }
            }
        };
        return Response.ok(stream).build();
    }
}
