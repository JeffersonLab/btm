package org.jlab.btm.business.service;

import org.jlab.btm.persistence.entity.ExpHallShiftPurpose;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.persistence.enumeration.Hall;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Responsible for experimenter hall shift purpose business operations.
 *
 * @author ryans
 */
@Stateless
public class ExpHallShiftPurposeService extends AbstractService<ExpHallShiftPurpose> {

    @PersistenceContext(unitName = "btmPU")
    private EntityManager em;

    public ExpHallShiftPurposeService() {
        super(ExpHallShiftPurpose.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @PermitAll
    public List<ExpHallShiftPurpose> findByHall(Hall hall, Boolean active) {
        String query = "select a from ExpHallShiftPurpose a where hall = :hall ";
        String order = "order by experiment desc, active desc, name asc";

        if (active != null) {
            query = query + "and active = " + (active ? "'1' " : "'0' ");
        }

        query = query + order;

        TypedQuery<ExpHallShiftPurpose> q = em.createQuery(
                query,
                ExpHallShiftPurpose.class);

        q.setParameter("hall", hall);

        return q.getResultList();
    }

    @PermitAll
    public boolean isDuplicate(Hall hall, String name) {
        TypedQuery<Long> q = em.createQuery(
                "select count(a.name) from ExpHallShiftPurpose a where a.hall = :hall and a.name = :name",
                Long.class);

        q.setParameter("hall", hall);
        q.setParameter("name", name);

        Long count = q.getSingleResult();

        return count > 0;
    }

    @RolesAllowed({"cc", "btm-admin", "schcom"})
    public void add(Hall hall, String name, String alias, String url, Boolean experiment, Boolean active) throws
            UserFriendlyException {
        ExpHallShiftPurpose purpose = new ExpHallShiftPurpose();

        if (isDuplicate(hall, name)) {
            throw new UserFriendlyException("Program with name " + name + " already exists");
        }

        purpose.setHall(hall);
        purpose.setName(name);
        purpose.setAlias(alias);
        purpose.setUrl(url);
        purpose.setExperiment(experiment);
        purpose.setActive(active);

        create(purpose);
    }

    @PermitAll
    public boolean isInUse(BigInteger programId) {
        TypedQuery<Long> q = em.createQuery(
                "select count(a.hall) from ExpHallShift a where a.expHallShiftPurpose.expHallShiftPurposeId = :id",
                Long.class);

        q.setParameter("id", programId);

        Long count = q.getSingleResult();

        return count > 0;
    }

    @RolesAllowed({"cc", "btm-admin", "schcom"})
    public void remove(BigInteger programId) throws UserFriendlyException {
        ExpHallShiftPurpose purpose = find(programId);

        if (isInUse(programId)) {
            throw new UserFriendlyException(
                    "You cannot remove a program which is already in use.  Update the Active attribute instead.");
        }

        this.remove(purpose);
    }

    @RolesAllowed({"cc", "btm-admin", "schcom"})
    public void edit(BigInteger programId, String name, String alias, String url, Boolean experiment, Boolean active) throws
            UserFriendlyException {
        if (programId == null) {
            throw new UserFriendlyException("Program Id must not be empty");
        }

        ExpHallShiftPurpose purpose = find(programId);

        if (purpose == null) {
            throw new UserFriendlyException("Program with ID: " + programId + " not found");
        }

        purpose.setName(name);
        purpose.setAlias(alias);
        purpose.setUrl(url);
        purpose.setExperiment(experiment);
        purpose.setActive(active);
    }

    @PermitAll
    public Map<Integer, ExpHallShiftPurpose> findPurposeByIdMap() {
        Map<Integer, ExpHallShiftPurpose> purposeMap = new HashMap<>();

        List<ExpHallShiftPurpose> purposeList = findAll();

        for (ExpHallShiftPurpose purpose : purposeList) {
            purposeMap.put(purpose.getExpHallShiftPurposeId().intValue(), purpose);
        }

        return purposeMap;
    }

    @PermitAll
    public Map<String, ExpHallShiftPurpose> findPurposeByHallNameMap(Hall hall) {
        Map<String, ExpHallShiftPurpose> purposeMap = new HashMap<>();

        List<ExpHallShiftPurpose> purposeList = this.findByHall(hall, null);

        for (ExpHallShiftPurpose purpose : purposeList) {
            purposeMap.put(purpose.getName(), purpose);
        }

        return purposeMap;
    }
}
