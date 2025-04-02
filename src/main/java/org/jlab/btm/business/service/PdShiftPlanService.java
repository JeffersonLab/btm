package org.jlab.btm.business.service;

import java.util.Date;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.jlab.btm.persistence.entity.PdShiftPlan;
import org.jlab.btm.persistence.projection.PdAccSum;
import org.jlab.smoothness.business.util.TimeUtil;

/**
 * @author ryans
 */
@Stateless
public class PdShiftPlanService extends AbstractService<PdShiftPlan> {

  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  public PdShiftPlanService() {
    super(PdShiftPlan.class);
  }

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  @PermitAll
  public PdShiftPlan findInDatabase(Date startDayAndHour) {
    TypedQuery<PdShiftPlan> query =
        em.createQuery(
            "select a from PdShiftPlan a where a.startDayAndHour = :startDayAndHour",
            PdShiftPlan.class);

    query.setParameter("startDayAndHour", startDayAndHour);

    List<PdShiftPlan> planList = query.getResultList();
    PdShiftPlan plan = null;

    if (planList != null && !planList.isEmpty()) {
      plan = planList.get(0);
    }

    return plan;
  }

  /**
   * Returns an array of hall A - D seconds of scheduled time between the start and end dates. Note
   * that shifts cannot be split so if start and end don't fall on crew chief shift boundaries then
   * the data is inaccurate.
   *
   * <p>Another thing to watch out for is the the shift_plans table has a shift column that is
   * ignored as the start_day_and_hour includes all the information you need.
   *
   * @param start The starting shift start time (inclusive)
   * @param end The ending shift start time (exclusive)
   * @return
   */
  @PermitAll
  public Long[] findScheduledHallTime(Date start, Date end) {
    Long[] values = new Long[4];

    Query q =
        em.createNativeQuery(
            "select sum(hall_a_up_seconds), sum(hall_b_up_seconds), sum(hall_c_up_seconds), sum(hall_d_up_seconds) from pd_shift_plan where start_day_and_hour < :end and start_day_and_hour  >= :start");

    q.setParameter("start", start);
    q.setParameter("end", end);

    List resultList = q.getResultList();

    if (resultList != null && resultList.size() == 1) {
      Object[] row = (Object[]) resultList.get(0);
      for (int i = 0; i < 4; i++) {
        Object item = row[i];
        Number n = (Number) item;
        values[i] = n == null ? 0 : n.longValue();
      }
    }

    return values;
  }

  @PermitAll
  public PdAccSum findSummary(Date start, Date end) {
    Long[] values = new Long[5];

    // PD Shift Plans line up with CC Shifts
    start = TimeUtil.getCcShiftStart(start);
    end = TimeUtil.isCrewChiefShiftStart(end) ? end : TimeUtil.getCcShiftEnd(end);

    Query q =
        em.createNativeQuery(
            "select sum(physics_seconds), sum(sad_seconds), sum(studies_seconds), sum(restore_seconds), sum(acc_seconds) from pd_shift_plan where start_day_and_hour < :end and start_day_and_hour  >= :start");

    q.setParameter("start", start);
    q.setParameter("end", end);

    List resultList = q.getResultList();

    if (resultList != null && resultList.size() == 1) {
      Object[] row = (Object[]) resultList.get(0);
      for (int i = 0; i < 5; i++) {
        Object item = row[i];
        Number n = (Number) item;
        values[i] = n == null ? 0 : n.longValue();
      }
    }

    return new PdAccSum(start, end, values[0], values[1], values[2], values[3], values[4]);
  }
}
