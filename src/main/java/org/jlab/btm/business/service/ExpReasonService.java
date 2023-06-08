package org.jlab.btm.business.service;

import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.jlab.btm.persistence.entity.ExpReason;
import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * Responsible for experimenter hall reason not ready business operations.
 *
 * @author ryans
 */
@Stateless
public class ExpReasonService extends AbstractService<ExpReason> {

    @PersistenceContext(unitName = "btmPU")
    private EntityManager em;

    public ExpReasonService() {
        super(ExpReason.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @PermitAll
    public List<ExpReason> findByActive(Hall hall, boolean active) {
        TypedQuery<ExpReason> q = em.createNamedQuery("ExpReason.findByHallAndActive", ExpReason.class);

        q.setParameter("hall", hall);
        q.setParameter("active", active);

        return q.getResultList();
    }
}
