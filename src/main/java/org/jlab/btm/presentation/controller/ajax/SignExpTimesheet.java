package org.jlab.btm.presentation.controller.ajax;

import org.jlab.btm.business.service.*;
import org.jlab.btm.business.util.BtmTimeUtil;
import org.jlab.btm.persistence.entity.CcHallHour;
import org.jlab.btm.persistence.entity.CcShift;
import org.jlab.btm.persistence.entity.PdShiftPlan;
import org.jlab.btm.persistence.enumeration.DurationUnits;
import org.jlab.btm.persistence.enumeration.TimesheetType;
import org.jlab.btm.persistence.projection.AcceleratorShiftAvailability;
import org.jlab.btm.persistence.projection.CcHallShiftAvailability;
import org.jlab.btm.persistence.projection.MultiplicityShiftAvailability;
import org.jlab.btm.presentation.util.BtmParamConverter;
import org.jlab.btm.presentation.util.InternalHtmlRequestExecutor;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.ExceptionUtil;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.enumeration.Hall;
import org.jlab.smoothness.persistence.enumeration.Shift;

import javax.ejb.EJB;
import javax.ejb.EJBAccessException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ryans
 */
@WebServlet(name = "SignExpTimesheet", urlPatterns = {"/ajax/sign-exp-timesheet"})
public class SignExpTimesheet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(
            SignExpTimesheet.class.getName());

    @EJB
    ExpSignatureService signatureService;
    @EJB
    LogbookService logbookService;

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
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
            Hall hall = BtmParamConverter.convertHall(request, "hall");

            TimesheetType type;
            switch(hall) {
                case A:
                    type = TimesheetType.EA;
                    break;
                case B:
                    type = TimesheetType.EB;
                    break;
                case C:
                    type = TimesheetType.EC;
                    break;
                case D:
                    type = TimesheetType.ED;
                    break;
                default:
                    throw new UserFriendlyException("hall must not be empty");
            }

            if (startHour == null || !TimeUtil.isExperimenterShiftStart(startHour)) {
                throw new UserFriendlyException("Shift Start hour must be one of 0, 8, 16");
            }

            signatureService.signTimesheet(hall, startHour);


            /*Try to create a log entry*/
            try {
                Shift shift = BtmTimeUtil.calculateExperimenterShift(startHour);
                Date endHour = TimeUtil.calculateExperimenterShiftEndDayAndHour(startHour);

                signatureService.populateRequestAttributes(request, hall, startHour, endHour);

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
                SimpleDateFormat urlDateFormat = new SimpleDateFormat("dd-MMM-yyyy");

                String subject = "Experimenter Timesheet Summary: " + dateFormat.format(endHour)
                        + " " + shift;

                String frontEndUrl = System.getenv("FRONTEND_SERVER_URL");

                String timesheetUrl = frontEndUrl + "/btm/timesheet/" + type.toString().toLowerCase() + "/"
                        + urlDateFormat.format(endHour).toLowerCase() + "/"
                        + shift.name().toLowerCase() + "/hours";

                request.setAttribute("durationUnits", DurationUnits.HOURS);
                request.setAttribute("title", subject);
                request.setAttribute("timesheetUrl", timesheetUrl);

                String html = InternalHtmlRequestExecutor.execute(
                        "/WEB-INF/views/log-exp-timesheet.jsp", request, response);

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
            logger.log(Level.WARNING,
                    "Unable to sign timesheet due to access exception", e);
            errorReason = "Access Denied";
        } catch (UserFriendlyException e) {
            logger.log(Level.INFO,
                    "Unable to sign timesheet: {0}",
                    e.getMessage());
            errorReason = e.getMessage();
        } catch (Exception e) {
            logger.log(Level.SEVERE,
                    "Unable to sign timesheet", e);
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
            xml = "<response><span class=\"status\">Error</span><span "
                    + "class=\"reason\">" + errorReason + "</span></response>";
        }

        pw.write(xml);

        pw.flush();

        boolean error = pw.checkError();

        if (error) {
            logger.log(Level.SEVERE, "PrintWriter Error");
        }
    }
}
