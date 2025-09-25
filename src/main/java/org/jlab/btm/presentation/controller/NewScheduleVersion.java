package org.jlab.btm.presentation.controller;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jlab.btm.business.service.MonthlyScheduleService;
import org.jlab.btm.persistence.entity.MonthlySchedule;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "NewScheduleVersion",
    urlPatterns = {"/new-schedule-version"})
public class NewScheduleVersion extends HttpServlet {

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

    BigInteger scheduleId = ParamConverter.convertBigInteger(request, "scheduleId");

    try {
      MonthlySchedule schedule = scheduleService.newVersion(scheduleId);

      String url = getUrl(request, schedule.getStartDay(), schedule.getVersion());

      response.sendRedirect(response.encodeRedirectURL(url));
    } catch (UserFriendlyException e) {
      throw new ServletException("Unable to create new version of schedule");
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
