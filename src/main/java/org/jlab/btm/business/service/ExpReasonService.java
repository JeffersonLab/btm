package org.jlab.btm.business.service;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import org.jlab.btm.persistence.entity.ExpReason;
import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * Responsible for experimenter hall reason not ready business operations.
 *
 * @author ryans
 */
@Stateless
public class ExpReasonService extends AbstractService<ExpReason> {

  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  public ExpReasonService() {
    super(ExpReason.class);
  }

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  @PermitAll
  @Override
  public ExpReason find(Object id) {
    return super.find(id);
  }

  @PermitAll
  public List<ExpReason> findByActive(Hall hall, boolean active) {
    TypedQuery<ExpReason> q = em.createNamedQuery("ExpReason.findByHallAndActive", ExpReason.class);

    q.setParameter("hall", hall);
    q.setParameter("active", active);

    return q.getResultList();
  }
}
