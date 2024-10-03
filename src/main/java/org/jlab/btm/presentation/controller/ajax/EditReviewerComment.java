package org.jlab.btm.presentation.controller.ajax;

import java.io.IOException;
import java.io.PrintWriter;
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
import org.jlab.btm.business.service.CcCrossCheckCommentService;
import org.jlab.btm.presentation.util.BtmParamConverter;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.ExceptionUtil;

/**
 * @author ryans
 */
@WebServlet(
    name = "EditReviewerComment",
    urlPatterns = {"/ajax/edit-reviewer-comment"})
public class EditReviewerComment extends HttpServlet {

  private static final Logger LOGGER = Logger.getLogger(EditReviewerComment.class.getName());

  @EJB CcCrossCheckCommentService crossCheckCommentService;

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

      Date startDayAndHour = BtmParamConverter.convertDayAndHour(request, "startDayAndHour");
      String comments = request.getParameter("comments");

      crossCheckCommentService.editReviewerRemark(startDayAndHour, comments);
    } catch (EJBAccessException e) {
      LOGGER.log(Level.WARNING, "Unable to edit shift info due to access exception", e);
      errorReason = "Access Denied";
    } catch (UserFriendlyException e) {
      LOGGER.log(Level.INFO, "Unable to edit shift info: {0}", e.getMessage());
      errorReason = e.getMessage();
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Unable to edit shift info", e);
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
      LOGGER.log(Level.SEVERE, "PrintWriter Error");
    }
  }
}
