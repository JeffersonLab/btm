package org.jlab.btm.business.service;

import org.jlab.btm.persistence.entity.Staff;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ryans
 */
@Stateless
public class StaffService extends AbstractService<Staff> {
    @PersistenceContext(unitName = "btmPU")
    private EntityManager em;

    public StaffService() {
        super(Staff.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @PermitAll
    public Staff findByUsername(String username) {
        TypedQuery<Staff> q = em.createQuery("select s from Staff s where username = :username", Staff.class);

        q.setParameter("username", username);

        Staff staff = null;

        List<Staff> resultList = q.getResultList();

        if (resultList != null && !resultList.isEmpty()) {
            staff = resultList.get(0);
        }

        return staff;
    }

    @PermitAll
    public List<Staff> filterList(String lastname, int offset, int max) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Staff> cq = cb.createQuery(Staff.class);
        Root<Staff> root = cq.from(Staff.class);
        cq.select(root);

        List<Predicate> filters = new ArrayList<>();

        if (lastname != null && !lastname.isEmpty()) {
            filters.add(cb.like(cb.lower(root.get("lastname")), lastname.toLowerCase()));
        }

        if (!filters.isEmpty()) {
            cq.where(cb.and(filters.toArray(new Predicate[]{})));
        }

        List<Order> orders = new ArrayList<>();
        Path p0 = root.get("lastname");
        Order o0 = cb.asc(p0);
        orders.add(o0);
        cq.orderBy(orders);
        return getEntityManager().createQuery(cq).setFirstResult(offset).setMaxResults(max).getResultList();
    }

    @PermitAll
    public long countList(String lastname, int offset, int max) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Staff> root = cq.from(Staff.class);

        List<Predicate> filters = new ArrayList<>();

        if (lastname != null && !lastname.isEmpty()) {
            filters.add(cb.like(cb.lower(root.get("lastname")), lastname.toLowerCase()));
        }

        if (!filters.isEmpty()) {
            cq.where(cb.and(filters.toArray(new Predicate[]{})));
        }

        cq.select(cb.count(root));
        TypedQuery<Long> q = getEntityManager().createQuery(cq);
        return q.getSingleResult();
    }
}
