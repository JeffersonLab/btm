package org.jlab.btm.presentation.controller;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.jlab.btm.business.service.ExcelScheduleService;
import org.jlab.btm.business.service.ExpProgramService;
import org.jlab.btm.business.service.MonthlyScheduleService;
import org.jlab.btm.business.service.ScheduleDayService;
import org.jlab.btm.persistence.entity.ExpProgram;
import org.jlab.btm.persistence.entity.MonthlySchedule;
import org.jlab.btm.persistence.entity.ScheduleDay;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "ExportExcelSchedule",
    urlPatterns = {"/schedule/schedule.xlsx"})
public class ExportExcelSchedule extends HttpServlet {

  @EJB MonthlyScheduleService scheduleService;
  @EJB ScheduleDayService scheduleDayService;
  @EJB ExpProgramService programService;
  @EJB ExcelScheduleService excelService;

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

    BigInteger scheduleId = ParamConverter.convertBigInteger(request, "scheduleId");

    MonthlySchedule schedule = scheduleService.find(scheduleId);

    if (schedule != null) {
      Date start = schedule.getStartDay();
      Date end = TimeUtil.endOfMonth(start, Calendar.getInstance());

      List<ScheduleDay> dayList =
          scheduleDayService.find(schedule.getMonthlyScheduleId(), start, end);
      schedule.setScheduleDayList(dayList);
    }

    Map<Integer, ExpProgram> purposeMap = programService.findProgramByIdMap();

    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("content-disposition", "attachment;filename=\"schedule.xlsx\"");

    excelService.export(response.getOutputStream(), schedule, purposeMap);
  }
}
