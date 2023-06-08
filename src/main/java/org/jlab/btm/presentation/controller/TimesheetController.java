package org.jlab.btm.presentation.controller;

import org.jlab.btm.business.service.*;
import org.jlab.btm.business.util.BtmTimeUtil;
import org.jlab.btm.persistence.entity.*;
import org.jlab.btm.persistence.enumeration.DurationUnits;
import org.jlab.btm.persistence.enumeration.TimesheetType;
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
@WebServlet(name = "TimesheetController", urlPatterns = {"/timesheet"})
public class TimesheetController extends HttpServlet {

    private final static Logger logger = Logger.getLogger(
            TimesheetController.class.getName());

    @EJB
    CcAccHourService accHourService;
    @EJB
    CcHallHourService hallHourService;
    @EJB
    CcMultiplicityHourService multiplicityHourService;
    @EJB
    ExpHourService expHourService;
    @EJB
    CcShiftService ccShiftService;
    @EJB
    ExpShiftService expShiftService;
    @EJB
    CcCrossCheckCommentService crossCheckCommentService;
    @EJB
    CcSignatureService ccSignatureService;
    @EJB
    ExpSignatureService expSignatureService;
    @EJB
    PdShiftPlanService planService;
    @EJB
    DowntimeService downService;
    @EJB
    ExpSecurityRuleService ruleService;
    @EJB
    ExpShiftPurposeService purposeService;
    @EJB
    ExpHourReasonTimeService reasonTimeService;
    @EJB
    ExpReasonService reasonService;

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

        boolean redirect = false;

        TimesheetType type = BtmParamConverter.convertTimesheetType(request, "type");

        if(type == null) {
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

        if(shift == null) {
            if(TimesheetType.CC == type) {
                shift = TimeUtil.calculateCrewChiefShift(now);
            } else {
                shift = BtmTimeUtil.calculateExperimenterShift(now);
            }

            redirect = true;
        }

        if (day == null) {
            if(TimesheetType.CC == type) {
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

        DurationUnits units = BtmParamConverter.convertDurationUnits(request, "units",null);

        if(units == null) {
            if(TimesheetType.CC == type) {
                units = DurationUnits.HOURS;
            } else {
                units = DurationUnits.MINUTES;
            }

            redirect = true;
        }

        if(redirect) {
            response.sendRedirect(response.encodeRedirectURL(this.getCurrentUrl(request, type, day, shift,
                    units)));
            return;
        }

        String previousUrl = getPreviousUrl(request, type, day, shift, units);
        String nextUrl = getNextUrl(request, type, day, shift, units);

        Date startHour, endHour;

        if(TimesheetType.CC == type) {
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

        if(TimesheetType.CC.equals(type)) {
            handleCCTimesheet(request, startHour, endHour, startOfNextShift);
        } else {
            Hall hall;

            switch(type) {
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

            handleExpTimesheet(request, hall, startHour, endHour);
        }

        dateFormat = new SimpleDateFormat("dd MMM yyyy");

        String message = "Type \"" + type.getLabel() + "\" and Date \"" + dateFormat.format(day) + "\" and Shift \"" + shift + "\" and Units \"" + units.toString().toLowerCase() + "\"";

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
            request.getRequestDispatcher("/WEB-INF/views/cross-check-only.jsp").forward(
                    request, response);
        } else {
            request.getRequestDispatcher("/WEB-INF/views/timesheet.jsp").forward(
                    request, response);
        }
    }

    private void handleCCTimesheet(HttpServletRequest request, Date startHour, Date endHour, Date startOfNextShift) {
        PdShiftPlan plan = planService.findInDatabase(startHour);

        /*ACCELERATOR AVAILABILITY*/
        AcceleratorShiftAvailability accAvailability = accHourService.getAcceleratorAvailability(
                startHour,
                endHour, true, plan);

        /*HALL AVAILABILITY*/
        List<CcHallShiftAvailability> hallAvailabilityList = hallHourService.getHallAvailablilityList(
                startHour, endHour, true, plan);

        List<List<CcHallHour>> hallHoursList = new ArrayList<>();
        hallHoursList.add(hallAvailabilityList.get(0).getEpicsHourList());
        hallHoursList.add(hallAvailabilityList.get(1).getEpicsHourList());
        hallHoursList.add(hallAvailabilityList.get(2).getEpicsHourList());
        hallHoursList.add(hallAvailabilityList.get(3).getEpicsHourList());

        /*MULTIPLICITY AVAILABILITY*/
        MultiplicityShiftAvailability multiplicityAvailability
                = multiplicityHourService.getMultiShiftAvailability(startHour,
                endHour, true, hallHoursList);

        /*EXPERIMENTAL HALL PERSPECTIVE*/
        List<ExpShiftTotals> expHallHourTotalsList = expHourService.findExpHallShiftTotals(
                startHour, endHour);

        List<ExpShiftAvailability> expHallAvailabilityList = expHourService.findAvailability(startHour, endHour);

        /*SHIFT INFORMATION*/
        CcShift dbShiftInfo = ccShiftService.findInDatabase(startHour);
        CcShift epicsShiftInfo = null;

        try {
            epicsShiftInfo = ccShiftService.findInEpics(startHour);
        } catch (UserFriendlyException e) {
            logger.log(Level.FINEST, "Unable to obtain EPICS shift info data", e);
        }

        CcShift shiftInfo = dbShiftInfo;

        if (dbShiftInfo == null) {
            shiftInfo = epicsShiftInfo;
        }

        /*CROSS CHECK COMMENT*/
        CcCrossCheckComment crossCheckComment = crossCheckCommentService.findInDatabase(startHour);

        /*SIGNATURES*/
        List<CcSignature> signatureList = ccSignatureService.find(startHour);
        CcTimesheetStatus status = ccSignatureService.calculateStatus(startHour, endHour,
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

        CcBeamModeCrossCheck modeCrossCheck = new CcBeamModeCrossCheck(
                accAvailability.getShiftTotals(), hallAvailabilityList.get(0).getShiftTotals(),
                hallAvailabilityList.get(1).getShiftTotals(),
                hallAvailabilityList.get(2).getShiftTotals(),
                hallAvailabilityList.get(3).getShiftTotals());

        CcAcceleratorCrossCheck accCrossCheck = new CcAcceleratorCrossCheck(
                accAvailability.getShiftTotals(),
                expHallHourTotalsList.get(0), expHallHourTotalsList.get(1),
                expHallHourTotalsList.get(2), expHallHourTotalsList.get(3));

        CcHallCrossCheck hallCrossCheck = new CcHallCrossCheck(
                hallAvailabilityList.get(0).getShiftTotals(),
                hallAvailabilityList.get(1).getShiftTotals(),
                hallAvailabilityList.get(2).getShiftTotals(),
                hallAvailabilityList.get(3).getShiftTotals(),
                expHallHourTotalsList.get(0), expHallHourTotalsList.get(1),
                expHallHourTotalsList.get(2), expHallHourTotalsList.get(3));

        CcMultiplicityCrossCheck multiCrossCheck = new CcMultiplicityCrossCheck(
                hallAvailabilityList.get(0).getShiftTotals(),
                hallAvailabilityList.get(1).getShiftTotals(),
                hallAvailabilityList.get(2).getShiftTotals(),
                hallAvailabilityList.get(3).getShiftTotals(),
                multiplicityAvailability.getShiftTotals());

        // Downtime check
        DowntimeSummaryTotals dtmTotals = downService.reportTotals(startHour, startOfNextShift);

        CcDowntimeCrossCheck downCrossCheck = new CcDowntimeCrossCheck(accAvailability.getShiftTotals(), dtmTotals.getEventSeconds());

        request.setAttribute("plan", plan);
        request.setAttribute("accAvailability", accAvailability);
        request.setAttribute("hallAvailabilityList", hallAvailabilityList);
        request.setAttribute("multiplicityAvailability", multiplicityAvailability);
        request.setAttribute("expHallHourTotalsList", expHallHourTotalsList);
        request.setAttribute("crossCheckComment", crossCheckComment);
        request.setAttribute("shiftInfo", shiftInfo);
        request.setAttribute("epicsShiftInfo", epicsShiftInfo);
        request.setAttribute("signatureList", signatureList);
        request.setAttribute("status", status);
        request.setAttribute("editable", editable);
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
    }

    private void handleExpTimesheet(HttpServletRequest request, Hall hall, Date startHour, Date endHour) {
        /*AVAILABILITY*/
        ExpShiftAvailability expAvailability = expHourService.getHallAvailability(hall,
                startHour,
                endHour, true);

        /*REASONS NOT READY*/
        List<ExpReason> reasonList = reasonService.findByActive(hall, true);
        List<ExpHourReasonTime> reasonsNotReady = reasonTimeService.find(hall, startHour, endHour);

        /*SHIFT INFORMATION*/
        ExpShift shiftInfo = expShiftService.find(hall, startHour);

        /*SIGNATURES*/
        List<ExpSignature> signatureList = expSignatureService.find(hall, startHour);
        ExpTimesheetStatus status = expSignatureService.calculateStatus(startHour, endHour,
                expAvailability.getDbHourList(),
                reasonsNotReady,
                shiftInfo, signatureList);

        /*Purposes*/
        List<ExpShiftPurpose> experimentList = purposeService.findActiveExperimentsByHall(hall);
        List<ExpShiftPurpose> nonexperimentList = purposeService.findActiveNonExperimentsByHall(hall);

        boolean editable = ruleService.isEditAllowed(hall, startHour);

        request.setAttribute("status", status);
        request.setAttribute("editable", editable);
        request.setAttribute("reasonList", reasonList);
        request.setAttribute("availability", expAvailability);
        request.setAttribute("shiftInfo", shiftInfo);
        request.setAttribute("signatureList", signatureList);
        request.setAttribute("experimentList", experimentList);
        request.setAttribute("nonexperimentList", nonexperimentList);
    }

    private String getCurrentUrl(HttpServletRequest request, TimesheetType type, Date day, Shift shift,
                                 DurationUnits units) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

        return request.getContextPath() + "/timesheet/" + type.toString().toLowerCase() + "/" + dateFormat.format(
                day).toLowerCase() + "/" + shift.toString().toLowerCase() + "/"
                + units.toString().toLowerCase();
    }

    private String getPreviousUrl(HttpServletRequest request, TimesheetType type, Date day, Shift shift,
                                  DurationUnits units) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

        Date previousDay = day;

        Shift previousShift = shift.getPrevious();

        if (previousShift == Shift.SWING) {
            previousDay = TimeUtil.addDays(day, -1);
        }

        return request.getContextPath() + "/timesheet/" + type.toString().toLowerCase() + "/" + dateFormat.format(
                previousDay).toLowerCase() + "/" + previousShift.toString().toLowerCase() + "/"
                + units.toString().toLowerCase();
    }

    private String getNextUrl(HttpServletRequest request, TimesheetType type, Date day, Shift shift,
                              DurationUnits units) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

        Date nextDay = day;

        Shift nextShift = shift.getNext();

        if (nextShift == Shift.OWL) {
            nextDay = TimeUtil.addDays(day, 1);
        }

        return request.getContextPath() + "/timesheet/" + type.toString().toLowerCase() + "/" + dateFormat.format(
                nextDay).toLowerCase() + "/" + nextShift.toString().toLowerCase() + "/"
                + units.toString().toLowerCase();
    }
}
