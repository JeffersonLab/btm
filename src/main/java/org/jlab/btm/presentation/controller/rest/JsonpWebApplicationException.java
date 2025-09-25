package org.jlab.btm.presentation.controller.rest;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jlab.smoothness.business.util.ObjectUtil;

/**
 * @author ryans
 */
public class JsonpWebApplicationException extends WebApplicationException {

  public JsonpWebApplicationException(Response.Status status, String message, String callback) {
    super(JsonpWebApplicationException.createJsonPResponse(status, message, callback));
  }

  private static Response createJsonPResponse(
      Response.Status status, String message, String callback) {
    JsonObjectBuilder builder =
        Json.createObjectBuilder().add("message", ObjectUtil.coalesce(message, ""));
    return Response.status(status)
        .entity(callback + "(" + builder.build().toString() + ");")
        .build();
  }
}
