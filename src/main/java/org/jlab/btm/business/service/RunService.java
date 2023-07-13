package org.jlab.btm.business.service;

import org.jlab.btm.business.util.BtmTimeUtil;
import org.jlab.btm.business.util.DateRange;
import org.jlab.btm.persistence.entity.MonthlySchedule;
import org.jlab.btm.persistence.entity.Run;
import org.jlab.btm.persistence.entity.ScheduleDay;
import org.jlab.btm.persistence.projection.PacAccSum;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.DateIterator;
import org.jlab.smoothness.business.util.TimeUtil;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.math.BigInteger;
import java.time.Duration;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ryans
 */
@Stateless
public class RunService extends AbstractService<Run> {

    private final static Logger LOGGER
            = Logger.getLogger(RunService.class.getName());

    @PersistenceContext(unitName = "btmPU")
    private EntityManager em;

    public RunService() {
        super(Run.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @PermitAll
    public List<Run> findAll() {
        TypedQuery<Run> q = em.createQuery(
                "select r from Run r order by r.startDay desc",
                Run.class);

        return q.getResultList();
    }


    @PermitAll
    public Run getCurrentRun() {
        Date today = new Date();
        Run run = null;

        TypedQuery<Run> q = em.createQuery(
                "select r from Run r where r.startDay <= :today and r.endDay > :today order by r.startDay desc",
                Run.class);

        q.setParameter("today", today);

        List<Run> runs = q.getResultList();

        if(runs != null && !runs.isEmpty()) {
            run = runs.get(0);
        }
        return run;
    }

    @PermitAll
    public Run getRunBefore(Run run) {
        Run previous = null;

        Date currentRunStart = new Date();

        if(run != null) {
            currentRunStart = run.getStartDay();
        }

        TypedQuery<Run> q = em.createQuery(
                "select r from Run r where r.startDay < :runStart order by r.startDay desc",
                Run.class);;

        q.setParameter("runStart", currentRunStart);

        List<Run> runs = q.getResultList();

        if(runs != null && !runs.isEmpty()) {
            previous = runs.get(0);
        }
        return previous;
    }
}
