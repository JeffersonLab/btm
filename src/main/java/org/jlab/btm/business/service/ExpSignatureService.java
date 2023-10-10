package org.jlab.btm.business.service;

import java.util.Date;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;

import org.jlab.btm.persistence.entity.*;
import org.jlab.btm.persistence.enumeration.Role;
import org.jlab.btm.persistence.projection.ExpShiftAvailability;
import org.jlab.btm.persistence.projection.ExpTimesheetStatus;
import org.jlab.btm.persistence.projection.HourReasonDiscrepancy;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * Responsible for experimenter hall signature business operations.
 *
 * @author ryans
 */
@Stateless
public class ExpSignatureService extends AbstractService<ExpSignature> {

    @PersistenceContext(unitName = "btmPU")
    protected EntityManager em;

    @EJB
    ExpUedExplanationService explanationService;
    @EJB
    ExpHourService hourService;
    @EJB
    ExpShiftService shiftService;
    @EJB
    ExpSecurityRuleService ruleService;
    @EJB
    ExpProgramService programService;
    @EJB
    ExpReasonService reasonService;

    public ExpSignatureService() {
        super(ExpSignature.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @PermitAll
    public List<ExpSignature> find(Hall hall, Date startDayAndHour) {
        TypedQuery<ExpSignature> q = em.createNamedQuery("ExpSignature.findByHallStartDayAndHour", ExpSignature.class);

        q.setParameter("hall", hall);
        q.setParameter("startDayAndHour", startDayAndHour);

        return q.getResultList();
    }

    @PermitAll
    public ExpTimesheetStatus calculateStatus(Hall hall, Date startDayAndHour) {

        Date endDayAndHour = TimeUtil.calculateExperimenterShiftEndDayAndHour(startDayAndHour);

        List<ExpHour> availabilityList = hourService.findInDatabase(hall, startDayAndHour, endDayAndHour);

        List<ExpUedExplanation> explanationsList = explanationService.find(hall, startDayAndHour, endDayAndHour);

        ExpShift shift = shiftService.find(hall, startDayAndHour);

        List<ExpSignature> signatureList = find(hall, startDayAndHour);

        return this.calculateStatus(hall, startDayAndHour, endDayAndHour, availabilityList, explanationsList, shift, signatureList);
    }

    @PermitAll
    public ExpTimesheetStatus calculateStatus(Hall hall, Date startDayAndHour, Date endDayAndHour, List<ExpHour> expAvailabilityList,
                                              List<ExpUedExplanation> reasonsNotReadyList, ExpShift shiftInfo,
                                              List<ExpSignature> signatureList) {
        ExpTimesheetStatus status = new ExpTimesheetStatus();

        long hoursInShift = TimeUtil.differenceInHours(startDayAndHour, endDayAndHour) + 1;

        if (expAvailabilityList != null) {
            if(expAvailabilityList.size() == hoursInShift) {
                status.setAvailabilityComplete(true);
                status.setCcHoursComplete(true);
                // If count of hours is 1 less than entire shift worth AND last hour in ordered list is NOT equal to last hour of shift then missing hour MUST be from last hour of shift
            } else if(expAvailabilityList.size() == (hoursInShift - 1) && !expAvailabilityList.get(expAvailabilityList.size() - 1).getDayAndHour().equals(endDayAndHour)) {
                status.setCcHoursComplete(true);
            }
        }

        Date lastHourOfPreviousShift = TimeUtil.addHours(startDayAndHour, -1);

        ExpHour previousLast = hourService.findInDatabase(hall, lastHourOfPreviousShift);

        if(previousLast != null) {
            status.setPreviousLastHourComplete(true);
        }

        List<HourReasonDiscrepancy> discrepancies = explanationService.validateUED(expAvailabilityList, reasonsNotReadyList);

        status.setReasonDiscrepancyList(discrepancies);

        if(discrepancies.isEmpty()) {
            status.setReasonsNotReadyComplete(true);
        }

        if (shiftInfo != null) {
            status.setShiftInfoComplete(true);
        }

        if (signatureList != null && !signatureList.isEmpty()) {
            status.setSignatureComplete(true);
        }

        return status;
    }

    @PermitAll
    public void signTimesheet(Hall hall, Date startDayAndHour) throws UserFriendlyException {
        Role role = Role.USER;

        if (context.isCallerInRole("btm-admin")) {
            role = Role.OPERABILITY_MANAGER;
        }

        String username = context.getCallerPrincipal().getName();

        List<ExpSignature> signatureList = find(hall, startDayAndHour);

        for (ExpSignature sig : signatureList) {
            if (sig.getSignedBy().equals(username)) {
                throw new UserFriendlyException("User has already signed the timesheet");
            }
            if(sig.getSignedRole() == role) {
                throw new UserFriendlyException("A " + role.getLabel() + " has already signed the timesheet");
            }
        }

        ExpTimesheetStatus status = this.calculateStatus(hall, startDayAndHour);

        if (!status.isAvailabilityComplete()) {
            throw new UserFriendlyException("You must save all availability hours");
        }

        if (!status.isReasonsNotReadyComplete()) {
            throw new UserFriendlyException("You must explain UED with Reasons Not Ready");
        }

        if (!status.isShiftInfoComplete()) {
            throw new UserFriendlyException("You must save shift information");
        }

        ExpSignature signature = new ExpSignature();
        signature.setHall(hall);
        signature.setStartDayAndHour(startDayAndHour);
        signature.setSignedDate(new Date());
        signature.setSignedRole(role);
        signature.setSignedBy(username);

        create(signature);
    }

    @PermitAll
    public void populateRequestAttributes(HttpServletRequest request, Hall hall, Date startHour, Date endHour) {
        /*AVAILABILITY*/
        ExpShiftAvailability expAvailability = hourService.getHallAvailability(hall,
                startHour,
                endHour, true);

        /*REASONS NOT READY*/
        List<ExpReason> reasonList = reasonService.findByActive(hall, true);
        List<ExpUedExplanation> explanationList = explanationService.find(hall, startHour, endHour);

        int explanationSecondsTotal = 0;
        for(ExpUedExplanation explanation: explanationList) {
            explanationSecondsTotal = explanationSecondsTotal + explanation.getSeconds();
        }

        /*SHIFT INFORMATION*/
        ExpShift shiftInfo = shiftService.find(hall, startHour);

        /*SIGNATURES*/
        List<ExpSignature> signatureList = this.find(hall, startHour);
        ExpTimesheetStatus status = this.calculateStatus(hall, startHour, endHour,
                expAvailability.getDbHourList(),
                explanationList,
                shiftInfo, signatureList);

        /*Purposes*/
        List<ExpProgram> experimentList = programService.findActiveExperimentsByHall(hall);
        List<ExpProgram> nonexperimentList = programService.findActiveNonExperimentsByHall(hall);

        boolean editable = ruleService.isEditAllowed(hall, startHour);

        request.setAttribute("status", status);
        request.setAttribute("editable", editable);
        request.setAttribute("reasonList", reasonList);
        request.setAttribute("explanationList", explanationList);
        request.setAttribute("explanationSecondsTotal", explanationSecondsTotal);
        request.setAttribute("availability", expAvailability);
        request.setAttribute("shiftInfo", shiftInfo);
        request.setAttribute("signatureList", signatureList);
        request.setAttribute("experimentList", experimentList);
        request.setAttribute("nonexperimentList", nonexperimentList);
    }
}
