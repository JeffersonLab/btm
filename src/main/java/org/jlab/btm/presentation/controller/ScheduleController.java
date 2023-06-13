package org.jlab.btm.presentation.controller;

import org.jlab.btm.business.service.ExpProgramService;
import org.jlab.btm.business.service.MonthlyScheduleService;
import org.jlab.btm.business.service.ScheduleDayService;
import org.jlab.btm.persistence.entity.ExpProgram;
import org.jlab.btm.persistence.entity.MonthlySchedule;
import org.jlab.btm.persistence.entity.ScheduleDay;
import org.jlab.btm.presentation.util.BtmParamConverter;
import org.jlab.btm.presentation.util.WallCalendar;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.enumeration.Hall;
import org.jlab.smoothness.presentation.util.ParamBuilder;
import org.jlab.smoothness.presentation.util.ParamUtil;
import org.jlab.smoothness.presentation.util.ServletUtil;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.MonthDay;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author ryans
 */
@WebServlet(name = "ScheduleController", urlPatterns = {"/schedule"})
public class ScheduleController extends HttpServlet {

    @EJB
    MonthlyScheduleService monthlyScheduleService;
    @EJB
    ScheduleDayService scheduleDayService;
    @EJB
    ExpProgramService programService;

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Date start = null;

        try {
            start = BtmParamConverter.convertMonthAndYear(request, "date");
        } catch (ParseException e) {
            throw new ServletException("Unable to parse date parameter", e);
        }

        if (start == null) {
            start = TimeUtil.startOfMonth(new Date(), Calendar.getInstance());
        }

        Date end = TimeUtil.endOfMonth(start, Calendar.getInstance());

        Integer version = ParamUtil.convertAndValidateNonNegativeInteger(request, "version");

        String view = request.getParameter("view");

        if(view == null || view.isEmpty()) {
            view = "calendar";
        }

        boolean publicProxy = ParamUtil.convertAndValidateYNBoolean(request, "public-proxy", false);

        if (request.getHeader("X-Public-Proxy") != null || request.getParameter("public-proxy") != null) {
            publicProxy = true; // You don't have a choice if proxy server sets this
        }

        List<MonthlySchedule> scheduleList = monthlyScheduleService.findAll(start);
        MonthlySchedule schedule = null;

        if (version == null) {
            schedule = monthlyScheduleService.findMostRecentPublished(scheduleList);

            if(schedule != null) {
                version = schedule.getVersion();
            } else {
                // If no versions exist then assume first version
                version = 1;
            }
        } else {
            // Try to grab specific version
            if (scheduleList != null && !scheduleList.isEmpty()) {
                for (MonthlySchedule s : scheduleList) {
                    if (s.getVersion() == version) {
                        schedule = s;
                        break;
                    }
                }
            }
        }

        // Fetch actual days of schedule
        if (schedule != null) {
            List<ScheduleDay> dayList = scheduleDayService.find(schedule.getMonthlyScheduleId(),
                    start, end);
            schedule.setScheduleDayList(dayList);

            if ("calendar".equals(view)) {
                WallCalendar<ScheduleDay> calendar = new WallCalendar<>(start);
                for (ScheduleDay day : dayList) {
                    MonthDay md = MonthDay.from(day.getDayMonthYear().toInstant().atZone(ZoneId.systemDefault()));
                    calendar.addNote(md, day);
                }
                request.setAttribute("calendar", calendar);
            }
        }

        Map<Integer, ExpProgram> purposeMap = programService.findProgramByIdMap();
        List<ExpProgram> hallAPurposeList = programService.findByHall(Hall.A, true);
        List<ExpProgram> hallBPurposeList = programService.findByHall(Hall.B, true);
        List<ExpProgram> hallCPurposeList = programService.findByHall(Hall.C, true);
        List<ExpProgram> hallDPurposeList = programService.findByHall(Hall.D, true);

        request.setAttribute("start", start);
        request.setAttribute("scheduleList", scheduleList);
        request.setAttribute("schedule", schedule);
        request.setAttribute("version", version);
        request.setAttribute("purposeMap", purposeMap);
        request.setAttribute("hallAPurposeList", hallAPurposeList);
        request.setAttribute("hallBPurposeList", hallBPurposeList);
        request.setAttribute("hallCPurposeList", hallCPurposeList);
        request.setAttribute("hallDPurposeList", hallDPurposeList);
        request.setAttribute("previousUrl", getPreviousUrl(request, start));
        request.setAttribute("nextUrl", getNextUrl(request, start));

        if (publicProxy) {
            request.getRequestDispatcher("/WEB-INF/views/public-schedule.jsp").forward(request, response);
        } else {
            request.getRequestDispatcher("/WEB-INF/views/schedule.jsp").forward(request, response);
        }
    }

    private String getPreviousUrl(HttpServletRequest request, Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM-yyyy");

        Date previousMonth = TimeUtil.addMonths(date, -1);

        String url = request.getContextPath() + "/schedule/" + dateFormat.format(
                previousMonth).toLowerCase();

        ParamBuilder builder = new ParamBuilder();

        boolean fullscreen = "Y".equals(request.getParameter("fullscreen"));
        if (fullscreen) {
            builder.add("print", "Y");
            builder.add("fullscreen", "Y");
        }

        if ("table".equals(request.getParameter("view"))) {
            builder.add("view", "table");
        }

        url = url + ServletUtil.buildQueryString(builder.getParams(), "UTF-8");

        return url;
    }

    private String getNextUrl(HttpServletRequest request, Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM-yyyy");

        Date nextMonth = TimeUtil.addMonths(date, 1);

        String url = request.getContextPath() + "/schedule/" + dateFormat.format(
                nextMonth).toLowerCase();

        ParamBuilder builder = new ParamBuilder();

        boolean fullscreen = "Y".equals(request.getParameter("fullscreen"));
        if (fullscreen) {
            builder.add("print", "Y");
            builder.add("fullscreen", "Y");
        }

        if ("table".equals(request.getParameter("view"))) {
            builder.add("view", "table");
        }

        url = url + ServletUtil.buildQueryString(builder.getParams(), "UTF-8");

        return url;
    }
}
