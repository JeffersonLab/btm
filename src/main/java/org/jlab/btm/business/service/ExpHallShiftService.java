package org.jlab.btm.business.service;

import org.jlab.btm.persistence.entity.ExpHallShift;
import org.jlab.smoothness.persistence.enumeration.Hall;

import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Responsible for experimenter hall shift business operations.
 *
 * @author ryans
 */
@Stateless
public class ExpHallShiftService extends AbstractService<ExpHallShift> {

    @PersistenceContext(unitName = "btmPU")
    private EntityManager em;

    public ExpHallShiftService() {
        super(ExpHallShift.class);
    }

    @EJB
    ExpSecurityRuleService ruleService;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @PermitAll
    public List<ExpHallShift> findByShiftStartAndLoadPurpose(Date startDayAndHour) {
        TypedQuery<ExpHallShift> q = em.createQuery(
                "select a from ExpHallShift a where a.startDayAndHour = :startDayAndHour order by hall desc",
                ExpHallShift.class);

        q.setParameter("startDayAndHour", startDayAndHour);

        List<ExpHallShift> shiftList = q.getResultList();

        if (shiftList != null) {
            for (ExpHallShift shift : shiftList) {
                shift.getExpHallShiftPurpose().getName();
            }
        }

        return shiftList;
    }

    @PermitAll
    public ExpHallShift find(Hall hall, Date startDayAndHour) {
        TypedQuery<ExpHallShift> q = em.createNamedQuery("ExpHallShift.findByHallAndStartDayAndHour", ExpHallShift.class);

        q.setParameter("hall", hall);
        q.setParameter("startDayAndHour", startDayAndHour);

        // We don't use q.getSingleResult() because it throws NoResultException,
        // and we simply want to return null if no result.
        List<ExpHallShift> results = q.getResultList();
        ExpHallShift result = null;

        if(results.size() > 1) {
            throw new NonUniqueResultException("There should only be one shift for a particular hall and start day and hour");
        }
        else if(results.size() == 1) {
            result = results.get(0);
        }

        return result;
    }

    @PermitAll
    public Map<Hall, ExpHallShift> getMap(List<ExpHallShift> shiftList) {
        Map<Hall, ExpHallShift> shiftMap = new HashMap<>();

        if (shiftList != null) {
            for (ExpHallShift shift : shiftList) {
                shiftMap.put(shift.getHall(), shift);
            }
        }

        return shiftMap;
    }

    @Override
    @PermitAll
    public void create(ExpHallShift shift) {
        ruleService.editCheck(shift.getHall(), shift.getStartDayAndHour());

        super.create(shift);
    }

    @Override
    @PermitAll
    public void remove(ExpHallShift shift) {
        ruleService.editCheck(shift.getHall(), shift.getStartDayAndHour());

        super.remove(shift);
    }

    @Override
    @PermitAll
    public void edit(ExpHallShift shift) {
        ruleService.editCheck(shift.getHall(), shift.getStartDayAndHour());

        super.edit(shift);
    }
}
