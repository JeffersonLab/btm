package org.jlab.btm.presentation.controller.rest;

import org.jlab.btm.business.service.MonthlyScheduleService;
import org.jlab.btm.business.service.RunService;
import org.jlab.btm.business.util.DateRange;
import org.jlab.btm.persistence.entity.MonthlySchedule;
import org.jlab.btm.persistence.entity.Run;
import org.jlab.btm.presentation.util.BtmParamConverter;
import org.jlab.smoothness.business.util.TimeUtil;

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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author ryans
 */
@Path("runs")
public class Runs {

    private RunService lookupRunService() {
        try {
            InitialContext ic = new InitialContext();
            return (RunService) ic.lookup("java:global/btm/RunService");
        } catch (NamingException e) {
            throw new RuntimeException("Unable to obtain EJB", e);
        }
    }

    @GET
    @Produces("application/json")
    public Response getRunsJson() {
        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream out) {
                try (JsonGenerator gen = Json.createGenerator(out)) {
                    RunService runService = lookupRunService();

                    Run currentRun = runService.getCurrentRun();

                    Run previousRun = runService.getRunBefore(currentRun);

                    // ISO 8601
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                    gen.writeStartObject();
                    if (currentRun != null) {
                        String startDate = dateFormat.format(currentRun.getStartDay());
                        String endDate = dateFormat.format(currentRun.getEndDay());

                        gen.writeStartObject("current")
                                .write("start", startDate)
                                .write("end", endDate).writeEnd();
                    }
                    if (previousRun != null) {
                        String startDate = dateFormat.format(previousRun.getStartDay());
                        String endDate = dateFormat.format(previousRun.getEndDay());

                        gen.writeStartObject("previous")
                                .write("start", startDate)
                                .write("end", endDate).writeEnd();
                    }
                    gen.writeEnd();
                }
            }
        };
        Date midnight = TimeUtil.startOfDay(new Date(), Calendar.getInstance());
        midnight = TimeUtil.addDays(midnight, 1);
        return Response.ok(stream).expires(midnight).build();
    }
}
