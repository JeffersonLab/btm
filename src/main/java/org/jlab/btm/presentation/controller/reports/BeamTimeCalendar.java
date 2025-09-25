package org.jlab.btm.presentation.controller.reports;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.jlab.btm.business.service.ReviewCalendarService;
import org.jlab.btm.persistence.projection.ReviewWeek;
import org.jlab.btm.presentation.util.BtmParamConverter;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.ParamUtil;

/**
 * @author ryans
 */
@WebServlet(
    name = "BeamTimeCalendar",
    urlPatterns = {"/reports/bt-calendar"})
public class BeamTimeCalendar extends HttpServlet {

  @EJB ReviewCalendarService calendarService;

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

    try {
      start = BtmParamConverter.convertMonthAndYear2(request, "date");
    } catch (Exception e) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    int offset = ParamUtil.convertAndValidateNonNegativeInt(request, "offset", 0);
    int max = ParamUtil.convertAndValidateNonNegativeInt(request, "max", 5);

    Calendar c = Calendar.getInstance();
    // Date now = c.getTime();
    c.set(Calendar.MILLISECOND, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.DATE, 1); // Just-in-case force first of month
    // Date today = c.getTime();
    c.add(Calendar.MONTH, -1);
    Date currentMonthStart = c.getTime();

    if (start == null) {
      start = new Date();
    }

    start = TimeUtil.startOfMonth(start, Calendar.getInstance());

    c.setTime(start);
    c.add(Calendar.MONTH, -1);
    Date previousMonth = c.getTime();
    c.add(Calendar.MONTH, 2);
    Date nextMonth = c.getTime();

    // Date end = TimeUtil.startOfNextMonth(start, Calendar.getInstance());

    List<ReviewWeek> weekList = calendarService.getMonth(start);

    SimpleDateFormat formatter = new SimpleDateFormat("MMMM yyyy");

    String selectionMessage = formatter.format(start);

    request.setAttribute("previousMonth", previousMonth);
    request.setAttribute("nextMonth", nextMonth);
    request.setAttribute("selectionMessage", selectionMessage);
    request.setAttribute("weekList", weekList);
    request.setAttribute("start", start);

    request
        .getRequestDispatcher("/WEB-INF/views/reports/bt-calendar.jsp")
        .forward(request, response);
  }
}
