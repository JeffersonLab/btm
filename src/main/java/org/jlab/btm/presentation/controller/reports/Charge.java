package org.jlab.btm.presentation.controller.reports;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.btm.business.service.ExpProgramService;
import org.jlab.btm.business.service.MonthlyScheduleService;
import org.jlab.btm.persistence.entity.ExpProgram;
import org.jlab.btm.persistence.entity.MonthlySchedule;
import org.jlab.btm.persistence.entity.ScheduleDay;
import org.jlab.btm.persistence.projection.DailyCharge;
import org.jlab.btm.persistence.projection.HallChargeData;
import org.jlab.btm.presentation.util.BtmParamConverter;
import org.jlab.smoothness.business.util.TimeUtil;

/**
 * @author ryans
 */
@WebServlet(
    name = "Charge",
    urlPatterns = {"/reports/charge"})
public class Charge extends HttpServlet {

  private static final Logger LOGGER = Logger.getLogger(Charge.class.getName());

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
    } catch (ParseException e) {
      throw new ServletException("Unable to parse date", e);
    }

    Double period = null;
    String selectionMessage = null;
    HallChargeData scheduledChargeData = null;

    if (start != null && end != null) {
      if (!start.before(end)) {
        throw new ServletException("start date must be before end date");
      }

      period = (end.getTime() - start.getTime()) / 1000.0 / 60 / 60;

      List<MonthlySchedule> monthlySchedules =
          scheduleService.findMostRecentPublishedInDateRange(start, end);
      List<ScheduleDay> scheduleDays =
          scheduleService.filterScheduleDaysFromRange(monthlySchedules, start, end);

      scheduledChargeData = calculateScheduledHallCharge(scheduleDays);

      /*LOGGER.log(Level.WARNING, "Printing Monthly Schedules: ");
      for(MonthlySchedule schedule: monthlySchedules) {
          LOGGER.log(Level.WARNING, schedule.toString());
      }

      LOGGER.log(Level.WARNING, "Printing Days: ");
      for(ScheduleDay day: scheduleDays) {
          LOGGER.log(Level.WARNING, day.toString());
      }*/

      /*LOGGER.log(Level.WARNING, "Printing Hall A Charge: ");
      for(DailyCharge charge: scheduledChargeData.chargeListA) {
          LOGGER.log(Level.WARNING, charge.toString());
      }*/

      selectionMessage = TimeUtil.formatSmartRangeSeparateTime(start, end);
    }

    request.setAttribute("start", start);
    request.setAttribute("end", end);
    request.setAttribute("selectionMessage", selectionMessage);
    request.setAttribute("period", period);
    request.setAttribute("scheduledChargeData", scheduledChargeData);

    request.getRequestDispatcher("/WEB-INF/views/reports/charge.jsp").forward(request, response);
  }

  private HallChargeData calculateScheduledHallCharge(List<ScheduleDay> scheduleDays) {
    HallChargeData data = new HallChargeData();

    Map<Integer, ExpProgram> purposeMap = programService.findProgramByIdMap();

    if (scheduleDays != null && !scheduleDays.isEmpty()) {
      // Date dateprev = TimeUtil.addDays(scheduleDays.get(0).getDayMonthYear(), -1);
      Date dateprev = scheduleDays.get(0).getDayMonthYear();
      DailyCharge prevA = new DailyCharge();
      DailyCharge prevB = new DailyCharge();
      DailyCharge prevC = new DailyCharge();
      DailyCharge prevD = new DailyCharge();

      for (ScheduleDay day : scheduleDays) {
        Date d = day.getDayMonthYear();
        long elapsedSeconds = (d.getTime() - dateprev.getTime()) / 1000;
        dateprev = d;

        // Hall A
        DailyCharge recordA = new DailyCharge();
        recordA.program = purposeMap.get(day.getHallAProgramId()).getName();
        Integer nAA = day.getHallANanoAmps();

        if (nAA == null) {
          nAA = 0;
        }

        recordA.d = d;
        recordA.nA = nAA;
        recordA.nC = (prevA.nA * elapsedSeconds) + prevA.nC;
        prevA = recordA;
        data.getChargeListA().add(recordA);

        /*LOGGER.log(Level.WARNING, "Hall A: " + d);
        LOGGER.log(Level.WARNING, "Current (nA): " + nAA);
        LOGGER.log(Level.WARNING, "Elapsed Seconds: " + elapsedSeconds);
        LOGGER.log(Level.WARNING, "Charge (nC): " + recordA.nC);*/

        // Hall B
        DailyCharge recordB = new DailyCharge();
        recordB.program = purposeMap.get(day.getHallBProgramId()).getName();
        Integer nAB = day.getHallBNanoAmps();

        if (nAB == null) {
          nAB = 0;
        }

        recordB.d = d;
        recordB.nA = nAB;
        recordB.nC = (prevB.nA * elapsedSeconds) + prevB.nC;
        prevB = recordB;
        data.getChargeListB().add(recordB);

        // Hall C
        DailyCharge recordC = new DailyCharge();
        recordC.program = purposeMap.get(day.getHallCProgramId()).getName();
        Integer nAC = day.getHallCNanoAmps();

        if (nAC == null) {
          nAC = 0;
        }

        recordC.d = d;
        recordC.nA = nAC;
        recordC.nC = (prevC.nA * elapsedSeconds) + prevC.nC;
        prevC = recordC;
        data.getChargeListC().add(recordC);

        // Hall D
        DailyCharge recordD = new DailyCharge();
        recordD.program = purposeMap.get(day.getHallDProgramId()).getName();
        Integer nAD = day.getHallDNanoAmps();

        if (nAD == null) {
          nAD = 0;
        }

        recordD.d = d;
        recordD.nA = nAD;
        recordD.nC = (prevD.nA * elapsedSeconds) + prevD.nC;
        prevD = recordD;
        data.getChargeListD().add(recordD);
      }
    }

    return data;
  }
}
