package org.jlab.btm.business.service;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.btm.business.util.RunCalculator;
import org.jlab.btm.business.util.RunCalculator.Bounds;
import org.jlab.btm.persistence.entity.Run;
import org.jlab.smoothness.business.exception.UserFriendlyException;

/**
 * The runs: stretches of days the accelerator is scheduled to be on, kept in the RUN table so they
 * needn't be worked out from the schedules on each request.
 *
 * <p>Runs come from each month's latest published schedule (see {@link RunCalculator}) and are
 * recalculated whenever a schedule is published and when the app starts. An admin can set a run's
 * days by hand where the actual run differs from the schedule; such a manual run is kept as it is,
 * and replaces any run from the schedules that shares a day with it.
 *
 * @author ryans
 */
@Stateless
public class RunService extends AbstractService<Run> {

  private static final Logger LOGGER = Logger.getLogger(RunService.class.getName());

  private static final DateTimeFormatter FRIENDLY =
      DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.US);

  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  public RunService() {
    super(Run.class);
  }

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  /**
   * @return All runs, latest first
   */
  @PermitAll
  public List<Run> findAll() {
    TypedQuery<Run> q = em.createQuery("select r from Run r order by r.startDay desc", Run.class);

    return q.getResultList();
  }

  /**
   * @return All runs, latest first, each with the run the published schedules have in its place
   */
  @PermitAll
  public List<Run> findAllWithScheduled() {
    List<Run> runs = findAll();
    List<Bounds> scheduled = findScheduledRuns();

    for (Run run : runs) {
      Bounds bounds = bounds(run);

      for (Bounds s : scheduled) {
        if (s.overlaps(bounds)) {
          run.setScheduled(s);
          break;
        }
      }
    }

    return runs;
  }

  @PermitAll
  public Run getCurrentRun() {
    Date today = new Date();
    Run run = null;

    TypedQuery<Run> q =
        em.createQuery(
            "select r from Run r where r.startDay <= :today and r.endDay > :today order by r.startDay desc",
            Run.class);

    q.setParameter("today", today);

    List<Run> runs = q.getResultList();

    if (runs != null && !runs.isEmpty()) {
      run = runs.get(0);
    }
    return run;
  }

  @PermitAll
  public Run getRunBefore(Run run) {
    Run previous = null;

    Date currentRunStart = new Date();

    if (run != null) {
      currentRunStart = run.getStartDay();
    }

    TypedQuery<Run> q =
        em.createQuery(
            "select r from Run r where r.startDay < :runStart order by r.startDay desc", Run.class);

    q.setParameter("runStart", currentRunStart);

    List<Run> runs = q.getResultList();

    if (runs != null && !runs.isEmpty()) {
      previous = runs.get(0);
    }
    return previous;
  }

  /**
   * Works out the runs from each month's latest published schedule; a month without a published
   * schedule counts as OFF.
   *
   * @return The runs, earliest first
   */
  @PermitAll
  public List<Bounds> findScheduledRuns() {
    // Days of each month's latest published schedule: no other published schedule for the month
    // has a later publication, or the same publication and a higher version
    List<Object[]> rows =
        em.createQuery(
                "select d.dayMonthYear, d.accProgram, m.startDay from ScheduleDay d "
                    + "join d.monthlySchedule m where m.publishedDate is not null "
                    + "and not exists (select n from MonthlySchedule n "
                    + "where n.startDay = m.startDay and n.publishedDate is not null "
                    + "and (n.publishedDate > m.publishedDate "
                    + "or (n.publishedDate = m.publishedDate and n.version > m.version)))",
                Object[].class)
            .getResultList();

    Map<LocalDate, String> programByDay = new HashMap<>();

    for (Object[] row : rows) {
      LocalDate day = RunCalculator.toLocalDate((Date) row[0]);
      LocalDate month = RunCalculator.toLocalDate((Date) row[2]);

      // A schedule speaks only for its own month
      if (day.getYear() == month.getYear() && day.getMonth() == month.getMonth()) {
        programByDay.put(day, (String) row[1]);
      }
    }

    return RunCalculator.findRuns(programByDay);
  }

  /**
   * Brings the RUN table up to date with the published schedules. Runs set by hand are left as they
   * are, and no run from the schedules that shares a day with one is added. Runs from the schedules
   * that haven't changed keep their rows.
   *
   * <p>Anyone may call this: the result depends only on the published schedules and the runs set by
   * hand. It's called when a schedule is published and when the app starts.
   */
  @PermitAll
  public void recalculate() {
    List<Run> runs = findAll();

    List<Bounds> manual = new ArrayList<>();

    for (Run run : runs) {
      if (run.isManual()) {
        manual.add(bounds(run));
      }
    }

    Set<Bounds> wanted = new LinkedHashSet<>();

    for (Bounds scheduled : findScheduledRuns()) {
      if (manual.stream().noneMatch(scheduled::overlaps)) {
        wanted.add(scheduled);
      }
    }

    int removed = 0;

    for (Run run : runs) {
      if (!run.isManual() && !wanted.remove(bounds(run))) {
        em.remove(run);
        removed++;
      }
    }

    for (Bounds bounds : wanted) {
      Run run = new Run();
      setBounds(run, bounds);
      run.setManual(false);
      em.persist(run);
    }

    if (removed > 0 || !wanted.isEmpty()) {
      LOGGER.log(
          Level.INFO,
          "Runs recalculated from the published schedules: {0} added, {1} removed",
          new Object[] {wanted.size(), removed});
    }
  }

  /**
   * Adds a run by hand.
   *
   * @param firstDay The first day of the run
   * @param lastDay The last day of the run
   * @throws UserFriendlyException If the days aren't valid or another run shares one of them
   */
  @RolesAllowed("btm-admin")
  public void add(LocalDate firstDay, LocalDate lastDay) throws UserFriendlyException {
    Bounds bounds = validate(null, firstDay, lastDay);

    Run run = new Run();
    setBounds(run, bounds);
    run.setManual(true);
    em.persist(run);

    recalculate();
  }

  /**
   * Sets a run's days by hand. The run then stays as it is when the schedules change, until it's
   * removed.
   *
   * @param id The run's ID
   * @param firstDay The first day of the run
   * @param lastDay The last day of the run
   * @throws UserFriendlyException If there is no such run, the days aren't valid, or another run
   *     shares one of them
   */
  @RolesAllowed("btm-admin")
  public void edit(BigInteger id, LocalDate firstDay, LocalDate lastDay)
      throws UserFriendlyException {
    Run run = findOrThrow(id);
    Bounds bounds = validate(id, firstDay, lastDay);

    setBounds(run, bounds);
    run.setManual(true);

    recalculate();
  }

  /**
   * Removes a run set by hand; the published schedules' runs then apply to its days again.
   *
   * @param id The run's ID
   * @throws UserFriendlyException If there is no such run, or it comes from the schedules
   */
  @RolesAllowed("btm-admin")
  public void remove(BigInteger id) throws UserFriendlyException {
    Run run = findOrThrow(id);

    if (!run.isManual()) {
      throw new UserFriendlyException(
          "This run comes from the published schedules, which would add it again; "
              + "edit it to set its actual days instead");
    }

    em.remove(run);

    recalculate();
  }

  private Run findOrThrow(BigInteger id) throws UserFriendlyException {
    if (id == null) {
      throw new UserFriendlyException("Run ID must not be empty");
    }

    Run run = find(id);

    if (run == null) {
      throw new UserFriendlyException("No run has ID " + id);
    }

    return run;
  }

  private Bounds validate(BigInteger id, LocalDate firstDay, LocalDate lastDay)
      throws UserFriendlyException {
    if (firstDay == null) {
      throw new UserFriendlyException("First day must not be empty");
    }

    if (lastDay == null) {
      throw new UserFriendlyException("Last day must not be empty");
    }

    if (lastDay.isBefore(firstDay)) {
      throw new UserFriendlyException("Last day must not be before the first day");
    }

    Bounds bounds = new Bounds(firstDay, lastDay.plusDays(1));

    for (Run other : findAll()) {
      if (!other.getRunId().equals(id) && bounds(other).overlaps(bounds)) {
        throw new UserFriendlyException(
            "The run from "
                + FRIENDLY.format(RunCalculator.toLocalDate(other.getStartDay()))
                + " to "
                + FRIENDLY.format(RunCalculator.toLocalDate(other.getLastDay()))
                + " shares days with it; edit that run instead");
      }
    }

    return bounds;
  }

  private static Bounds bounds(Run run) {
    return new Bounds(
        RunCalculator.toLocalDate(run.getStartDay()), RunCalculator.toLocalDate(run.getEndDay()));
  }

  private static void setBounds(Run run, Bounds bounds) {
    run.setStartDay(RunCalculator.toDate(bounds.start()));
    run.setEndDay(RunCalculator.toDate(bounds.end()));
  }
}
