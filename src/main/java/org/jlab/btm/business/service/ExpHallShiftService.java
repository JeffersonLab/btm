package org.jlab.btm.business.service;

import org.jlab.btm.persistence.entity.ExpHallShift;
import org.jlab.smoothness.persistence.enumeration.Hall;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
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
    public Map<Hall, ExpHallShift> getMap(List<ExpHallShift> shiftList) {
        Map<Hall, ExpHallShift> shiftMap = new HashMap<>();

        if (shiftList != null) {
            for (ExpHallShift shift : shiftList) {
                shiftMap.put(shift.getHall(), shift);
            }
        }

        return shiftMap;
    }
}
