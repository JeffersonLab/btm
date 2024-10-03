package org.jlab.btm.presentation.controller.excel;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.btm.business.service.CcAccHourService;
import org.jlab.btm.business.service.MonthlyScheduleService;
import org.jlab.btm.business.service.PdShiftPlanService;
import org.jlab.btm.business.service.excel.ExcelBeamSummaryService;
import org.jlab.btm.persistence.projection.CcAccSum;
import org.jlab.btm.persistence.projection.PacAccSum;
import org.jlab.btm.persistence.projection.PdAccSum;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "ExcelBeamSummary",
    urlPatterns = {"/excel/beam-summary.xlsx"})
public class ExcelBeamSummary extends HttpServlet {

  @EJB ExcelBeamSummaryService excelService;
  @EJB CcAccHourService ccService;
  @EJB PdShiftPlanService pdService;
  @EJB MonthlyScheduleService pacService;

  /**
   * Handles the HTTP <code>GET</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    Date start = null;
    Date end = null;
    CcAccSum ccSum = null;
    PdAccSum pdSum = null;
    PacAccSum pacSum = null;

    try {
      start = ParamConverter.convertFriendlyDate(request, "start");
      end = ParamConverter.convertFriendlyDate(request, "end");
    } catch (ParseException e) {
      throw new ServletException("Unable to parse date", e);
    }

    if (start == null) {
      throw new ServletException("Start date must not be null");
    }

    if (end == null) {
      throw new ServletException("End date must not be null");
    }

    if (start != null && end != null) {
      if (start.after(end)) {
        throw new ServletException("start date cannot be after end date");
      }
    }

    ccSum = ccService.findSummary(start, end);
    pdSum = pdService.findSummary(start, end);
    pacSum = pacService.findSummary(start, end);

    String filters = TimeUtil.formatSmartRangeSeparateTime(start, end);

    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("content-disposition", "attachment;filename=\"beam-summary.xlsx\"");

    excelService.export(response.getOutputStream(), ccSum, pdSum, pacSum, filters.trim());
  }
}
