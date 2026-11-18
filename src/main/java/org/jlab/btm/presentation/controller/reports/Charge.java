package org.jlab.btm.presentation.controller.reports;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.jlab.btm.business.service.ExpProgramService;
import org.jlab.btm.business.service.MonthlyScheduleService;
import org.jlab.btm.persistence.entity.ExpProgram;
import org.jlab.btm.persistence.entity.MonthlySchedule;
import org.jlab.btm.persistence.entity.ScheduleDay;
import org.jlab.btm.persistence.projection.DailyPrograms;
import org.jlab.btm.presentation.util.BtmParamConverter;
import org.jlab.smoothness.business.util.TimeUtil;

/**
 * @author ryans
 */
@WebServlet(
    name = "Charge",
    urlPatterns = {"/reports/charge"})
public class Charge extends HttpServlet {

  @EJB MonthlyScheduleService scheduleService;

  @EJB ExpProgramService programService;

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
      start = BtmParamConverter.convertJLabDate(request, "start");
      end = BtmParamConverter.convertJLabDate(request, "end");
    } catch (Exception e) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    String selectionMessage = null;
    List<DailyPrograms> programDays = null;

    if (start != null && end != null) {
      if (!start.before(end)) {
        throw new ServletException("start date must be before end date");
      }

      List<MonthlySchedule> monthlySchedules =
          scheduleService.findMostRecentPublishedInDateRange(start, end);
      List<ScheduleDay> scheduleDays =
          scheduleService.filterScheduleDaysFromRange(monthlySchedules, start, end);

      programDays = programsByDay(scheduleDays, programService.findProgramByIdMap());

      selectionMessage = TimeUtil.formatSmartRangeSeparateTime(start, end);
    }

    request.setAttribute("start", start);
    request.setAttribute("end", end);
    request.setAttribute("selectionMessage", selectionMessage);
    request.setAttribute("programDays", programDays);

    request.getRequestDispatcher("/WEB-INF/views/reports/charge.jsp").forward(request, response);
  }

  /**
   * Names the program each hall is scheduled to run on each day, for the charts' hover.
   *
   * @param scheduleDays the published schedule's days, in order
   * @param programMap the programs by ID
   * @return the programs by day, in the same order
   */
  static List<DailyPrograms> programsByDay(
      List<ScheduleDay> scheduleDays, Map<Integer, ExpProgram> programMap) {
    List<DailyPrograms> programDays = new ArrayList<>();

    for (ScheduleDay day : scheduleDays) {
      programDays.add(
          new DailyPrograms(
              day.getDayMonthYear(),
              programMap.get(day.getHallAProgramId()).getName(),
              programMap.get(day.getHallBProgramId()).getName(),
              programMap.get(day.getHallCProgramId()).getName(),
              programMap.get(day.getHallDProgramId()).getName()));
    }

    return programDays;
  }
}
