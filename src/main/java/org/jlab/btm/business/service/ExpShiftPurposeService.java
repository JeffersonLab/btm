package org.jlab.btm.business.service;

import org.jlab.btm.persistence.entity.ExpShiftPurpose;
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
public class ExpShiftPurposeService extends AbstractService<ExpShiftPurpose> {

    @PersistenceContext(unitName = "btmPU")
    private EntityManager em;

    public ExpShiftPurposeService() {
        super(ExpShiftPurpose.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @PermitAll
    @Override
    public ExpShiftPurpose find(Object id) {
        return super.find(id);
    }

    @PermitAll
    public List<ExpShiftPurpose> findByHall(Hall hall, Boolean active) {
        String query = "select a from ExpShiftPurpose a where hall = :hall ";
        String order = "order by experiment desc, active desc, name asc";

        if (active != null) {
            query = query + "and active = " + (active ? "'1' " : "'0' ");
        }

        query = query + order;

        TypedQuery<ExpShiftPurpose> q = em.createQuery(
                query,
                ExpShiftPurpose.class);

        q.setParameter("hall", hall);

        return q.getResultList();
    }

    @PermitAll
    public List<ExpShiftPurpose> findActiveExperimentsByHall(Hall hall) {
        String query = "select a from ExpShiftPurpose a where hall = :hall and active = true and experiment = true ";
        String order = "order by name asc";

        query = query + order;

        TypedQuery<ExpShiftPurpose> q = em.createQuery(
                query,
                ExpShiftPurpose.class);

        q.setParameter("hall", hall);

        return q.getResultList();
    }

    @PermitAll
    public List<ExpShiftPurpose> findActiveNonExperimentsByHall(Hall hall) {
        String query = "select a from ExpShiftPurpose a where hall = :hall and active = true and experiment = false ";
        String order = "order by name asc";

        query = query + order;

        TypedQuery<ExpShiftPurpose> q = em.createQuery(
                query,
                ExpShiftPurpose.class);

        q.setParameter("hall", hall);

        return q.getResultList();
    }

    @PermitAll
    public boolean isDuplicate(Hall hall, String name) {
        TypedQuery<Long> q = em.createQuery(
                "select count(a.name) from ExpShiftPurpose a where a.hall = :hall and a.name = :name",
                Long.class);

        q.setParameter("hall", hall);
        q.setParameter("name", name);

        Long count = q.getSingleResult();

        return count > 0;
    }

    @RolesAllowed({"cc", "btm-admin", "schcom"})
    public void add(Hall hall, String name, String alias, String url, Boolean experiment, Boolean active) throws
            UserFriendlyException {
        ExpShiftPurpose purpose = new ExpShiftPurpose();

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
                "select count(a.hall) from ExpShift a where a.expShiftPurpose.expShiftPurposeId = :id",
                Long.class);

        q.setParameter("id", programId);

        Long count = q.getSingleResult();

        return count > 0;
    }

    @RolesAllowed({"cc", "btm-admin", "schcom"})
    public void remove(BigInteger programId) throws UserFriendlyException {
        ExpShiftPurpose purpose = find(programId);

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

        ExpShiftPurpose purpose = find(programId);

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
    public Map<Integer, ExpShiftPurpose> findPurposeByIdMap() {
        Map<Integer, ExpShiftPurpose> purposeMap = new HashMap<>();

        List<ExpShiftPurpose> purposeList = findAll();

        for (ExpShiftPurpose purpose : purposeList) {
            purposeMap.put(purpose.getExpShiftPurposeId().intValue(), purpose);
        }

        return purposeMap;
    }

    @PermitAll
    public Map<String, ExpShiftPurpose> findPurposeByHallNameMap(Hall hall) {
        Map<String, ExpShiftPurpose> purposeMap = new HashMap<>();

        List<ExpShiftPurpose> purposeList = this.findByHall(hall, null);

        for (ExpShiftPurpose purpose : purposeList) {
            purposeMap.put(purpose.getName(), purpose);
        }

        return purposeMap;
    }
}
