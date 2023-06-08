package org.jlab.btm.business.service;

import org.jlab.btm.persistence.entity.*;
import org.jlab.btm.persistence.enumeration.Role;
import org.jlab.btm.persistence.projection.CcTimesheetStatus;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.enumeration.Hall;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

/**
 * @author ryans
 */
@Stateless
public class CcSignatureService extends AbstractService<CcSignature> {

    @EJB
    CcAccHourService accHourService;
    @EJB
    CcHallHourService hallHourService;
    @EJB
    CcMultiplicityHourService multiHourService;
    @EJB
    CcShiftService shiftService;
    @PersistenceContext(unitName = "btmPU")
    private EntityManager em;

    public CcSignatureService() {
        super(CcSignature.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @PermitAll
    public List<CcSignature> find(Date startDayAndHour) {
        TypedQuery<CcSignature> query = em.createQuery(
                "select a from CcSignature a where a.startDayAndHour = :startDayAndHour order by a.signedDate asc",
                CcSignature.class);

        query.setParameter("startDayAndHour", startDayAndHour);

        return query.getResultList();
    }

    @PermitAll
    public CcTimesheetStatus calculateStatus(Date startDayAndHour, Date endDayAndHour,
                                             List<CcAccHour> accHourList, List<CcHallHour> hallAHourList,
                                             List<CcHallHour> hallBHourList, List<CcHallHour> hallCHourList,
                                             List<CcHallHour> hallDHourList, List<CcMultiplicityHour> multiHourList, CcShift shift,
                                             List<CcSignature> signatureList) {
        CcTimesheetStatus status = new CcTimesheetStatus();

        long hoursInShift = TimeUtil.differenceInHours(startDayAndHour, endDayAndHour) + 1;

        if (accHourList != null && accHourList.size() == hoursInShift) {
            status.setAcceleratorComplete(true);
        }

        if (hallAHourList != null && hallAHourList.size() == hoursInShift) {
            status.setHallAComplete(true);
        }

        if (hallBHourList != null && hallBHourList.size() == hoursInShift) {
            status.setHallBComplete(true);
        }

        if (hallCHourList != null && hallCHourList.size() == hoursInShift) {
            status.setHallCComplete(true);
        }

        if (hallDHourList != null && hallDHourList.size() == hoursInShift) {
            status.setHallDComplete(true);
        }

        if (multiHourList != null && multiHourList.size() == hoursInShift) {
            status.setMultiplicityComplete(true);
        }

        if (shift != null) {
            status.setShiftInfoComplete(true);
        }

        if (signatureList != null && !signatureList.isEmpty()) {
            status.setSignatureComplete(true);
        }

        return status;
    }

    @PermitAll
    public CcTimesheetStatus calculateStatus(Date startDayAndHour) {

        Date endDayAndHour = TimeUtil.calculateCrewChiefShiftEndDayAndHour(startDayAndHour);

        List<CcAccHour> accHourList = accHourService.findInDatabase(startDayAndHour, endDayAndHour);

        List<CcHallHour> hallAHourList = hallHourService.findInDatabase(Hall.A, startDayAndHour,
                endDayAndHour);
        List<CcHallHour> hallBHourList = hallHourService.findInDatabase(Hall.B, startDayAndHour,
                endDayAndHour);
        List<CcHallHour> hallCHourList = hallHourService.findInDatabase(Hall.C, startDayAndHour,
                endDayAndHour);
        List<CcHallHour> hallDHourList = hallHourService.findInDatabase(Hall.D, startDayAndHour,
                endDayAndHour);

        List<CcMultiplicityHour> multiHourList = multiHourService.findInDatabase(startDayAndHour,
                endDayAndHour);

        CcShift shift = shiftService.findInDatabase(startDayAndHour);

        List<CcSignature> signatureList = find(startDayAndHour);

        return this.calculateStatus(startDayAndHour, endDayAndHour, accHourList, hallAHourList,
                hallBHourList, hallCHourList,
                hallDHourList, multiHourList, shift, signatureList);
    }

    @RolesAllowed({"cc", "btm-admin"})
    public void signTimesheet(Date startDayAndHour) throws UserFriendlyException {
        Role role = Role.CREW_CHIEF;

        if (context.isCallerInRole("btm-admin")) {
            role = Role.OPERABILITY_MANAGER;
        }

        String username = context.getCallerPrincipal().getName();

        List<CcSignature> signatureList = find(startDayAndHour);

        for (CcSignature sig : signatureList) {
            if (sig.getStartDayAndHour().getTime() == startDayAndHour.getTime()
                    && sig.getSignedBy().equals(username) && sig.getSignedRole() == role) {
                throw new UserFriendlyException("User has already signed the timesheet");
            }
        }

        CcTimesheetStatus status = this.calculateStatus(startDayAndHour);

        if (!status.isAcceleratorComplete()) {
            throw new UserFriendlyException("You must save all accelerator availability hours");
        }

        if (!status.isHallAComplete()) {
            throw new UserFriendlyException("You must save all Hall A availability hours");
        }

        if (!status.isHallBComplete()) {
            throw new UserFriendlyException("You must save all Hall B availability hours");
        }

        if (!status.isHallCComplete()) {
            throw new UserFriendlyException("You must save all Hall C availability hours");
        }

        if (!status.isHallDComplete()) {
            throw new UserFriendlyException("You must save all Hall D availability hours");
        }

        if (!status.isMultiplicityComplete()) {
            throw new UserFriendlyException("You must save all multiplicity hours");
        }

        if (!status.isShiftInfoComplete()) {
            throw new UserFriendlyException("You must save shift information");
        }

        CcSignature signature = new CcSignature();
        signature.setStartDayAndHour(startDayAndHour);
        signature.setSignedDate(new Date());
        signature.setSignedRole(role);
        signature.setSignedBy(username);

        create(signature);
    }
}
