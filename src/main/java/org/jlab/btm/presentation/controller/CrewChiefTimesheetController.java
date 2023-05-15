package org.jlab.btm.presentation.controller;

import org.jlab.btm.business.service.*;
import org.jlab.btm.persistence.entity.*;
import org.jlab.btm.persistence.enumeration.DurationUnits;
import org.jlab.btm.persistence.projection.*;
import org.jlab.btm.presentation.util.BtmParamConverter;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.enumeration.Hall;
import org.jlab.smoothness.persistence.enumeration.Shift;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ryans
 */
@WebServlet(name = "CrewChiefTimesheetController", urlPatterns
        = {"/crew-chief-timesheet"})
public class CrewChiefTimesheetController extends HttpServlet {

    private final static Logger logger = Logger.getLogger(
            CrewChiefTimesheetController.class.getName());

    @EJB
    OpAccHourService accHourService;
    @EJB
    OpHallHourService hallHourService;
    @EJB
    OpMultiplicityHourService multiplicityHourService;
    @EJB
    ExpHallHourService expHallHourService;
    @EJB
    OpShiftService shiftService;
    @EJB
    OpCrossCheckCommentService crossCheckCommentService;
    @EJB
    OpSignatureService signatureService;
    @EJB
    PdShiftPlanService planService;
    @EJB
    DowntimeService downService;

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

        Date now = new Date();
        Date day;
        try {
            day = BtmParamConverter.convertISO8601Date(request, "day", null);
        } catch (ParseException ex) {
            throw new ServletException("Unable to parse date", ex);
        }

        Shift shift = BtmParamConverter.convertShift(request, "shift",
                TimeUtil.calculateCrewChiefShift(now));

        DurationUnits units = BtmParamConverter.convertDurationUnits(request, "units",
                DurationUnits.HOURS);

        if (day == null) {
            day = TimeUtil.getCurrentCrewChiefShiftDay(now);

            if (TimeUtil.isFirstHourOfCrewChiefShift(now)) {
                shift = shift.getPrevious();

                if (shift == Shift.SWING) {
                    day = TimeUtil.addDays(day, -1);
                }
            }

            response.sendRedirect(response.encodeRedirectURL(this.getCurrentUrl(request, day, shift,
                    units)));
            return;
        }

        String previousUrl = getPreviousUrl(request, day, shift, units);
        String nextUrl = getNextUrl(request, day, shift, units);

        Date startHour = TimeUtil.getCrewChiefStartDayAndHour(day, shift);
        Date endHour = TimeUtil.getCrewChiefEndDayAndHour(day, shift);

        long hoursInShift = TimeUtil.differenceInHours(startHour, endHour) + 1;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH");
        String shiftStartHourStr = dateFormat.format(startHour);

        PdShiftPlan plan = planService.findInDatabase(startHour);

        /*ACCELERATOR AVAILABILITY*/
        AcceleratorShiftAvailability accAvailability = accHourService.getAcceleratorAvailability(
                startHour,
                endHour, true, plan);

        /*HALL AVAILABILITY*/
        List<OpHallShiftAvailability> hallAvailabilityList = hallHourService.getHallAvailablilityList(
                startHour, endHour, true, plan);

        List<List<OpHallHour>> hallHoursList = new ArrayList<>();
        hallHoursList.add(hallAvailabilityList.get(0).getEpicsHourList());
        hallHoursList.add(hallAvailabilityList.get(1).getEpicsHourList());
        hallHoursList.add(hallAvailabilityList.get(2).getEpicsHourList());
        hallHoursList.add(hallAvailabilityList.get(3).getEpicsHourList());

        /*MULTIPLICITY AVAILABILITY*/
        MultiplicityShiftAvailability multiplicityAvailability
                = multiplicityHourService.getMultiShiftAvailability(startHour,
                endHour, true, hallHoursList);

        /*EXPERIMENTAL HALL PERSPECTIVE*/
        List<ExpHallShiftTotals> expHallHourTotalsList = expHallHourService.findExpHallShiftTotals(
                startHour, endHour);

        List<ExpHallShiftAvailability> expHallAvailabilityList = expHallHourService.findAvailability(startHour, endHour);

        /*SHIFT INFORMATION*/
        OpShift dbShiftInfo = shiftService.findInDatabase(startHour);
        OpShift epicsShiftInfo = null;

        try {
            epicsShiftInfo = shiftService.findInEpics(startHour);
        } catch (UserFriendlyException e) {
            logger.log(Level.FINEST, "Unable to obtain EPICS shift info data", e);
        }

        OpShift shiftInfo = dbShiftInfo;

        if (dbShiftInfo == null) {
            shiftInfo = epicsShiftInfo;
        }

        /*CROSS CHECK COMMENT*/
        OpCrossCheckComment crossCheckComment = crossCheckCommentService.findInDatabase(startHour);

        /*SIGNATURES*/
        List<OpSignature> signatureList = signatureService.find(startHour);
        TimesheetStatus status = signatureService.calculateStatus(startHour, endHour,
                accAvailability.getDbHourList(), hallAvailabilityList.get(0).getDbHourList(),
                hallAvailabilityList.get(1).getDbHourList(),
                hallAvailabilityList.get(2).getDbHourList(),
                hallAvailabilityList.get(3).getDbHourList(),
                multiplicityAvailability.getDbHourList(),
                dbShiftInfo, signatureList);

        boolean editable =
                request.isUserInRole("btm-admin") || request.isUserInRole("cc");

        /*Cross Check*/
        HourlyCrossCheckService crossCheckService = new HourlyCrossCheckService();
        List<HallHourCrossCheck> hallAHourCrossCheckList = crossCheckService.getHourList(Hall.A, accAvailability, multiplicityAvailability, hallAvailabilityList.get(0), expHallAvailabilityList.get(0));
        List<HallHourCrossCheck> hallBHourCrossCheckList = crossCheckService.getHourList(Hall.B, accAvailability, multiplicityAvailability, hallAvailabilityList.get(1), expHallAvailabilityList.get(1));
        List<HallHourCrossCheck> hallCHourCrossCheckList = crossCheckService.getHourList(Hall.C, accAvailability, multiplicityAvailability, hallAvailabilityList.get(2), expHallAvailabilityList.get(2));
        List<HallHourCrossCheck> hallDHourCrossCheckList = crossCheckService.getHourList(Hall.D, accAvailability, multiplicityAvailability, hallAvailabilityList.get(3), expHallAvailabilityList.get(3));

        CrewChiefBeamModeCrossCheck modeCrossCheck = new CrewChiefBeamModeCrossCheck(
                accAvailability.getShiftTotals(), hallAvailabilityList.get(0).getShiftTotals(),
                hallAvailabilityList.get(1).getShiftTotals(),
                hallAvailabilityList.get(2).getShiftTotals(),
                hallAvailabilityList.get(3).getShiftTotals());

        CrewChiefAcceleratorCrossCheck accCrossCheck = new CrewChiefAcceleratorCrossCheck(
                accAvailability.getShiftTotals(),
                expHallHourTotalsList.get(0), expHallHourTotalsList.get(1),
                expHallHourTotalsList.get(2), expHallHourTotalsList.get(3));

        CrewChiefHallCrossCheck hallCrossCheck = new CrewChiefHallCrossCheck(
                hallAvailabilityList.get(0).getShiftTotals(),
                hallAvailabilityList.get(1).getShiftTotals(),
                hallAvailabilityList.get(2).getShiftTotals(),
                hallAvailabilityList.get(3).getShiftTotals(),
                expHallHourTotalsList.get(0), expHallHourTotalsList.get(1),
                expHallHourTotalsList.get(2), expHallHourTotalsList.get(3));

        CrewChiefMultiplicityCrossCheck multiCrossCheck = new CrewChiefMultiplicityCrossCheck(
                hallAvailabilityList.get(0).getShiftTotals(),
                hallAvailabilityList.get(1).getShiftTotals(),
                hallAvailabilityList.get(2).getShiftTotals(),
                hallAvailabilityList.get(3).getShiftTotals(),
                multiplicityAvailability.getShiftTotals());

        // Downtime check
        Date startOfNextShift = TimeUtil.addHours(endHour, 1);

        DowntimeSummaryTotals dtmTotals = downService.reportTotals(startHour, startOfNextShift);

        CrewChiefDowntimeCrossCheck downCrossCheck = new CrewChiefDowntimeCrossCheck(accAvailability.getShiftTotals(), dtmTotals.getEventSeconds());

        request.setAttribute("day", day);
        request.setAttribute("startHour", startHour);
        request.setAttribute("startOfNextShift", startOfNextShift);
        request.setAttribute("endHour", endHour);
        request.setAttribute("shift", shift);
        request.setAttribute("previousUrl", previousUrl);
        request.setAttribute("nextUrl", nextUrl);
        request.setAttribute("plan", plan);
        request.setAttribute("accAvailability", accAvailability);
        request.setAttribute("hallAvailabilityList", hallAvailabilityList);
        request.setAttribute("multiplicityAvailability", multiplicityAvailability);
        request.setAttribute("expHallHourTotalsList", expHallHourTotalsList);
        request.setAttribute("durationUnits", units);
        request.setAttribute("crossCheckComment", crossCheckComment);
        request.setAttribute("shiftInfo", shiftInfo);
        request.setAttribute("epicsShiftInfo", epicsShiftInfo);
        request.setAttribute("shiftStartHourStr", shiftStartHourStr);
        request.setAttribute("signatureList", signatureList);
        request.setAttribute("status", status);
        request.setAttribute("editable", editable);
        request.setAttribute("hoursInShift", hoursInShift);
        request.setAttribute("now", new Date());
        request.setAttribute("modeCrossCheck", modeCrossCheck);
        request.setAttribute("accCrossCheck", accCrossCheck);
        request.setAttribute("hallCrossCheck", hallCrossCheck);
        request.setAttribute("multiCrossCheck", multiCrossCheck);
        request.setAttribute("downCrossCheck", downCrossCheck);
        request.setAttribute("dtmTotals", dtmTotals);
        request.setAttribute("hallAHourCrossCheckList", hallAHourCrossCheckList);
        request.setAttribute("hallBHourCrossCheckList", hallBHourCrossCheckList);
        request.setAttribute("hallCHourCrossCheckList", hallCHourCrossCheckList);
        request.setAttribute("hallDHourCrossCheckList", hallDHourCrossCheckList);

        if ("Y".equals(request.getParameter("crosscheck"))) {
            request.getRequestDispatcher("/WEB-INF/views/cross-check-only.jsp").forward(
                    request, response);
        } else {
            request.getRequestDispatcher("/WEB-INF/views/crew-chief-timesheet.jsp").forward(
                    request, response);
        }
    }

    private String getCurrentUrl(HttpServletRequest request, Date day, Shift shift,
                                 DurationUnits units) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

        return request.getContextPath() + "/crew-chief-timesheet/" + dateFormat.format(
                day).toLowerCase() + "/" + shift.toString().toLowerCase() + "/"
                + units.toString().toLowerCase();
    }

    private String getPreviousUrl(HttpServletRequest request, Date day, Shift shift,
                                  DurationUnits units) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

        Date previousDay = day;

        Shift previousShift = shift.getPrevious();

        if (previousShift == Shift.SWING) {
            previousDay = TimeUtil.addDays(day, -1);
        }

        return request.getContextPath() + "/crew-chief-timesheet/" + dateFormat.format(
                previousDay).toLowerCase() + "/" + previousShift.toString().toLowerCase() + "/"
                + units.toString().toLowerCase();
    }

    private String getNextUrl(HttpServletRequest request, Date day, Shift shift,
                              DurationUnits units) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

        Date nextDay = day;

        Shift nextShift = shift.getNext();

        if (nextShift == Shift.OWL) {
            nextDay = TimeUtil.addDays(day, 1);
        }

        return request.getContextPath() + "/crew-chief-timesheet/" + dateFormat.format(
                nextDay).toLowerCase() + "/" + nextShift.toString().toLowerCase() + "/"
                + units.toString().toLowerCase();
    }
}
