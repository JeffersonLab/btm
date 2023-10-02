package org.jlab.btm.business.service;

import org.hibernate.envers.RevisionType;
import org.jlab.btm.business.params.ActivityAuditParams;
import org.jlab.btm.business.util.BtmTimeUtil;
import org.jlab.btm.persistence.entity.*;
import org.jlab.btm.persistence.entity.audit.CcAccHourAud;
import org.jlab.btm.persistence.entity.audit.CcShiftAud;
import org.jlab.btm.persistence.entity.audit.ExpHourAud;
import org.jlab.btm.persistence.entity.audit.ExpShiftAud;
import org.jlab.btm.persistence.enumeration.TimesheetType;
import org.jlab.btm.persistence.projection.AuditedEntityChange;
import org.jlab.smoothness.business.util.TimeUtil;
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

    private void addCommonFilters(CriteriaBuilder cb, CriteriaQuery<? extends Object> cq, Root<RevisionInfo> root, ActivityAuditParams params, List<Predicate> filters) {
        if (params.getModifiedStart() != null) {
            filters.add(cb.greaterThanOrEqualTo(root.get("ts"), params.getModifiedStart().getTime()));
        }

        if (params.getModifiedEnd() != null) {
            filters.add(cb.lessThan(root.get("ts"), params.getModifiedEnd().getTime()));
        }

        if (params.getType() != null) {
            Date startHour = null;
            Date endHour = null;

            if(params.getType() == TimesheetType.CC) {
                if(params.getShift() != null && params.getTimesheetDate() != null) {
                    startHour = TimeUtil.getCrewChiefStartDayAndHour(params.getTimesheetDate(), params.getShift());
                    endHour = TimeUtil.getCrewChiefEndDayAndHour(params.getTimesheetDate(), params.getShift());
                }

                Subquery<Integer> shiftSubquery = cq.subquery(Integer.class);
                Root<CcShiftAud> shiftRoot = shiftSubquery.from(CcShiftAud.class);
                shiftSubquery.select(shiftRoot.get("revision"));
                if(startHour != null) {
                    shiftSubquery.where(cb.equal(shiftRoot.get("startDayAndHour"), startHour));
                }
                Predicate shiftPredicate = cb.in(root.get("id")).value(shiftSubquery);

                Subquery<Integer> accHourSubquery = cq.subquery(Integer.class);
                Root<CcAccHourAud> accHourRoot = accHourSubquery.from(CcAccHourAud.class);
                accHourSubquery.select(accHourRoot.get("revision"));
                if(startHour != null) {
                    accHourSubquery.where(cb.and(cb.greaterThanOrEqualTo(accHourRoot.get("dayAndHour"), startHour)), cb.lessThanOrEqualTo(accHourRoot.get("dayAndHour"), endHour));
                }
                Predicate accHourPredicate = cb.in(root.get("id")).value(accHourSubquery);

                /*Subquery<Integer> signatureSubquery = cq.subquery(Integer.class);
                Root<CcSignatureAud> signatureRoot = signatureSubquery.from(CcSignatureAud.class);
                signatureSubquery.select(signatureRoot.get("revision"));
                Predicate signaturePredicate = cb.in(root.get("id")).value(signatureSubquery);*/

                filters.add(cb.or(shiftPredicate, accHourPredicate));
            } else {
                if(params.getShift() != null && params.getTimesheetDate() != null) {
                    startHour = BtmTimeUtil.getExperimenterStartDayAndHour(params.getTimesheetDate(), params.getShift());
                    endHour = BtmTimeUtil.getExperimenterEndDayAndHour(params.getTimesheetDate(), params.getShift());
                }

                Hall hall;

                switch(params.getType()) {
                    case EA:
                        hall = Hall.A;
                        break;
                    case EB:
                        hall = Hall.B;
                        break;
                    case EC:
                        hall = Hall.C;
                        break;
                    case ED:
                        hall = Hall.D;
                        break;
                    default:
                        throw new RuntimeException("Unknown hall: " + params.getType());
                }

                Subquery<Integer> shiftSubquery = cq.subquery(Integer.class);
                Root<ExpShiftAud> shiftRoot = shiftSubquery.from(ExpShiftAud.class);
                shiftSubquery.select(shiftRoot.get("revision"));
                Predicate shiftWherePredicate = cb.equal(shiftRoot.get("hall"), hall);
                if(startHour != null) {
                    shiftWherePredicate = cb.and(shiftWherePredicate, cb.equal(shiftRoot.get("startDayAndHour"), startHour));
                }
                shiftSubquery.where(shiftWherePredicate);
                Predicate shiftPredicate = cb.in(root.get("id")).value(shiftSubquery);

                Subquery<Integer> hourSubquery = cq.subquery(Integer.class);
                Root<ExpHourAud> hourRoot = hourSubquery.from(ExpHourAud.class);
                hourSubquery.select(hourRoot.get("revision"));
                Predicate hourWherePredicate = cb.equal(hourRoot.get("hall"), hall);
                if(startHour != null) {
                    hourWherePredicate = cb.and(hourWherePredicate, cb.and(cb.greaterThanOrEqualTo(hourRoot.get("dayAndHour"), startHour)), cb.lessThanOrEqualTo(hourRoot.get("dayAndHour"), endHour));
                }
                hourSubquery.where(hourWherePredicate);
                Predicate hourPredicate = cb.in(root.get("id")).value(hourSubquery);

                filters.add(cb.or(shiftPredicate, hourPredicate));
            }
        }
    }

    @PermitAll
    public List<RevisionInfo> filterList(ActivityAuditParams params) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<RevisionInfo> cq = cb.createQuery(RevisionInfo.class);
        Root<RevisionInfo> root = cq.from(RevisionInfo.class);
        cq.select(root);

        List<Predicate> filters = new ArrayList<>();

        addCommonFilters(cb, cq, root, params, filters);

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

        addCommonFilters(cb, cq, root, params, filters);

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
