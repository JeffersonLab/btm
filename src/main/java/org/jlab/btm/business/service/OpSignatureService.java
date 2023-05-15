package org.jlab.btm.business.service;

import org.jlab.btm.persistence.entity.*;
import org.jlab.btm.persistence.enumeration.Role;
import org.jlab.btm.persistence.projection.TimesheetStatus;
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
public class OpSignatureService extends AbstractService<OpSignature> {

    @EJB
    StaffService staffService;
    @EJB
    OpAccHourService accHourService;
    @EJB
    OpHallHourService hallHourService;
    @EJB
    OpMultiplicityHourService multiHourService;
    @EJB
    OpShiftService shiftService;
    @PersistenceContext(unitName = "btmPU")
    private EntityManager em;

    public OpSignatureService() {
        super(OpSignature.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @PermitAll
    public List<OpSignature> find(Date startDayAndHour) {
        TypedQuery<OpSignature> query = em.createQuery(
                "select a from OpSignature a where a.startDayAndHour = :startDayAndHour order by a.signedDate asc",
                OpSignature.class);

        query.setParameter("startDayAndHour", startDayAndHour);

        return query.getResultList();
    }

    @PermitAll
    public TimesheetStatus calculateStatus(Date startDayAndHour, Date endDayAndHour,
                                           List<OpAccHour> accHourList, List<OpHallHour> hallAHourList,
                                           List<OpHallHour> hallBHourList, List<OpHallHour> hallCHourList,
                                           List<OpHallHour> hallDHourList, List<OpMultiplicityHour> multiHourList, OpShift shift,
                                           List<OpSignature> signatureList) {
        TimesheetStatus status = new TimesheetStatus();

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
    public TimesheetStatus calculateStatus(Date startDayAndHour) {

        Date endDayAndHour = TimeUtil.calculateCrewChiefShiftEndDayAndHour(startDayAndHour);

        List<OpAccHour> accHourList = accHourService.findInDatabase(startDayAndHour, endDayAndHour);

        List<OpHallHour> hallAHourList = hallHourService.findInDatabase(Hall.A, startDayAndHour,
                endDayAndHour);
        List<OpHallHour> hallBHourList = hallHourService.findInDatabase(Hall.B, startDayAndHour,
                endDayAndHour);
        List<OpHallHour> hallCHourList = hallHourService.findInDatabase(Hall.C, startDayAndHour,
                endDayAndHour);
        List<OpHallHour> hallDHourList = hallHourService.findInDatabase(Hall.D, startDayAndHour,
                endDayAndHour);

        List<OpMultiplicityHour> multiHourList = multiHourService.findInDatabase(startDayAndHour,
                endDayAndHour);

        OpShift shift = shiftService.findInDatabase(startDayAndHour);

        List<OpSignature> signatureList = find(startDayAndHour);

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

        username = username.split(":")[2];

        Staff staff = staffService.findByUsername(username);

        if (staff == null) {
            throw new UserFriendlyException("User not found in staff database: " + username);
        }

        List<OpSignature> signatureList = find(startDayAndHour);

        for (OpSignature sig : signatureList) {
            if (sig.getStartDayAndHour().getTime() == startDayAndHour.getTime()
                    && sig.getSignedBy().equals(staff) && sig.getSignedRole() == role) {
                throw new UserFriendlyException("User has already signed the timesheet");
            }
        }

        TimesheetStatus status = this.calculateStatus(startDayAndHour);

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

        OpSignature signature = new OpSignature();
        signature.setStartDayAndHour(startDayAndHour);
        signature.setSignedDate(new Date());
        signature.setSignedRole(role);
        signature.setSignedBy(staff);

        create(signature);
    }
}
