package org.jlab.btm.business.service;

import java.util.Date;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.jlab.btm.persistence.entity.*;
import org.jlab.btm.persistence.enumeration.Role;
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
    ExpHourReasonTimeService reasonTimeService;
    @EJB
    ExpHourService expHourService;
    @EJB
    ExpShiftService shiftService;

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

        List<ExpHour> availabilityList = expHourService.findInDatabase(hall, startDayAndHour, endDayAndHour);

        List<ExpHourReasonTime> explanationsList = reasonTimeService.find(hall, startDayAndHour, endDayAndHour);

        ExpShift shift = shiftService.find(hall, startDayAndHour);

        List<ExpSignature> signatureList = find(hall, startDayAndHour);

        return this.calculateStatus(startDayAndHour, endDayAndHour, availabilityList, explanationsList, shift, signatureList);
    }

    @PermitAll
    public ExpTimesheetStatus calculateStatus(Date startDayAndHour, Date endDayAndHour, List<ExpHour> expAvailabilityList,
                                              List<ExpHourReasonTime> reasonsNotReadyList, ExpShift shiftInfo,
                                              List<ExpSignature> signatureList) {
        ExpTimesheetStatus status = new ExpTimesheetStatus();

        long hoursInShift = TimeUtil.differenceInHours(startDayAndHour, endDayAndHour) + 1;

        if (expAvailabilityList != null && expAvailabilityList.size() == hoursInShift) {
            status.setAvailabilityComplete(true);
        }

        List<HourReasonDiscrepancy> discrepancies = reasonTimeService.validateUED(expAvailabilityList, reasonsNotReadyList);

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
            if (sig.getStartDayAndHour().getTime() == startDayAndHour.getTime()
                    && sig.getSignedBy().equals(username) && sig.getSignedRole() == role) {
                throw new UserFriendlyException("User has already signed the timesheet");
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
}
