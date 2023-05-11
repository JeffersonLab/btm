package org.jlab.btm.presentation.controller.reports;

import org.jlab.btm.business.service.ExpHallHourService;
import org.jlab.btm.business.service.PdShiftPlanService;
import org.jlab.btm.persistence.projection.ExpHallHourTotals;
import org.jlab.btm.presentation.util.BtmParamConverter;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.enumeration.Shift;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author ryans
 */
@WebServlet(name = "HallAvailability", urlPatterns = {"/reports/hall-availability"})
public class HallAvailability extends HttpServlet {

    @EJB
    ExpHallHourService expHourService;
    @EJB
    PdShiftPlanService pdShiftService;

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
        Date end = null;

        try {
            start = BtmParamConverter.convertJLabDateTime(request, "start");
            end = BtmParamConverter.convertJLabDateTime(request, "end");
        } catch (ParseException e) {
            throw new ServletException("Unable to parse date", e);
        }

        Calendar c = Calendar.getInstance();
        Date now = new Date();
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR_OF_DAY, 7);
        Date today = c.getTime();
        c.add(Calendar.DATE, -7);
        Date sevenDaysAgo = c.getTime();

        String chart = request.getParameter("availability-chart");

        /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
        HttpSession session = request.getSession(true);
        Date sessionStart = (Date) session.getAttribute("start");
        Date sessionEnd = (Date) session.getAttribute("end");
        String sessionData = (String) session.getAttribute("availabilityChart");

        /* Redirect if using defaults to maintain bookmarkability (html-to-image/pdf for example) */
        boolean needRedirect = false;

        if (start == null) {
            needRedirect = true;
            if (sessionStart != null) {
                start = sessionStart;
            } else {
                start = sevenDaysAgo;
            }
        }

        if (end == null) {
            needRedirect = true;
            if (sessionEnd != null) {
                end = sessionEnd;
            } else {
                end = today;
            }
        }

        if (chart == null) {
            needRedirect = true;
            if (sessionData != null) {
                chart = sessionData;
            } else {
                chart = "bar";
            }
        }

        if (needRedirect) {
            response.sendRedirect(
                    response.encodeRedirectURL(this.getCurrentUrl(request, start, end, chart)));
            return;
        }

        session.setAttribute("start", start);
        session.setAttribute("end", end);
        session.setAttribute("availabilityChart", chart);

        Double period = null;
        String selectionMessage = null;
        List<ExpHallHourTotals> totalsList = null;
        List<Double> expUnknownList = new ArrayList<>();
        Long[] hallScheduledArray = null;

        if (start != null && end != null) {
            if (start.after(end)) {
                throw new ServletException("start date cannot be after end date");
            }

            period = (end.getTime() - start.getTime()) / 1000.0 / 60 / 60;

            totalsList = expHourService.findExpHallHourTotals(start, end);

            for (ExpHallHourTotals totals : totalsList) {
                Double unknown = period - (totals.getAbuSeconds() + totals.getBanuSeconds() + totals.getBnaSeconds() + totals.getAccSeconds() + totals.getOffSeconds()) / 3600;
                expUnknownList.add(unknown);
            }

            hallScheduledArray = pdShiftService.findScheduledHallTime(start, end);

            selectionMessage = TimeUtil.formatSmartRangeSeparateTime(start, end);
        }

        request.setAttribute("start", start);
        request.setAttribute("end", end);
        request.setAttribute("chart", chart);
        request.setAttribute("selectionMessage", selectionMessage);
        request.setAttribute("totalsList", totalsList);
        request.setAttribute("period", period);
        request.setAttribute("expUnknownList", expUnknownList);
        request.setAttribute("hallScheduledArray", hallScheduledArray);

        request.getRequestDispatcher("/WEB-INF/views/reports/hall-availability.jsp").forward(request,
                response);
    }

    private String getCurrentUrl(HttpServletRequest request, Date start, Date end, String chart) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm");

        return request.getContextPath() + "/reports/hall-availability?start="
                + URLEncoder.encode(dateFormat.format(
                start), StandardCharsets.UTF_8) + "&end=" + URLEncoder.encode(
                dateFormat.format(end), StandardCharsets.UTF_8) + "&availability-chart=" + URLEncoder.encode(chart, StandardCharsets.UTF_8);
    }
}
