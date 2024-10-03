package org.jlab.btm.presentation.controller;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.btm.business.service.*;
import org.jlab.btm.business.util.BtmTimeUtil;
import org.jlab.btm.persistence.entity.*;
import org.jlab.btm.persistence.enumeration.DurationUnits;
import org.jlab.btm.persistence.enumeration.TimesheetType;
import org.jlab.btm.persistence.projection.*;
import org.jlab.btm.presentation.util.BtmParamConverter;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.enumeration.Hall;
import org.jlab.smoothness.persistence.enumeration.Shift;

/**
 * @author ryans
 */
@WebServlet(
    name = "TimesheetController",
    urlPatterns = {"/timesheet"})
public class TimesheetController extends HttpServlet {

  private static final Logger logger = Logger.getLogger(TimesheetController.class.getName());

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

        /*if (BtmTimeUtil.isFirstHourOfExperimenterShift(now)) {
            shift = shift.getPrevious();

            if (shift == Shift.SWING) {
                day = TimeUtil.addDays(day, -1);
            }
        }*/
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

    if (redirect) {
      response.sendRedirect(
          response.encodeRedirectURL(this.getCurrentUrl(request, type, day, shift, units)));
      return;
    }

    String previousUrl = getPreviousUrl(request, type, day, shift, units);
    String nextUrl = getNextUrl(request, type, day, shift, units);

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
    request.setAttribute("previousUrl", previousUrl);
    request.setAttribute("nextUrl", nextUrl);
    request.setAttribute("message", message);
    request.setAttribute("now", new Date());
    request.setAttribute("hoursInShift", hoursInShift);

    if ("Y".equals(request.getParameter("crosscheck"))) {
      request
          .getRequestDispatcher("/WEB-INF/views/cross-check-only.jsp")
          .forward(request, response);
    } else {
      request.getRequestDispatcher("/WEB-INF/views/timesheet.jsp").forward(request, response);
    }
  }

  private String getCurrentUrl(
      HttpServletRequest request, TimesheetType type, Date day, Shift shift, DurationUnits units) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

    return request.getContextPath()
        + "/timesheet/"
        + type.toString().toLowerCase()
        + "/"
        + dateFormat.format(day).toLowerCase()
        + "/"
        + shift.toString().toLowerCase()
        + "/"
        + units.toString().toLowerCase();
  }

  private String getPreviousUrl(
      HttpServletRequest request, TimesheetType type, Date day, Shift shift, DurationUnits units) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

    Date previousDay = day;

    Shift previousShift = shift.getPrevious();

    if (previousShift == Shift.SWING) {
      previousDay = TimeUtil.addDays(day, -1);
    }

    return request.getContextPath()
        + "/timesheet/"
        + type.toString().toLowerCase()
        + "/"
        + dateFormat.format(previousDay).toLowerCase()
        + "/"
        + previousShift.toString().toLowerCase()
        + "/"
        + units.toString().toLowerCase();
  }

  private String getNextUrl(
      HttpServletRequest request, TimesheetType type, Date day, Shift shift, DurationUnits units) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

    Date nextDay = day;

    Shift nextShift = shift.getNext();

    if (nextShift == Shift.OWL) {
      nextDay = TimeUtil.addDays(day, 1);
    }

    return request.getContextPath()
        + "/timesheet/"
        + type.toString().toLowerCase()
        + "/"
        + dateFormat.format(nextDay).toLowerCase()
        + "/"
        + nextShift.toString().toLowerCase()
        + "/"
        + units.toString().toLowerCase();
  }
}
