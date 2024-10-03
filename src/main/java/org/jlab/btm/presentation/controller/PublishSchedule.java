package org.jlab.btm.presentation.controller;

import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.btm.business.service.ExpProgramService;
import org.jlab.btm.business.service.MonthlyScheduleService;
import org.jlab.btm.business.service.ScheduleArchiveService;
import org.jlab.btm.business.service.ScheduleDayService;
import org.jlab.btm.persistence.entity.ExpProgram;
import org.jlab.btm.persistence.entity.MonthlySchedule;
import org.jlab.btm.persistence.entity.ScheduleDay;
import org.jlab.btm.presentation.util.InternalHtmlRequestExecutor;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "PublishSchedule",
    urlPatterns = {"/publish-schedule"})
public class PublishSchedule extends HttpServlet {

  private static final Logger logger = Logger.getLogger(PublishSchedule.class.getName());

  @EJB MonthlyScheduleService scheduleService;
  @EJB ScheduleDayService scheduleDayService;
  @EJB ScheduleArchiveService archiveService;
  @EJB ExpProgramService programService;

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
      MonthlySchedule schedule = scheduleService.publish(scheduleId);

      try {
        Map<Integer, ExpProgram> purposeMap = programService.findProgramByIdMap();

        if (schedule != null) {
          Date start = schedule.getStartDay();
          Date end = TimeUtil.endOfMonth(start, Calendar.getInstance());

          List<ScheduleDay> dayList =
              scheduleDayService.find(schedule.getMonthlyScheduleId(), start, end);
          schedule.setScheduleDayList(dayList);
        }

        request.setAttribute("schedule", schedule);
        request.setAttribute("purposeMap", purposeMap);

        String html =
            InternalHtmlRequestExecutor.execute(
                "/WEB-INF/views/export-html-schedule.jsp", request, response);

        archiveService.publish(schedule, html);
      } catch (Exception e) {
        logger.log(
            Level.WARNING,
            "Unable to archive published schedule: "
                + schedule.getStartDay()
                + "; "
                + schedule.getVersion(),
            e);
      }

      String url = getUrl(request, schedule.getStartDay(), schedule.getVersion());

      response.sendRedirect(response.encodeRedirectURL(url));
    } catch (UserFriendlyException e) {
      throw new ServletException("Unable to publish schedule");
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
