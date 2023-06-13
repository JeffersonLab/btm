package org.jlab.btm.presentation.controller.ajax;

import org.jlab.btm.business.service.ExpProgramService;
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
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ryans
 */
@WebServlet(name = "AddProgram", urlPatterns = {"/ajax/add-program"})
public class AddProgram extends HttpServlet {

    private static final Logger logger = Logger.getLogger(
            AddProgram.class.getName());

    @EJB
    ExpProgramService programService;

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

            Hall hall = BtmParamConverter.convertHall(request, "hall");
            String name = request.getParameter("name");
            String alias = request.getParameter("alias");
            String url = request.getParameter("url");
            Boolean experiment = ParamConverter.convertYNBoolean(request, "experiment");
            Boolean active = ParamConverter.convertYNBoolean(request, "active");

            programService.add(hall, name, alias, url, experiment, active);
        } catch (EJBAccessException e) {
            logger.log(Level.WARNING,
                    "Unable to add program due to access exception", e);
            errorReason = "Access Denied";
        } catch (UserFriendlyException e) {
            logger.log(Level.INFO,
                    "Unable to add program: {0}",
                    e.getMessage());
            errorReason = e.getMessage();
        } catch (Exception e) {
            logger.log(Level.SEVERE,
                    "Unable to add program", e);
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
