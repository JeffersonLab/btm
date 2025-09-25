package org.jlab.btm.presentation.controller.rest;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.io.StringWriter;
import org.jlab.smoothness.business.util.ObjectUtil;

/**
 * @author ryans
 */
public class JsonWebApplicationException extends WebApplicationException {

  public JsonWebApplicationException(Response.Status status, String message) {
    super(JsonWebApplicationException.createJsonResponse(status, message));
  }

  private static Response createJsonResponse(Response.Status status, String message) {
    JsonObject obj =
        Json.createObjectBuilder().add("message", ObjectUtil.coalesce(message, "")).build();
    StringWriter strWriter = new StringWriter();
    try (JsonWriter jsonWriter = Json.createWriter(strWriter)) {
      jsonWriter.writeObject(obj);
    }
    return Response.status(status).entity(strWriter.toString()).build();
  }
}
