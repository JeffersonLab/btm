package org.jlab.btm.business.service;

import java.util.Date;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.jlab.btm.persistence.entity.ExpHallSignature;
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
}
