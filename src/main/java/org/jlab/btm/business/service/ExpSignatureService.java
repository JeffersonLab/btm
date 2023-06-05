package org.jlab.btm.business.service;

import java.util.Date;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.jlab.btm.persistence.entity.ExpHallHour;
import org.jlab.btm.persistence.entity.ExpHallHourReasonTime;
import org.jlab.btm.persistence.entity.ExpHallShift;
import org.jlab.btm.persistence.entity.ExpHallSignature;
import org.jlab.btm.persistence.projection.CcTimesheetStatus;
import org.jlab.btm.persistence.projection.ExpHallShiftAvailability;
import org.jlab.btm.persistence.projection.ExpTimesheetStatus;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * Responsible for experimenter hall signature business operations.
 *
 * @author ryans
 */
@Stateless
public class ExpSignatureService extends AbstractService<ExpHallSignature> {

    @PersistenceContext(unitName = "btmPU")
    protected EntityManager em;

    @EJB
    ExpHallHourReasonTimeService reasonTimeService;

    public ExpSignatureService() {
        super(ExpHallSignature.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @PermitAll
    public List<ExpHallSignature> find(Hall hall, Date startDayAndHour) {
        TypedQuery<ExpHallSignature> q = em.createNamedQuery("ExpHallSignature.findByHallStartDayAndHour", ExpHallSignature.class);

        q.setParameter("hall", hall);
        q.setParameter("startDayAndHour", startDayAndHour);

        return q.getResultList();
    }

    @PermitAll
    public ExpTimesheetStatus calculateStatus(Date startDayAndHour, Date endDayAndHour, List<ExpHallHour> expAvailabilityList,
                                              List<ExpHallHourReasonTime> reasonsNotReadyList, ExpHallShift shiftInfo,
                                              List<ExpHallSignature> signatureList) {
        ExpTimesheetStatus status = new ExpTimesheetStatus();

        long hoursInShift = TimeUtil.differenceInHours(startDayAndHour, endDayAndHour) + 1;

        if (expAvailabilityList != null && expAvailabilityList.size() == hoursInShift) {
            status.setAvailabilityComplete(true);
        }

        List<String> discrepancies = reasonTimeService.validateUED(expAvailabilityList, reasonsNotReadyList);

        status.setUedDiscrepancies(discrepancies);

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
}
