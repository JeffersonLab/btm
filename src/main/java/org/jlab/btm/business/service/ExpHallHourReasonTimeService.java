package org.jlab.btm.business.service;

import org.jlab.btm.persistence.entity.ExpHallHour;
import org.jlab.btm.persistence.entity.ExpHallHourReasonTime;
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
public class ExpHallHourReasonTimeService extends AbstractService<ExpHallHourReasonTime> {

    @PersistenceContext(unitName = "btmPU")
    private EntityManager em;

    public ExpHallHourReasonTimeService() {
        super(ExpHallHourReasonTime.class);
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
    public List<ExpHallHourReasonTime> find(Hall hall, Date startDayAndHour,
                                            Date endDayAndHour) {
        TypedQuery<ExpHallHourReasonTime> q = em.createNamedQuery(
                "ExpHallHourReasonTime.findByHallAndHourRange", ExpHallHourReasonTime.class);

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
    public List<String> validateUED(List<ExpHallHour> availabilityList, List<ExpHallHourReasonTime> explanationList) {
        List<String> discrepancies = new ArrayList<>();

        if(availabilityList != null) {
            for(ExpHallHour hour: availabilityList) {
                int uedSeconds = hour.getUedSeconds();
                int explanationSeconds = calculateExplanationSeconds(hour.getExpHallHourId(), explanationList);

                if(uedSeconds != explanationSeconds) {
                    discrepancies.add(explanationSeconds + " seconds of UED explanation for Hour " + hour.getDayAndHour() + " and expected " + uedSeconds);
                }
            }
        }

        return discrepancies;
    }

    private int calculateExplanationSeconds(BigInteger hourId, List<ExpHallHourReasonTime> explanationList) {
        int total = 0;

        if(explanationList != null) {
            for(ExpHallHourReasonTime explanation: explanationList) {
                if(hourId.equals(explanation.getExpHallHour().getExpHallHourId())) {
                    total = total + explanation.getSeconds();
                }
            }
        }

        return total;
    }
}
