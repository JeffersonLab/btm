package org.jlab.btm.presentation.controller.ajax;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBAccessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.btm.business.service.MonthlyScheduleService;
import org.jlab.btm.presentation.util.BtmParamConverter;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.ExceptionUtil;
import org.jlab.smoothness.presentation.util.ParamConverter;
import org.jlab.smoothness.presentation.util.ParamUtil;

/**
 * @author ryans
 */
@WebServlet(
    name = "EditScheduleRow",
    urlPatterns = {"/ajax/edit-schedule-row"})
public class EditScheduleRow extends HttpServlet {

  private static final Logger logger = Logger.getLogger(EditScheduleRow.class.getName());

  @EJB MonthlyScheduleService monthlyScheduleService;

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

      Date date = BtmParamConverter.convertDayMonthAndYear(request, "date");
      BigInteger scheduleId = ParamConverter.convertBigInteger(request, "scheduleId");
      String accProgram = request.getParameter("accProgram");
      Integer kiloVoltsPerPass =
          ParamUtil.convertAndValidateNonNegativeInteger(request, "kiloVoltsPerPass");
      Integer minHallCount =
          ParamUtil.convertAndValidateNonNegativeInteger(request, "minHallCount");
      int hallAProgramId = ParamUtil.convertAndValidateNonNegativeInt(request, "hallAProgramId");
      int hallBProgramId = ParamUtil.convertAndValidateNonNegativeInt(request, "hallBProgramId");
      int hallCProgramId = ParamUtil.convertAndValidateNonNegativeInt(request, "hallCProgramId");
      int hallDProgramId = ParamUtil.convertAndValidateNonNegativeInt(request, "hallDProgramId");
      Integer hallAKiloVolts =
          ParamUtil.convertAndValidateNonNegativeInteger(request, "hallAKiloVolts");
      Integer hallBKiloVolts =
          ParamUtil.convertAndValidateNonNegativeInteger(request, "hallBKiloVolts");
      Integer hallCKiloVolts =
          ParamUtil.convertAndValidateNonNegativeInteger(request, "hallCKiloVolts");
      Integer hallDKiloVolts =
          ParamUtil.convertAndValidateNonNegativeInteger(request, "hallDKiloVolts");
      Integer hallANanoAmps =
          ParamUtil.convertAndValidateNonNegativeInteger(request, "hallANanoAmps");
      Integer hallBNanoAmps =
          ParamUtil.convertAndValidateNonNegativeInteger(request, "hallBNanoAmps");
      Integer hallCNanoAmps =
          ParamUtil.convertAndValidateNonNegativeInteger(request, "hallCNanoAmps");
      Integer hallDNanoAmps =
          ParamUtil.convertAndValidateNonNegativeInteger(request, "hallDNanoAmps");
      boolean hallAPolarized = ParamUtil.convertAndValidateYNBoolean(request, "hallAPolarized");
      boolean hallBPolarized = ParamUtil.convertAndValidateYNBoolean(request, "hallBPolarized");
      boolean hallCPolarized = ParamUtil.convertAndValidateYNBoolean(request, "hallCPolarized");
      boolean hallDPolarized = ParamUtil.convertAndValidateYNBoolean(request, "hallDPolarized");
      Integer hallAPasses = ParamUtil.convertAndValidateNonNegativeInteger(request, "hallAPasses");
      Integer hallBPasses = ParamUtil.convertAndValidateNonNegativeInteger(request, "hallBPasses");
      Integer hallCPasses = ParamUtil.convertAndValidateNonNegativeInteger(request, "hallCPasses");
      Integer hallDPasses = ParamUtil.convertAndValidateNonNegativeInteger(request, "hallDPasses");
      Integer hallAPriority =
          ParamUtil.convertAndValidateNonNegativeInteger(request, "hallAPriority");
      Integer hallBPriority =
          ParamUtil.convertAndValidateNonNegativeInteger(request, "hallBPriority");
      Integer hallCPriority =
          ParamUtil.convertAndValidateNonNegativeInteger(request, "hallCPriority");
      Integer hallDPriority =
          ParamUtil.convertAndValidateNonNegativeInteger(request, "hallDPriority");
      String hallANotes = request.getParameter("hallANotes");
      String hallBNotes = request.getParameter("hallBNotes");
      String hallCNotes = request.getParameter("hallCNotes");
      String hallDNotes = request.getParameter("hallDNotes");
      String notes = request.getParameter("notes");
      int count = ParamUtil.convertAndValidateNonNegativeInt(request, "count");

      monthlyScheduleService.editRow(
          date,
          scheduleId,
          accProgram,
          kiloVoltsPerPass,
          minHallCount,
          hallAProgramId,
          hallBProgramId,
          hallCProgramId,
          hallDProgramId,
          hallAKiloVolts,
          hallBKiloVolts,
          hallCKiloVolts,
          hallDKiloVolts,
          hallANanoAmps,
          hallBNanoAmps,
          hallCNanoAmps,
          hallDNanoAmps,
          hallAPolarized,
          hallBPolarized,
          hallCPolarized,
          hallDPolarized,
          hallAPasses,
          hallBPasses,
          hallCPasses,
          hallDPasses,
          hallAPriority,
          hallBPriority,
          hallCPriority,
          hallDPriority,
          hallANotes,
          hallBNotes,
          hallCNotes,
          hallDNotes,
          notes,
          count);
    } catch (NumberFormatException e) {
      logger.log(Level.WARNING, "Unable to edit schedule row", e);
      errorReason = "Numeric field out of range or not a number";
    } catch (EJBAccessException e) {
      logger.log(Level.WARNING, "Unable to edit schedule row due to access exception", e);
      errorReason = "Access Denied";
    } catch (UserFriendlyException e) {
      logger.log(Level.INFO, "Unable to edit schedule row: {0}", e.getMessage());
      errorReason = e.getUserMessage();
    } catch (Exception e) {
      Throwable rootCause = ExceptionUtil.getRootCause(e);
      if (rootCause instanceof SQLException) {
        errorReason = "Database exception";
        logger.log(Level.SEVERE, "Unable to edit schedule row", e);
      } else if (rootCause instanceof ConstraintViolationException) {
        errorReason = "Constraint violation";
        logger.log(Level.FINEST, "Unable to edit schedule row due to constraint violoation", e);

      } else {
        errorReason = "Something unexpected happened";
        logger.log(Level.SEVERE, "Unable to edit schedule row", e);
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
