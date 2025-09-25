package org.jlab.btm.presentation.controller;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jlab.btm.business.service.MonthlyScheduleService;
import org.jlab.btm.persistence.entity.MonthlySchedule;
import org.jlab.btm.presentation.util.BtmParamConverter;
import org.jlab.smoothness.business.exception.UserFriendlyException;

/**
 * @author ryans
 */
@WebServlet(
    name = "CreateSchedule",
    urlPatterns = {"/create-schedule"})
public class CreateSchedule extends HttpServlet {

  @EJB MonthlyScheduleService scheduleService;

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

    Date start = null;

    try {
      start = BtmParamConverter.convertMonthAndYear(request, "date");
    } catch (ParseException e) {
      throw new ServletException("Unable to parse date parameter", e);
    }

    if (start == null) {
      throw new ServletException("You must specify a month");
    }

    try {
      MonthlySchedule schedule = scheduleService.create(start);

      String url = getUrl(request, schedule.getStartDay(), schedule.getVersion());

      response.sendRedirect(response.encodeRedirectURL(url));
    } catch (UserFriendlyException e) {
      throw new ServletException("Unable to create schedule");
    }
  }

  private String getUrl(HttpServletRequest request, Date date, Integer version) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM-yyyy");

    String url =
        request.getContextPath()
            + "/schedule/"
            + dateFormat.format(date).toLowerCase()
            + "/"
            + version;

    boolean fullscreen = "Y".equals(request.getParameter("fullscreen"));
    if (fullscreen) {
      url = url + "?print=Y&fullscreen=Y";
    }

    return url;
  }
}
