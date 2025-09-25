package org.jlab.btm.presentation.controller.rest;

import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.jlab.btm.business.service.RunService;
import org.jlab.btm.persistence.entity.Run;
import org.jlab.smoothness.business.util.TimeUtil;

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
    StreamingOutput stream =
        new StreamingOutput() {
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
                    .write("end", endDate)
                    .writeEnd();
              }
              if (previousRun != null) {
                String startDate = dateFormat.format(previousRun.getStartDay());
                String endDate = dateFormat.format(previousRun.getEndDay());

                gen.writeStartObject("previous")
                    .write("start", startDate)
                    .write("end", endDate)
                    .writeEnd();
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
