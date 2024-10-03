package org.jlab.btm.business.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.jlab.btm.business.util.BtmTimeUtil;
import org.jlab.btm.persistence.entity.ExpHour;
import org.jlab.btm.persistence.entity.ExpReason;
import org.jlab.btm.persistence.entity.ExpUedExplanation;
import org.jlab.btm.persistence.projection.HourReasonDiscrepancy;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * Responsible for experimenter UED explanation handling.
 *
 * @author ryans
 */
@Stateless
public class ExpUedExplanationService extends AbstractService<ExpUedExplanation> {

  @PersistenceContext(unitName = "btmPU")
  private EntityManager em;

  @EJB ExpHourService hourService;

  @EJB ExpSecurityRuleService ruleService;

  @EJB ExpReasonService reasonService;

  public ExpUedExplanationService() {
    super(ExpUedExplanation.class);
  }

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  /**
   * Fetches a list of experimenter hall hour reason not ready times for the specified hall, start
   * day and hour, and end day and hour.
   *
   * @param hall the hall.
   * @param startDayAndHour the start day and hour.
   * @param endDayAndHour the end day and hour.
   * @return the list of hour reason times.
   */
  @PermitAll
  public List<ExpUedExplanation> find(Hall hall, Date startDayAndHour, Date endDayAndHour) {
    TypedQuery<ExpUedExplanation> q =
        em.createNamedQuery("ExpHourReasonTime.findByHallAndHourRange", ExpUedExplanation.class);

    q.setParameter("hall", hall);
    q.setParameter("startDayAndHour", startDayAndHour);
    q.setParameter("endDayAndHour", endDayAndHour);

    return q.getResultList();
  }

  @PermitAll
  public List<HourReasonDiscrepancy> validateUED(
      List<ExpHour> availabilityList, List<ExpUedExplanation> explanationList) {
    List<HourReasonDiscrepancy> discrepancies = new ArrayList<>();

    if (availabilityList != null) {
      for (ExpHour hour : availabilityList) {
        int uedSeconds = hour.getUedSeconds();
        int explanationSeconds = calculateExplanationSeconds(hour.getExpHourId(), explanationList);

        if (uedSeconds != explanationSeconds) {
          HourReasonDiscrepancy discrepancy =
              new HourReasonDiscrepancy(hour.getDayAndHour(), uedSeconds, explanationSeconds);
          discrepancies.add(discrepancy);
        }
      }
    }

    return discrepancies;
  }

  private int calculateExplanationSeconds(
      BigInteger hourId, List<ExpUedExplanation> explanationList) {
    int total = 0;

    if (explanationList != null) {
      for (ExpUedExplanation explanation : explanationList) {
        if (hourId.equals(explanation.getExpHour().getExpHourId())) {
          total = total + explanation.getSeconds();
        }
      }
    }

    return total;
  }

  @PermitAll
  @Override
  public ExpUedExplanation find(Object id) {
    return super.find(id);
  }

  @PermitAll
  public void remove(Hall hall, Date startDayAndHour, BigInteger id) throws UserFriendlyException {
    ruleService.editCheck(hall, startDayAndHour);

    ExpUedExplanation explanation = this.find(id);

    if (explanation == null) {
      throw new UserFriendlyException("Explanation not found with ID: " + id);
    }

    super.remove(explanation);
  }

  @PermitAll
  public void add(Hall hall, Date dayAndHour, BigInteger reasonId, Short durationSeconds)
      throws UserFriendlyException {
    if (dayAndHour == null) {
      throw new UserFriendlyException("Hour must not be empty");
    }

    ruleService.editCheck(hall, BtmTimeUtil.getExpShiftStart(dayAndHour));

    if (durationSeconds == null || durationSeconds < 1) {
      throw new UserFriendlyException("Duration must not be empty and must be positive");
    }

    // This find method uses inclusive on both start and end!  Should have exclusive end.  Doh!
    List<ExpHour> hourList = hourService.findInDatabase(hall, dayAndHour, dayAndHour);

    ExpHour hour = null;

    if (hourList.size() == 1) {
      hour = hourList.get(0);
    } else {
      throw new UserFriendlyException("Unable to obtain hour " + dayAndHour);
    }

    if (reasonId == null) {
      throw new UserFriendlyException("Reason must not be empty");
    }

    ExpReason reason = reasonService.find(reasonId);

    if (reason == null) {
      throw new UserFriendlyException("Reason not found with ID: " + reasonId);
    }

    ExpUedExplanation explanation = new ExpUedExplanation();
    explanation.setHall(hall);
    explanation.setExpHour(hour);
    explanation.setSeconds(durationSeconds);
    explanation.setExpReason(reason);

    super.edit(explanation);
  }
}
