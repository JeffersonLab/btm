package org.jlab.btm.presentation.controller.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBAccessException;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.validation.ConstraintViolationException;
import org.jlab.btm.business.service.ExcelScheduleService;
import org.jlab.btm.persistence.entity.MonthlySchedule;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.ExceptionUtil;

/**
 * @author ryans
 */
@WebServlet(
    name = "Upload",
    urlPatterns = {"/schedule/upload"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,
    maxFileSize = 1024 * 1024 * 5,
    maxRequestSize = 1024 * 1024 * 5 * 5)
public class Upload extends HttpServlet {

  private static final Logger LOGGER = Logger.getLogger(Upload.class.getName());

  @EJB ExcelScheduleService excelService;

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
    List<MonthlySchedule> resultList = null;

    try {
      if (request.getRequestedSessionId() != null && !request.isRequestedSessionIdValid()) {
        throw new UserFriendlyException(
            "Your session has expired.  Please reload the page and relogin.");
      }

      if (request.getParts().size() == 1) {
        Part part = request.getParts().toArray(new Part[0])[0];
        String fileName = part.getSubmittedFileName();
        // part.write(uploadPath + File.separator + fileName);
        resultList = excelService.upload(part.getInputStream());
      } else {
        errorReason = "Multipart submission expected with single file";
      }
    } catch (EJBAccessException e) {
      LOGGER.log(Level.WARNING, "Unable to upload due to access exception", e);
      errorReason = "Access Denied";
    } catch (UserFriendlyException e) {
      LOGGER.log(Level.INFO, "Unable to upload: {0}", e.getMessage());
      errorReason = e.getMessage();
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Unable to upload", e);
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

      SimpleDateFormat formatter = new SimpleDateFormat("MMMM yyyy");

      xml = "<response><span class=\"status\">Success</span>";

      for (MonthlySchedule schedule : resultList) {
        xml =
            xml
                + "<span class=\"schedule\">"
                + "<span class=\"date\">"
                + formatter.format(schedule.getStartDay())
                + "</span>"
                + "<span class=\"version\">"
                + schedule.getVersion()
                + "</span></schedule>";
      }

      xml = xml + "</response>";
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
