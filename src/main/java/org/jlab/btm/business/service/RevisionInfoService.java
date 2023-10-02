package org.jlab.btm.business.service;

import org.hibernate.envers.RevisionType;
import org.jlab.btm.business.params.ActivityAuditParams;
import org.jlab.btm.persistence.entity.*;
import org.jlab.btm.persistence.projection.AuditedEntityChange;
import org.jlab.smoothness.persistence.enumeration.Hall;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author ryans
 */
@Stateless
public class RevisionInfoService extends AbstractService<RevisionInfo> {

    @PersistenceContext(unitName = "btmPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public RevisionInfoService() {
        super(RevisionInfo.class);
    }

    @PermitAll
    public List<RevisionInfo> filterList(ActivityAuditParams params) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<RevisionInfo> cq = cb.createQuery(RevisionInfo.class);
        Root<RevisionInfo> root = cq.from(RevisionInfo.class);
        cq.select(root);

        List<Predicate> filters = new ArrayList<>();

        if (params.getModifiedStart() != null) {
            filters.add(cb.greaterThanOrEqualTo(root.get("ts"), params.getModifiedStart().getTime()));
        }

        if (params.getModifiedEnd() != null) {
            filters.add(cb.lessThan(root.get("ts"), params.getModifiedEnd().getTime()));
        }

        if (!filters.isEmpty()) {
            cq.where(cb.and(filters.toArray(new Predicate[]{})));
        }
        List<Order> orders = new ArrayList<>();
        Path p0 = root.get("id");
        Order o0 = cb.desc(p0);
        orders.add(o0);
        cq.orderBy(orders);

        List<RevisionInfo> revisionList = getEntityManager().createQuery(cq).setFirstResult(params.getOffset()).setMaxResults(params.getMaxPerPage()).getResultList();

        if (revisionList != null) {
            for (RevisionInfo revision : revisionList) {
                revision.setChangeList(findEntityChangeList(revision.getId()));
            }
        }

        return revisionList;
    }

    @PermitAll
    public Long countFilterList(ActivityAuditParams params) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<RevisionInfo> root = cq.from(RevisionInfo.class);

        List<Predicate> filters = new ArrayList<>();

        if (params.getModifiedStart() != null) {
            filters.add(cb.greaterThanOrEqualTo(root.get("ts"), params.getModifiedStart().getTime()));
        }

        if (params.getModifiedEnd() != null) {
            filters.add(cb.lessThan(root.get("ts"), params.getModifiedEnd().getTime()));
        }

        if (!filters.isEmpty()) {
            cq.where(cb.and(filters.toArray(new Predicate[]{})));
        }

        cq.select(cb.count(root));
        TypedQuery<Long> q = getEntityManager().createQuery(cq);
        return q.getSingleResult();
    }

    @SuppressWarnings("unchecked")
    @PermitAll
    public List<AuditedEntityChange> findEntityChangeList(long revision) {
        // We drop TZD on to_char because ExpShift doesn't have one, and we can live with ambiguous hour on Transactions page.
        Query q = em.createNativeQuery(
                "select 'ES', exp_shift_id, revtype, to_char(start_day_and_hour, 'YYYY-MM-DD HH24'), hall from btm_owner.exp_shift_aud where rev = :revision " +
                "union select 'CS', cc_shift_id, revtype, to_char(start_day_and_hour, 'YYYY-MM-DD HH24'), 'A' from btm_owner.cc_shift_aud where rev = :revision " +
                "union select 'EH', exp_hour_id, revtype, to_char(day_and_hour, 'YYYY-MM-DD HH24'), hall from btm_owner.exp_hour_aud where rev = :revision " +
                "union select 'CAH', cc_acc_hour_id, revtype, to_char(day_and_hour, 'YYYY-MM-DD HH24'), 'A' from btm_owner.cc_acc_hour_aud where rev = :revision "
        );

        q.setParameter("revision", revision);

        List<Object[]> resultList = q.getResultList();

        List<AuditedEntityChange> changeList = new ArrayList<>();

        if (resultList != null) {
            for (Object[] row : resultList) {
                Class entityClass = null;
                entityClass = fromString(((String) row[0]));
                BigInteger entityId = BigInteger.valueOf(((Number) row[1]).longValue());
                RevisionType type = fromNumber((Number) row[2]);

                String dateStr = (String)row[3];

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH");

                Date date = null;
                try {
                    date = format.parse(dateStr);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

                Hall hall = Hall.valueOf((String)row[4]);
                changeList.add(new AuditedEntityChange(revision, type, entityId, entityClass, date, hall));
            }
        }

        return changeList;
    }

    @PermitAll
    public Class fromString(String s) {
        Class entityClass = null;

        if (s != null) {
            if (s.equals("ES")) {
                entityClass = ExpShift.class;
            } else if(s.equals("EH")) {
                entityClass = ExpHour.class;
            } else if (s.equals("CS")) {
                entityClass = CcShift.class;
            } else if (s.equals("CAH")) {
                entityClass = CcAccHour.class;
            }
        }

        return entityClass;
    }

    @PermitAll
    public RevisionType fromNumber(Number n) {
        RevisionType type = null;

        if (n != null) {
            int intValue = (int) n.longValue();

            switch (intValue) {
                case 0:
                    type = RevisionType.ADD;
                    break;
                case 1:
                    type = RevisionType.MOD;
                    break;
                case 2:
                    type = RevisionType.DEL;
                    break;
            }
        }

        return type;
    }
}
