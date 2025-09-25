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
import java.util.logging.Logger;
import org.jlab.btm.business.service.CcSignatureService;
import org.jlab.btm.business.service.ExpSignatureService;
import org.jlab.btm.business.util.BtmTimeUtil;
import org.jlab.btm.persistence.enumeration.DurationUnits;
import org.jlab.btm.persistence.enumeration.TimesheetType;
import org.jlab.btm.presentation.util.BtmParamConverter;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.enumeration.Hall;
import org.jlab.smoothness.persistence.enumeration.Shift;

/**
 * @author ryans
 */
@WebServlet(
    name = "TimesheetLogController",
    urlPatterns = {"/timesheet-log"})
public class TimesheetLogController extends HttpServlet {

  private static final Logger logger = Logger.getLogger(TimesheetLogController.class.getName());

  @EJB CcSignatureService ccSignatureService;
  @EJB ExpSignatureService expSignatureService;

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

    boolean redirect = false;

    TimesheetType type = BtmParamConverter.convertTimesheetType(request, "type");

    if (type == null) {
      type = TimesheetType.CC;
      redirect = true;
    }

    Date now = new Date();
    Date day;
    try {
      day = BtmParamConverter.convertISO8601Date(request, "day", null);
    } catch (ParseException ex) {
      throw new ServletException("Unable to parse date", ex);
    }

    Shift shift = BtmParamConverter.convertShift(request, "shift", null);

    if (shift == null) {
      if (TimesheetType.CC == type) {
        shift = TimeUtil.calculateCrewChiefShift(now);
      } else {
        shift = BtmTimeUtil.calculateExperimenterShift(now);
      }

      redirect = true;
    }

    if (day == null) {
      if (TimesheetType.CC == type) {
        day = TimeUtil.getCurrentCrewChiefShiftDay(now);

        if (TimeUtil.isFirstHourOfCrewChiefShift(now)) {
          shift = shift.getPrevious();

          if (shift == Shift.SWING) {
            day = TimeUtil.addDays(day, -1);
          }
        }
      } else {
        day = BtmTimeUtil.getCurrentExperimenterShiftDay(now);
      }

      redirect = true;
    }

    DurationUnits units = BtmParamConverter.convertDurationUnits(request, "units", null);

    if (units == null) {
      if (TimesheetType.CC == type) {
        units = DurationUnits.HOURS;
      } else {
        units = DurationUnits.MINUTES;
      }

      redirect = true;
    }

    Date startHour, endHour;

    if (TimesheetType.CC == type) {
      startHour = TimeUtil.getCrewChiefStartDayAndHour(day, shift);
      endHour = TimeUtil.getCrewChiefEndDayAndHour(day, shift);
    } else {
      startHour = BtmTimeUtil.getExperimenterStartDayAndHour(day, shift);
      endHour = BtmTimeUtil.getExperimenterEndDayAndHour(day, shift);
    }

    long hoursInShift = TimeUtil.differenceInHours(startHour, endHour) + 1;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH");
    String shiftStartHourStr = dateFormat.format(startHour);
    Date startOfNextShift = TimeUtil.addHours(endHour, 1);

    if (TimesheetType.CC.equals(type)) {
      ccSignatureService.populateRequestAttributes(request, startHour, endHour, startOfNextShift);
    } else {
      Hall hall;

      switch (type) {
        case EA:
          hall = Hall.A;
          break;
        case EB:
          hall = Hall.B;
          break;
        case EC:
          hall = Hall.C;
          break;
        case ED:
          hall = Hall.D;
          break;
        default:
          throw new RuntimeException("Unknown hall: " + type);
      }

      expSignatureService.populateRequestAttributes(request, hall, startHour, endHour);
    }

    dateFormat = new SimpleDateFormat("dd MMM yyyy");

    String message =
        "Type \""
            + type.getLabel()
            + "\" and Date \""
            + dateFormat.format(day)
            + "\" and Shift \""
            + shift
            + "\" and Units \""
            + units.toString().toLowerCase()
            + "\"";

    request.setAttribute("type", type);
    request.setAttribute("day", day);
    request.setAttribute("durationUnits", units);
    request.setAttribute("startHour", startHour);
    request.setAttribute("shiftStartHourStr", shiftStartHourStr);
    request.setAttribute("startOfNextShift", startOfNextShift);
    request.setAttribute("endHour", endHour);
    request.setAttribute("shift", shift);
    request.setAttribute("message", message);
    request.setAttribute("now", new Date());
    request.setAttribute("hoursInShift", hoursInShift);

    if (type == TimesheetType.CC) {
      request
          .getRequestDispatcher("/WEB-INF/views/log-cc-timesheet.jsp")
          .forward(request, response);
    } else {
      request
          .getRequestDispatcher("/WEB-INF/views/log-exp-timesheet.jsp")
          .forward(request, response);
    }
  }
}
