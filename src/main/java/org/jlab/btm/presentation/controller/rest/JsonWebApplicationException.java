package org.jlab.btm.presentation.controller.rest;

import org.jlab.smoothness.business.util.ObjectUtil;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.StringWriter;

/**
 * @author ryans
 */
public class JsonWebApplicationException extends WebApplicationException {

    public JsonWebApplicationException(Response.Status status, String message) {
        super(JsonWebApplicationException.createJsonResponse(status, message));
    }

    private static Response createJsonResponse(Response.Status status, String message) {
        JsonObject obj = Json.createObjectBuilder().add("message", ObjectUtil.coalesce(message, "")).build();
        StringWriter strWriter = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriter(strWriter)) {
            jsonWriter.writeObject(obj);
        }
        return Response.status(status).entity(strWriter.toString()).build();
    }
}
