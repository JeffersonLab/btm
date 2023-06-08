package org.jlab.btm.presentation.controller.reports;

import org.jlab.btm.business.service.ExpHourService;
import org.jlab.btm.business.service.CcMultiplicityHourService;
import org.jlab.btm.business.service.PdShiftPlanService;
import org.jlab.btm.persistence.projection.MultiplicitySummaryTotals;
import org.jlab.btm.persistence.projection.PhysicsSummaryTotals;
import org.jlab.btm.presentation.util.BtmParamConverter;
import org.jlab.btm.presentation.util.FilterSelectionMessage;
import org.jlab.smoothness.business.util.TimeUtil;

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
@WebServlet(name = "PhysicsSummary", urlPatterns = {"/reports/physics-summary"})
public class PhysicsSummary extends HttpServlet {

    @EJB
    CcMultiplicityHourService multiplicityService;

    @EJB
    ExpHourService expHourService;

    @EJB
    PdShiftPlanService planService;

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

        String data = request.getParameter("physics-data");

        /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
        HttpSession session = request.getSession(true);
        Date sessionStart = (Date) session.getAttribute("start");
        Date sessionEnd = (Date) session.getAttribute("end");
        String sessionData = (String) session.getAttribute("physicsData");

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

        if (data == null) {
            needRedirect = true;
            if (sessionData != null) {
                data = sessionData;
            } else {
                data = "available";
            }
        }

        if (needRedirect) {
            response.sendRedirect(
                    response.encodeRedirectURL(this.getCurrentUrl(request, start, end, data)));
            return;
        }

        session.setAttribute("start", start);
        session.setAttribute("end", end);
        session.setAttribute("physicsData", data);

        Double period = null;
        String selectionMessage = null;
        MultiplicitySummaryTotals multiplicityTotals = null;
        List<PhysicsSummaryTotals> physicsTotalsList = null;
        List<Double> expUnknownList = new ArrayList<>();
        List<Double> opUnknownList = new ArrayList<>();
        Long[] scheduledArray = null;

        if (start != null && end != null) {
            if (start.after(end)) {
                throw new ServletException("start date cannot be after end date");
            }

            period = (end.getTime() - start.getTime()) / 1000.0 / 60 / 60;

            multiplicityTotals = multiplicityService.reportTotals(start, end);
            physicsTotalsList = expHourService.reportTotals(start, end);
            scheduledArray = planService.findScheduledHallTime(start, end);

            for (PhysicsSummaryTotals totals : physicsTotalsList) {
                Double unknown = period - (totals.getAbuSeconds() + totals.getBanuSeconds() + totals.getBnaSeconds() + totals.getAccSeconds() + totals.getExpOffSeconds()) / 3600;
                expUnknownList.add(unknown);

                unknown = period - (totals.getUpSeconds() + totals.getTuningSeconds() + totals.getBnrSeconds() + totals.getDownSeconds() + totals.getOpOffSeconds()) / 3600;
                opUnknownList.add(unknown);
            }

            selectionMessage = FilterSelectionMessage.getPhysicsReportCaption(data) + "from ";
            selectionMessage = (selectionMessage + " " + TimeUtil.formatSmartRangeSeparateTime(start, end)).trim();

        }

        request.setAttribute("start", start);
        request.setAttribute("end", end);
        request.setAttribute("data", data);
        request.setAttribute("selectionMessage", selectionMessage);
        request.setAttribute("multiplicityTotals", multiplicityTotals);
        request.setAttribute("physicsTotalsList", physicsTotalsList);
        request.setAttribute("period", period);
        request.setAttribute("expUnknownList", expUnknownList);
        request.setAttribute("opUnknownList", opUnknownList);
        request.setAttribute("scheduledArray", scheduledArray);

        request.getRequestDispatcher("/WEB-INF/views/reports/physics-summary.jsp").forward(request,
                response);
    }

    private String getCurrentUrl(HttpServletRequest request, Date start, Date end, String data) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm");

        return request.getContextPath() + "/reports/physics-summary?start="
                + URLEncoder.encode(dateFormat.format(
                start), StandardCharsets.UTF_8) + "&end=" + URLEncoder.encode(
                dateFormat.format(end), StandardCharsets.UTF_8) + "&physics-data=" + URLEncoder.encode(data, StandardCharsets.UTF_8);
    }
}
