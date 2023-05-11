package org.jlab.btm.presentation.controller.ajax;

import org.jlab.btm.business.service.OpHallHourService;
import org.jlab.btm.persistence.entity.OpHallHour;
import org.jlab.btm.presentation.util.BtmParamConverter;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.ExceptionUtil;
import org.jlab.smoothness.persistence.enumeration.Hall;
import org.jlab.smoothness.presentation.util.ParamConverter;

import javax.ejb.EJB;
import javax.ejb.EJBAccessException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ryans
 */
@WebServlet(name = "EditHallHours", urlPatterns = {"/ajax/edit-hall-hours"})
public class EditHallHours extends HttpServlet {

    private static final Logger logger = Logger.getLogger(
            EditHallHours.class.getName());

    @EJB
    OpHallHourService hallHourService;

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

            Date[] hourArray = BtmParamConverter.convertDayHourArray(request,
                    "hour[]");
            Short[] upArray = ParamConverter.convertShortArray(request,
                    "up[]", (short) 0);
            Short[] tuneArray = ParamConverter.convertShortArray(request,
                    "tune[]", (short) 0);
            Short[] bnrArray = ParamConverter.convertShortArray(request,
                    "bnr[]", (short) 0);
            Short[] downArray = ParamConverter.convertShortArray(request,
                    "down[]", (short) 0);
            Short[] offArray = ParamConverter.convertShortArray(request,
                    "off[]", (short) 0);
            Hall hall = BtmParamConverter.convertHall(request, "hall");

            hallHourService.editHallHours(hall, hourArray, upArray, tuneArray, bnrArray,
                    downArray, offArray);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Unable to edit hall availability hours", e);
            errorReason = "Numeric field out of range or not a number";
        } catch (EJBAccessException e) {
            logger.log(Level.WARNING,
                    "Unable to edit hall availability hours due to access exception", e);
            errorReason = "Access Denied";
        } catch (UserFriendlyException e) {
            logger.log(Level.INFO,
                    "Unable to edit hall availability hours: {0}",
                    e.getMessage());
            errorReason = e.getMessage();
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtil.getRootCause(e);
            if (rootCause instanceof SQLException) {
                errorReason = "Database exception";
                logger.log(Level.SEVERE,
                        "Unable to edit hall hours", e);
            } else if (rootCause instanceof ConstraintViolationException) {
                ConstraintViolationException cve = (ConstraintViolationException) rootCause;
                Set<ConstraintViolation<?>> violations = cve.getConstraintViolations();
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH");
                errorReason = "";
                for (ConstraintViolation<?> violation : violations) {
                    Object bean = violation.getRootBean();
                    if (bean instanceof OpHallHour) {
                        OpHallHour hour = (OpHallHour) bean;
                        String msg = "Hour " + dateFormat.format(hour.getDayAndHour()) + " "
                                + violation.getMessage();
                        errorReason = errorReason + msg + ", ";
                    }
                }

                if (errorReason.length() == 0) {
                    errorReason = "Constraint violation";
                } else {
                    errorReason = errorReason.substring(0, errorReason.length() - 2); // Remove trailing comma
                }
                logger.log(Level.FINEST,
                        "Unable to edit hall hours due to constraint violoation",
                        e);

            } else {
                errorReason = "Something unexpected happened";
                logger.log(Level.SEVERE,
                        "Unable to edit hall hours", e);
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
