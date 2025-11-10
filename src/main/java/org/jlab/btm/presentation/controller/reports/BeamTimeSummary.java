package org.jlab.btm.presentation.controller.reports;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.jlab.btm.business.service.CcAccHourService;
import org.jlab.btm.business.service.DowntimeService;
import org.jlab.btm.business.service.MonthlyScheduleService;
import org.jlab.btm.business.service.PdShiftPlanService;
import org.jlab.btm.persistence.projection.CcAccSum;
import org.jlab.btm.persistence.projection.DowntimeSummaryTotals;
import org.jlab.btm.persistence.projection.PacAccSum;
import org.jlab.btm.persistence.projection.PdAccSum;
import org.jlab.btm.presentation.util.BtmParamConverter;
import org.jlab.smoothness.business.util.TimeUtil;

/**
 * @author ryans
 */
@WebServlet(
    name = "BeamTimeSummary",
    urlPatterns = {"/reports/beam-time-summary"})
public class BeamTimeSummary extends HttpServlet {

  @EJB CcAccHourService ccService;
  @EJB PdShiftPlanService pdService;
  @EJB MonthlyScheduleService pacService;
  @EJB DowntimeService downService;

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

    try {
      start = BtmParamConverter.convertJLabDateTime(request, "start");
      end = BtmParamConverter.convertJLabDateTime(request, "end");
    } catch (Exception e) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    Calendar c = Calendar.getInstance();
    Date now = c.getTime();
    c.set(Calendar.MILLISECOND, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.HOUR_OF_DAY, 7);
    Date today = c.getTime();
    c.add(Calendar.DATE, -7);
    Date sevenDaysAgo = c.getTime();

    /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
    HttpSession session = request.getSession(true);
    Date sessionStart = (Date) session.getAttribute("start");
    Date sessionEnd = (Date) session.getAttribute("end");

    /* Redirect if using defaults to maintain bookmarkability (html-to-image/pdf for example) */
    boolean needRedirect = false;

    if (start == null) {
      needRedirect = true;
      if (sessionStart != null) {
        start = sessionStart;
      } else {
        start = sevenDaysAgo;
      }
    }

    if (end == null) {
      needRedirect = true;
      if (sessionEnd != null) {
        end = sessionEnd;
      } else {
        end = today;
      }
    }

    if (needRedirect) {
      response.sendRedirect(response.encodeRedirectURL(this.getCurrentUrl(request, start, end)));
      return;
    }

    session.setAttribute("start", start);
    session.setAttribute("end", end);

    String selectionMessage = null;
    CcAccSum ccSum = null;
    PacAccSum pacSum = null;
    PdAccSum pdSum = null;
    long blockedSeconds = 0;
    long tuningSeconds = 0;

    if (start != null && end != null) {
      if (start.after(end)) {
        throw new ServletException("start date cannot be after end date");
      }

      // Start is inclusive, end is exclusive

      ccSum = ccService.findSummary(start, end);
      pdSum = pdService.findSummary(start, end);
      pacSum = pacService.findSummary(start, end);

      DowntimeSummaryTotals blockedTotals =
          downService.reportTotals(start, end, BigInteger.valueOf(1L));

      DowntimeSummaryTotals tuningTotals =
          downService.reportTotals(start, end, BigInteger.valueOf(9L));

      blockedSeconds = blockedTotals.getEventSeconds();
      tuningSeconds = tuningTotals.getEventSeconds();

      // System.err.println("tuningSeconds: " + tuningSeconds);

      selectionMessage = TimeUtil.formatSmartRangeSeparateTime(start, end);
    }

    request.setAttribute("start", start);
    request.setAttribute("end", end);
    request.setAttribute("selectionMessage", selectionMessage);
    request.setAttribute("ccSum", ccSum);
    request.setAttribute("pdSum", pdSum);
    request.setAttribute("pacSum", pacSum);
    request.setAttribute("blockedSeconds", blockedSeconds);
    request.setAttribute("tuningSeconds", tuningSeconds);

    request
        .getRequestDispatcher("/WEB-INF/views/reports/beam-time-summary.jsp")
        .forward(request, response);
  }

  private String getCurrentUrl(HttpServletRequest request, Date start, Date end) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm");

    return request.getContextPath()
        + "/reports/beam-time-summary?start="
        + URLEncoder.encode(dateFormat.format(start), StandardCharsets.UTF_8)
        + "&end="
        + URLEncoder.encode(dateFormat.format(end), StandardCharsets.UTF_8);
  }
}
