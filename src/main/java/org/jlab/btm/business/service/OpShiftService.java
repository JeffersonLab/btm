package org.jlab.btm.business.service;

import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import org.jlab.btm.business.service.epics.EpicsShiftService;
import org.jlab.btm.persistence.entity.OpShift;
import org.jlab.btm.persistence.enumeration.DataSource;
import org.jlab.smoothness.business.exception.UserFriendlyException;

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
public class OpShiftService extends AbstractService<OpShift> {

    @EJB
    EpicsShiftService epicsService;
    @PersistenceContext(unitName = "btmPU")
    private EntityManager em;

    public OpShiftService() {
        super(OpShift.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @PermitAll
    public OpShift findInDatabase(Date startDayAndHour) {
        TypedQuery<OpShift> query = em.createQuery(
                "select a from OpShift a where a.startDayAndHour = :startDayAndHour", OpShift.class);

        query.setParameter("startDayAndHour", startDayAndHour);

        List<OpShift> shiftList = query.getResultList();
        OpShift shift = null;

        if (shiftList != null && !shiftList.isEmpty()) {
            shift = shiftList.get(0);
            if (shift != null) {
                shift.setSource(DataSource.DATABASE);
            }
        }

        return shift;
    }

    @PermitAll
    public OpShift findInEpics(Date startHour) throws UserFriendlyException {
        try {
            return epicsService.find(startHour);
        } catch (TimeoutException | InterruptedException | CAException e) {
            throw new UserFriendlyException("Unable to query EPICS", e);
        }
    }

    @RolesAllowed({"cc", "btm-admin"})
    public void editShift(Date startDayAndHour, String crewChief, String operators, String program,
                          String programDeputy, String comments) throws UserFriendlyException {
        OpShift shift = findInDatabase(startDayAndHour);

        if (shift == null) {
            shift = new OpShift();
            shift.setStartDayAndHour(startDayAndHour);
        }

        shift.setCrewChief(crewChief);
        shift.setOperators(operators);
        shift.setProgram(program);
        shift.setProgramDeputy(programDeputy);
        shift.setRemark(comments);

        super.edit(shift);
    }

}
