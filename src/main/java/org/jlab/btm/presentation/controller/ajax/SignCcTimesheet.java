package org.jlab.btm.presentation.controller.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
import org.jlab.btm.business.service.*;
import org.jlab.btm.persistence.enumeration.DurationUnits;
import org.jlab.btm.presentation.util.BtmParamConverter;
import org.jlab.btm.presentation.util.InternalHtmlRequestExecutor;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.ExceptionUtil;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.enumeration.Shift;

/**
 * @author ryans
 */
@WebServlet(
    name = "SignCcTimesheet",
    urlPatterns = {"/ajax/sign-cc-timesheet"})
public class SignCcTimesheet extends HttpServlet {

  private static final Logger logger = Logger.getLogger(SignCcTimesheet.class.getName());

  @EJB CcSignatureService signatureService;
  @EJB CcAccHourService accHourService;
  @EJB CcHallHourService hallHourService;
  @EJB CcMultiplicityHourService multiplicityHourService;
  @EJB CcShiftService shiftService;
  @EJB LogbookService logbookService;
  @EJB PdShiftPlanService planService;

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

      Date startHour = BtmParamConverter.convertDayAndHour(request, "startDayAndHour");

      if (startHour == null || !TimeUtil.isCrewChiefShiftStart(startHour)) {
        throw new UserFriendlyException("Shift Start hour must be one of 23, 7, 15");
      }

      signatureService.signTimesheet(startHour);

      /*Try to create a log entry*/
      try {
        Shift shift = TimeUtil.calculateCrewChiefShift(startHour);
        Date endHour = TimeUtil.calculateCrewChiefShiftEndDayAndHour(startHour);
        Date startOfNextShift = TimeUtil.addHours(endHour, 1);

        signatureService.populateRequestAttributes(request, startHour, endHour, startOfNextShift);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        SimpleDateFormat urlDateFormat = new SimpleDateFormat("dd-MMM-yyyy");

        // Use endHour not startHour since in case of OWL shift startHour the day is yesterday
        String subject =
            "Crew Chief Timesheet Summary: " + dateFormat.format(endHour) + " " + shift;

        String frontEndUrl = System.getenv("FRONTEND_SERVER_URL");

        String timesheetUrl =
            frontEndUrl
                + "/btm/timesheet/cc/"
                + urlDateFormat.format(endHour).toLowerCase()
                + "/"
                + shift.name().toLowerCase()
                + "/hours";

        request.setAttribute("durationUnits", DurationUnits.HOURS);
        request.setAttribute("title", subject);
        request.setAttribute("timesheetUrl", timesheetUrl);

        String html =
            InternalHtmlRequestExecutor.execute(
                "/WEB-INF/views/log-cc-timesheet.jsp", request, response);

        String[] tokens = html.split("<body>");

        if (tokens.length == 2) {
          tokens = tokens[1].split("</body>");
        }

        if (tokens.length == 2) {
          html = tokens[0];
        }

        logbookService.createCrewChiefTimesheetEntry(subject, html);

      } catch (Exception e) {
        logger.log(Level.WARNING, "Unable to create log entry", e);
      }
    } catch (EJBAccessException e) {
      logger.log(Level.WARNING, "Unable to sign timesheet due to access exception", e);
      errorReason = "Access Denied";
    } catch (UserFriendlyException e) {
      logger.log(Level.INFO, "Unable to sign timesheet: {0}", e.getMessage());
      errorReason = e.getUserMessage();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Unable to sign timesheet", e);
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
