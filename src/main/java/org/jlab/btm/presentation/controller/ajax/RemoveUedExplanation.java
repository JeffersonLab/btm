package org.jlab.btm.presentation.controller.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBAccessException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import org.jlab.btm.business.service.ExpUedExplanationService;
import org.jlab.btm.presentation.util.BtmParamConverter;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.ExceptionUtil;
import org.jlab.smoothness.persistence.enumeration.Hall;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "RemoveUedExplanation",
    urlPatterns = {"/ajax/remove-ued-explanation"})
public class RemoveUedExplanation extends HttpServlet {

  private static final Logger logger = Logger.getLogger(RemoveUedExplanation.class.getName());

  @EJB ExpUedExplanationService explanationService;

  /**
   * Handles the HTTP <code>POST</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String errorReason = null;

    try {
      if (request.getRequestedSessionId() != null && !request.isRequestedSessionIdValid()) {
        throw new UserFriendlyException(
            "Your session has expired.  Please reload the page and relogin.");
      }

      Hall hall = BtmParamConverter.convertHall(request, "hall");
      Date startDayAndHour = BtmParamConverter.convertDayAndHour(request, "startDayAndHour");
      BigInteger explanationId = ParamConverter.convertBigInteger(request, "explanationId");

      explanationService.remove(hall, startDayAndHour, explanationId);
    } catch (EJBAccessException e) {
      logger.log(Level.WARNING, "Unable to add explanation due to access exception", e);
      errorReason = "Access Denied";
    } catch (UserFriendlyException e) {
      logger.log(Level.INFO, "Unable to add explanation: {0}", e.getMessage());
      errorReason = e.getUserMessage();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Unable to add explanation", e);
      Throwable rootCause = ExceptionUtil.getRootCause(e);
      if (rootCause instanceof SQLException) {
        errorReason = "Database exception";
      } else if (rootCause instanceof ConstraintViolationException) {
        errorReason = "Constraint violation";
      } else {
        errorReason = "Something unexpected happened";
      }
    }

    response.setContentType("text/xml");

    PrintWriter pw = response.getWriter();

    String xml;

    if (errorReason == null) {
      xml = "<response><span class=\"status\">Success</span></response>";
    } else {
      xml =
          "<response><span class=\"status\">Error</span><span "
              + "class=\"reason\">"
              + errorReason
              + "</span></response>";
    }

    pw.write(xml);

    pw.flush();

    boolean error = pw.checkError();

    if (error) {
      logger.log(Level.SEVERE, "PrintWriter Error");
    }
  }
}
