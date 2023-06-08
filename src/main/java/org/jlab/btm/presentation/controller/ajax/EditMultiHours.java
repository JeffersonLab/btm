package org.jlab.btm.presentation.controller.ajax;

import org.jlab.btm.business.service.CcMultiplicityHourService;
import org.jlab.btm.persistence.entity.CcMultiplicityHour;
import org.jlab.btm.presentation.util.BtmParamConverter;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.ExceptionUtil;
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
@WebServlet(name = "EditMultiHours", urlPatterns = {"/ajax/edit-multi-hours"})
public class EditMultiHours extends HttpServlet {

    private static final Logger logger = Logger.getLogger(
            EditMultiHours.class.getName());

    @EJB
    CcMultiplicityHourService multiHourService;

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
            Short[] fourUpArray = ParamConverter.convertShortArray(request,
                    "fourUp[]", (short) 0);
            Short[] threeUpArray = ParamConverter.convertShortArray(request,
                    "threeUp[]", (short) 0);
            Short[] twoUpArray = ParamConverter.convertShortArray(request,
                    "twoUp[]", (short) 0);
            Short[] oneUpArray = ParamConverter.convertShortArray(request,
                    "oneUp[]", (short) 0);
            Short[] anyUpArray = ParamConverter.convertShortArray(request,
                    "anyUp[]", (short) 0);
            Short[] allUpArray = ParamConverter.convertShortArray(request,
                    "allUp[]", (short) 0);
            Short[] downHardArray = ParamConverter.convertShortArray(request,
                    "downHard[]", (short) 0);

            multiHourService.editMultiHours(hourArray, fourUpArray, threeUpArray, twoUpArray, oneUpArray,
                    anyUpArray, allUpArray, downHardArray);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Unable to edit multiplicity hours", e);
            errorReason = "Numeric field out of range or not a number";
        } catch (EJBAccessException e) {
            logger.log(Level.WARNING,
                    "Unable to edit multiplicity hours due to access exception", e);
            errorReason = "Access Denied";
        } catch (UserFriendlyException e) {
            logger.log(Level.INFO,
                    "Unable to edit multiplicity hours: {0}",
                    e.getMessage());
            errorReason = e.getMessage();
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtil.getRootCause(e);
            if (rootCause instanceof SQLException) {
                errorReason = "Database exception";
                logger.log(Level.SEVERE,
                        "Unable to edit multiplicity hours", e);
            } else if (rootCause instanceof ConstraintViolationException) {
                ConstraintViolationException cve = (ConstraintViolationException) rootCause;
                Set<ConstraintViolation<?>> violations = cve.getConstraintViolations();
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH");
                errorReason = "";
                for (ConstraintViolation<?> violation : violations) {
                    Object bean = violation.getRootBean();
                    if (bean instanceof CcMultiplicityHour) {
                        CcMultiplicityHour hour = (CcMultiplicityHour) bean;
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
                        "Unable to edit multiplicity hours due to constraint violoation", e);

            } else {
                errorReason = "Something unexpected happened";
                logger.log(Level.SEVERE,
                        "Unable to edit multiplicity hours", e);
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
