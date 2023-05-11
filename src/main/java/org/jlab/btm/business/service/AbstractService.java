package org.jlab.btm.business.service;

import javax.annotation.Resource;
import javax.ejb.EJBAccessException;
import javax.ejb.SessionContext;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @param <T>
 * @author ryans
 */
public abstract class AbstractService<T> {
    @Resource
    protected SessionContext context;

    private Class<T> entityClass;

    public AbstractService(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected abstract EntityManager getEntityManager();

    protected void create(T entity) {
        getEntityManager().persist(entity);
    }

    protected void edit(T entity) {
        getEntityManager().merge(entity);
    }

    protected void remove(T entity) {
        getEntityManager().remove(getEntityManager().merge(entity));
    }

    protected T find(Object id) {
        return getEntityManager().find(entityClass, id);
    }

    protected List<T> findAll() {
        CriteriaQuery<T> cq = getEntityManager().getCriteriaBuilder().createQuery(entityClass);
        cq.select(cq.from(entityClass));
        return getEntityManager().createQuery(cq).getResultList();
    }

    protected List<T> findAll(OrderDirective... directives) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(entityClass);
        Root<T> root = cq.from(entityClass);
        cq.select(root);
        List<Order> orders = new ArrayList<>();
        for (OrderDirective ob : directives) {
            Order o;

            Path p = root.get(ob.field);

            if (ob.asc) {
                o = cb.asc(p);
            } else {
                o = cb.desc(p);
            }

            orders.add(o);
        }
        cq.orderBy(orders);
        return getEntityManager().createQuery(cq).getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    protected String checkAuthenticated() {
        String username = context.getCallerPrincipal().getName();
        if (username == null || username.isEmpty() || username.equalsIgnoreCase("ANONYMOUS")) {
            throw new EJBAccessException("You must be authenticated to perform the requested operation");
        } else {
            username = username.split(":")[2];
        }
        return username;
    }

    public static class OrderDirective {

        private String field;
        private boolean asc;

        public OrderDirective(String field) {
            this(field, true);
        }

        public OrderDirective(String field, boolean asc) {
            this.field = field;
            this.asc = asc;
        }

        public String getField() {
            return field;
        }

        public boolean isAsc() {
            return asc;
        }
    }
}
