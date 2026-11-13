package org.jlab.btm.presentation.controller.ajax;

import jakarta.ejb.EJBAccessException;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.ExceptionUtil;

/**
 * A change from Smoothness's editable row table, answered in the JSON it expects: any 2xx status is
 * success, and a failure carries its reason as "error".
 */
abstract class ChangeServlet extends HttpServlet {

  private static final Logger LOGGER = Logger.getLogger(ChangeServlet.class.getName());

  /**
   * Makes the change the request asks for.
   *
   * @param request The request
   * @throws UserFriendlyException If the change isn't valid
   */
  abstract void change(HttpServletRequest request) throws UserFriendlyException;

  /**
   * @return What is changed, for the log, such as "the state timeline"
   */
  abstract String subject();

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String error = null;
    int status = HttpServletResponse.SC_OK;

    try {
      if (request.getRequestedSessionId() != null && !request.isRequestedSessionIdValid()) {
        throw new UserFriendlyException(
            "Your session has expired.  Please reload the page and relogin.");
      }

      change(request);
    } catch (UserFriendlyException e) {
      LOGGER.log(Level.INFO, "Unable to change {0}: {1}", new Object[] {subject(), e.getMessage()});
      error = e.getUserMessage();
      status = HttpServletResponse.SC_BAD_REQUEST;
    } catch (EJBAccessException e) {
      LOGGER.log(Level.WARNING, "Unable to change " + subject() + " due to access exception", e);
      error = "Access Denied";
      status = HttpServletResponse.SC_FORBIDDEN;
    } catch (RuntimeException e) {
      Throwable rootCause = ExceptionUtil.getRootCause(e);

      if (rootCause instanceof UserFriendlyException) {
        error = ((UserFriendlyException) rootCause).getUserMessage();
        status = HttpServletResponse.SC_BAD_REQUEST;
      } else {
        LOGGER.log(Level.SEVERE, "Unable to change " + subject(), e);
        error = "Something unexpected happened";
        status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
      }
    }

    JsonObjectBuilder json = Json.createObjectBuilder().add("stat", error == null ? "ok" : "fail");

    if (error != null) {
      json.add("error", error);
    }

    response.setStatus(status);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().write(json.build().toString());
  }
}
