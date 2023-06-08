package org.jlab.btm.business.service;

import org.jlab.btm.persistence.entity.ExpHour;
import org.jlab.btm.persistence.entity.ExpHourReasonTime;
import org.jlab.smoothness.persistence.enumeration.Hall;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Responsible for experimenter hall hour reason not ready time business
 * operations.
 *
 * @author ryans
 */
@Stateless
public class ExpHourReasonTimeService extends AbstractService<ExpHourReasonTime> {

    @PersistenceContext(unitName = "btmPU")
    private EntityManager em;

    public ExpHourReasonTimeService() {
        super(ExpHourReasonTime.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /**
     * Fetches a list of experimenter hall hour reason not ready times for the
     * specified hall, start day and hour, and end day and hour.
     *
     * @param hall the hall.
     * @param startDayAndHour the start day and hour.
     * @param endDayAndHour the end day and hour.
     * @return the list of hour reason times.
     */
    @PermitAll
    public List<ExpHourReasonTime> find(Hall hall, Date startDayAndHour,
                                        Date endDayAndHour) {
        TypedQuery<ExpHourReasonTime> q = em.createNamedQuery(
                "ExpHourReasonTime.findByHallAndHourRange", ExpHourReasonTime.class);

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        start.setTime(startDayAndHour);
        end.setTime(endDayAndHour);

        q.setParameter("hall", hall);
        q.setParameter("startDayAndHourCal", start);
        q.setParameter("endDayAndHourCal", end);

        return q.getResultList();
    }

    @PermitAll
    public List<String> validateUED(List<ExpHour> availabilityList, List<ExpHourReasonTime> explanationList) {
        List<String> discrepancies = new ArrayList<>();

        if(availabilityList != null) {
            for(ExpHour hour: availabilityList) {
                int uedSeconds = hour.getUedSeconds();
                int explanationSeconds = calculateExplanationSeconds(hour.getExpHourId(), explanationList);

                if(uedSeconds != explanationSeconds) {
                    discrepancies.add(explanationSeconds + " seconds of UED explanation for Hour " + hour.getDayAndHour() + " and expected " + uedSeconds);
                }
            }
        }

        return discrepancies;
    }

    private int calculateExplanationSeconds(BigInteger hourId, List<ExpHourReasonTime> explanationList) {
        int total = 0;

        if(explanationList != null) {
            for(ExpHourReasonTime explanation: explanationList) {
                if(hourId.equals(explanation.getExpHallHour().getExpHourId())) {
                    total = total + explanation.getSeconds();
                }
            }
        }

        return total;
    }
}
