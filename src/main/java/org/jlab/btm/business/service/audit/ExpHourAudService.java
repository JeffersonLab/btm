package org.jlab.btm.business.service.audit;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import org.jlab.btm.business.service.AbstractService;
import org.jlab.btm.persistence.entity.audit.ExpHourAud;

/**
 * @author ryans
 */
@Stateless
public class ExpHourAudService extends AbstractService<ExpHourAud> {
  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public ExpHourAudService() {
    super(ExpHourAud.class);
  }

  @PermitAll
  public List<ExpHourAud> filterList(
      BigInteger entityId, BigInteger revisionId, int offset, int max) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<ExpHourAud> cq = cb.createQuery(ExpHourAud.class);
    Root<ExpHourAud> root = cq.from(ExpHourAud.class);
    cq.select(root);

    List<Predicate> filters = new ArrayList<Predicate>();

    if (entityId != null) {
      filters.add(cb.equal(root.get("expHourAudPK").get("expHourId"), entityId));
    }

    if (revisionId != null) {
      filters.add(cb.equal(root.get("revision").get("id"), revisionId));
    }

    if (!filters.isEmpty()) {
      cq.where(cb.and(filters.toArray(new Predicate[] {})));
    }
    List<Order> orders = new ArrayList<Order>();
    Path p0 = root.get("revision").get("id");
    Order o0 = cb.asc(p0);
    orders.add(o0);
    cq.orderBy(orders);

    List<ExpHourAud> entityList =
        getEntityManager()
            .createQuery(cq)
            .setFirstResult(offset)
            .setMaxResults(max)
            .getResultList();

    if (entityList != null) {
      for (ExpHourAud entity : entityList) {
        entity.getRevision().getId(); // Tickle to load
      }
    }

    return entityList;
  }

  @PermitAll
  public Long countFilterList(BigInteger entityId, BigInteger revisionId) {
    String selectFrom = "select count(*) from EXP_HOUR_AUD e ";

    List<String> whereList = new ArrayList<>();

    String w;

    if (entityId != null) {
      w = "e.exp_hour_id = " + entityId;
      whereList.add(w);
    }

    if (revisionId != null) {
      w = "e.rev = " + revisionId;
      whereList.add(w);
    }

    String where = "";

    if (!whereList.isEmpty()) {
      where = "where ";
      for (String wh : whereList) {
        where = where + wh + " and ";
      }

      where = where.substring(0, where.length() - 5);
    }

    String sql = selectFrom + " " + where;
    Query q = em.createNativeQuery(sql);

    return ((Number) q.getSingleResult()).longValue();
  }
}
