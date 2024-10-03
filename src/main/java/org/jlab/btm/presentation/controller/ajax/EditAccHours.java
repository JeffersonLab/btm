package org.jlab.btm.presentation.controller.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBAccessException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.jlab.btm.business.service.CcAccHourService;
import org.jlab.btm.persistence.entity.CcAccHour;
import org.jlab.btm.presentation.util.BtmParamConverter;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.ExceptionUtil;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "EditAccHours",
    urlPatterns = {"/ajax/edit-acc-hours"})
public class EditAccHours extends HttpServlet {

  private static final Logger logger = Logger.getLogger(EditAccHours.class.getName());

  @EJB CcAccHourService accHourService;

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

      Date[] hourArray = BtmParamConverter.convertDayHourArray(request, "hour[]");
      Short[] upArray = ParamConverter.convertShortArray(request, "up[]", (short) 0);
      Short[] sadArray = ParamConverter.convertShortArray(request, "sad[]", (short) 0);
      Short[] downArray = ParamConverter.convertShortArray(request, "down[]", (short) 0);
      Short[] studiesArray = ParamConverter.convertShortArray(request, "studies[]", (short) 0);
      Short[] restoreArray = ParamConverter.convertShortArray(request, "restore[]", (short) 0);
      Short[] accArray = ParamConverter.convertShortArray(request, "acc[]", (short) 0);

      accHourService.editAccHours(
          hourArray, upArray, sadArray, downArray, studiesArray, restoreArray, accArray);
    } catch (NumberFormatException e) {
      logger.log(Level.WARNING, "Unable to edit accelerator availability hours", e);
      errorReason = "Numeric field out of range or not a number";
    } catch (EJBAccessException e) {
      logger.log(
          Level.WARNING,
          "Unable to edit accelerator availability hours due to access exception",
          e);
      errorReason = "Access Denied";
    } catch (UserFriendlyException e) {
      logger.log(Level.INFO, "Unable to edit accelerator availability hours: {0}", e.getMessage());
      errorReason = e.getUserMessage();
    } catch (Exception e) {
      Throwable rootCause = ExceptionUtil.getRootCause(e);
      if (rootCause instanceof SQLException) {
        errorReason = "Database exception";
        logger.log(Level.SEVERE, "Unable to edit accelerator availability hours", e);
      } else if (rootCause instanceof ConstraintViolationException) {
        ConstraintViolationException cve = (ConstraintViolationException) rootCause;
        Set<ConstraintViolation<?>> violations = cve.getConstraintViolations();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH");
        errorReason = "";
        for (ConstraintViolation<?> violation : violations) {
          Object bean = violation.getRootBean();
          if (bean instanceof CcAccHour) {
            CcAccHour hour = (CcAccHour) bean;
            String msg =
                "Hour " + dateFormat.format(hour.getDayAndHour()) + " " + violation.getMessage();
            errorReason = errorReason + msg + ", ";
          }
        }

        if (errorReason.length() == 0) {
          errorReason = "Constraint violation";
        } else {
          errorReason = errorReason.substring(0, errorReason.length() - 2); // Remove trailing comma
        }
        logger.log(
            Level.FINEST,
            "Unable to edit accelerator availability hours due to constraint violoation",
            e);

      } else {
        errorReason = "Something unexpected happened";
        logger.log(Level.SEVERE, "Unable to edit accelerator availability hours", e);
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
