package org.jlab.btm.business.service;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jlab.btm.persistence.entity.ScheduleDay;
import org.jlab.btm.persistence.projection.ProjectedScheduleDay;
import org.jlab.smoothness.persistence.util.JPAUtil;

/**
 * @author ryans
 */
@Stateless
public class ScheduleDayService extends AbstractService<ScheduleDay> {

  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  public ScheduleDayService() {
    super(ScheduleDay.class);
  }

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  @PermitAll
  public List<ScheduleDay> find(BigInteger monthlyScheduleId, Date start, Date end) {
    String sql =
        "select z.day_month_year, x.acc_program, x.kilo_volts_per_pass, x.min_hall_count, x.hall_a_note, x.hall_b_note, x.hall_c_note, x.hall_d_note, x.note, "
            + "x.hall_a_program_id, x.hall_a_nano_amps, x.hall_a_kilo_volts, x.hall_a_passes, x.hall_a_priority, x.hall_a_polarized, "
            + "x.hall_b_program_id, x.hall_b_nano_amps, x.hall_b_kilo_volts, x.hall_b_passes, x.hall_b_priority, x.hall_b_polarized, "
            + "x.hall_c_program_id, x.hall_c_nano_amps, x.hall_c_kilo_volts, x.hall_c_passes, x.hall_c_priority, x.hall_c_polarized, "
            + "x.hall_d_program_id, x.hall_d_nano_amps, x.hall_d_kilo_volts, x.hall_d_passes, x.hall_d_priority, x.hall_d_polarized "
            + "from (select to_date(:start) - 1 + rownum as day_month_year from dual connect by rownum < (to_date(:end) - to_date(:start) + 2)) z "
            + "left outer join schedule_day x on z.day_month_year = x.day_month_year and x.monthly_schedule_id = :schedule_id order by z.day_month_year asc";

    Query q = em.createNativeQuery(sql);

    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

    q.setParameter("start", dateFormat.format(start));
    q.setParameter("end", dateFormat.format(end));
    q.setParameter("schedule_id", monthlyScheduleId);

    /**
     * JPA won't create object for me via getResultList due to day_month_year coming from dual table
     * so I use JPAUtil reflection, but it only works on objects with a single special constructor
     * and the entity must have a default no-arg constructor so we cannot use ScheduleDay directly
     * and instead use a ProjectedScheduleDay.
     */
    // List<ScheduleDay> scheduleList = q.getResultList();
    List<ProjectedScheduleDay> scheduleList = JPAUtil.getResultList(q, ProjectedScheduleDay.class);

    List<ScheduleDay> resultList = new ArrayList<>();
    for (ScheduleDay d : scheduleList) {
      resultList.add(d);
    }
    return resultList;
  }

  @PermitAll
  public ScheduleDay findByDateAndSchedule(Date dayMonthYear, BigInteger monthlyScheduleId) {
    TypedQuery<ScheduleDay> q =
        em.createQuery(
            "select s from ScheduleDay s where s.dayMonthYear = :day and s.monthlySchedule.monthlyScheduleId = :schedule_id",
            ScheduleDay.class);

    q.setParameter("day", dayMonthYear);
    q.setParameter("schedule_id", monthlyScheduleId);

    List<ScheduleDay> dayList = q.getResultList();

    ScheduleDay day = null;

    if (dayList != null && !dayList.isEmpty()) {
      day = dayList.get(0);
    }

    return day;
  }
}
