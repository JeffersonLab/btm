package org.jlab.btm.business.service;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import org.jlab.btm.persistence.entity.CcProgramInterval;
import org.jlab.btm.persistence.enumeration.TimelineProgram;
import org.jlab.smoothness.business.exception.UserFriendlyException;

/**
 * The State Timeline: the accelerator program over time, as intervals.
 *
 * <p>Each interval runs from its start until the next interval's start, and the latest one is the
 * current program. Neighboring intervals always have different programs: a change that would leave
 * two alike merges them.
 */
@Stateless
public class CcProgramIntervalService extends AbstractService<CcProgramInterval> {

  private static final DateTimeFormatter FRIENDLY =
      DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss").withZone(ZoneId.of("America/New_York"));

  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  public CcProgramIntervalService() {
    super(CcProgramInterval.class);
  }

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  /**
   * @return The latest interval, which is the current program; null if the timeline is empty
   */
  @PermitAll
  public CcProgramInterval findCurrent() {
    return first("select i from CcProgramInterval i order by i.startTime desc", null);
  }

  /**
   * @return The number of intervals
   */
  @PermitAll
  public long count() {
    return em.createQuery("select count(i) from CcProgramInterval i", Long.class).getSingleResult();
  }

  /**
   * @param offset How many of the latest intervals to skip
   * @param max The most intervals to return
   * @return Intervals from the latest back, each with its end
   */
  @PermitAll
  public List<CcProgramInterval> findPage(int offset, int max) {
    // One more, later, interval than asked for: its start is the end of the page's first
    int extra = offset > 0 ? 1 : 0;

    List<CcProgramInterval> intervals =
        em.createQuery(
                "select i from CcProgramInterval i order by i.startTime desc",
                CcProgramInterval.class)
            .setFirstResult(offset - extra)
            .setMaxResults(max + extra)
            .getResultList();

    for (int i = 1; i < intervals.size(); i++) {
      intervals.get(i).setEndTime(intervals.get(i - 1).getStartTime());
    }

    return extra == 0 || intervals.isEmpty() ? intervals : intervals.subList(1, intervals.size());
  }

  /**
   * @param start The start of a time range
   * @param end The end of the range, exclusive
   * @return The intervals that overlap the range, earliest first, each with its end; the last one's
   *     end is null, since it runs to the range's end or beyond
   */
  @PermitAll
  public List<CcProgramInterval> findOverlapping(Instant start, Instant end) {
    TypedQuery<CcProgramInterval> query =
        em.createQuery(
            "select i from CcProgramInterval i where i.startTime < :end and i.startTime >= "
                + "coalesce((select max(j.startTime) from CcProgramInterval j "
                + "where j.startTime <= :start), :start) order by i.startTime asc",
            CcProgramInterval.class);

    query.setParameter("start", start);
    query.setParameter("end", end);

    List<CcProgramInterval> intervals = query.getResultList();

    for (int i = 0; i < intervals.size() - 1; i++) {
      intervals.get(i).setEndTime(intervals.get(i + 1).getStartTime());
    }

    return intervals;
  }

  /**
   * Adds a program change: from the start on, until the next interval, the program is the given
   * one. The interval the start falls in ends there.
   *
   * @param program The program
   * @param start When the program started; seconds precision
   * @throws UserFriendlyException If the change isn't valid
   */
  @RolesAllowed({"cc", "btm-admin"})
  public void add(TimelineProgram program, Instant start) throws UserFriendlyException {
    start = validate(program, start);

    if (findByStart(start) != null) {
      throw new UserFriendlyException("An interval already starts at " + FRIENDLY.format(start));
    }

    CcProgramInterval previous = previous(start);

    if (previous != null && previous.getProgram() == program) {
      throw new UserFriendlyException(
          "The program is already " + program.getLabel() + " at " + FRIENDLY.format(start));
    }

    CcProgramInterval next = next(start);

    CcProgramInterval interval = new CcProgramInterval();
    interval.setProgram(program);
    interval.setStartTime(start);
    stamp(interval);

    create(interval);

    if (next != null && next.getProgram() == program) {
      remove(next); // The new interval now covers it
    }
  }

  /**
   * Changes an interval's program, its start, or both. The start must stay between the starts of
   * the intervals before and after it; the interval before it ends at the new start.
   *
   * @param id The interval's ID
   * @param program The program
   * @param start When the program started; seconds precision
   * @throws UserFriendlyException If the change isn't valid
   */
  @RolesAllowed({"cc", "btm-admin"})
  public void edit(BigInteger id, TimelineProgram program, Instant start)
      throws UserFriendlyException {
    CcProgramInterval interval = findOrThrow(id);

    CcProgramInterval previous = previous(interval.getStartTime());
    CcProgramInterval next = next(interval.getStartTime());

    start = validate(program, start);

    if (previous != null && !start.isAfter(previous.getStartTime())) {
      throw new UserFriendlyException(
          "Start must be after the start of the interval before it, "
              + FRIENDLY.format(previous.getStartTime()));
    }

    if (next != null && !start.isBefore(next.getStartTime())) {
      throw new UserFriendlyException(
          "Start must be before the start of the interval after it, "
              + FRIENDLY.format(next.getStartTime()));
    }

    interval.setProgram(program);
    interval.setStartTime(start);
    stamp(interval);

    if (previous != null && previous.getProgram() == program) {
      remove(interval); // The interval before it now covers it
      interval = previous;
    }

    if (next != null && next.getProgram() == interval.getProgram()) {
      remove(next);
    }
  }

  /**
   * Removes an interval; the interval before it then runs until the one after it.
   *
   * @param id The interval's ID
   * @throws UserFriendlyException If there is no such interval
   */
  @RolesAllowed({"cc", "btm-admin"})
  public void remove(BigInteger id) throws UserFriendlyException {
    CcProgramInterval interval = findOrThrow(id);

    CcProgramInterval previous = previous(interval.getStartTime());
    CcProgramInterval next = next(interval.getStartTime());

    remove(interval);

    if (previous != null && next != null && previous.getProgram() == next.getProgram()) {
      remove(next);
    }
  }

  /**
   * Records the program measured through EPICS, for the monitor: appends an interval unless the
   * timeline already ends with that program.
   *
   * <p>The measurement is skipped if it is no later than the latest interval's start: someone has
   * entered a later change by hand, and theirs stands.
   *
   * @param program The measured program
   * @param at When it was measured
   * @return Whether an interval was added
   */
  @PermitAll
  public boolean recordMeasured(TimelineProgram program, Instant at) {
    at = at.truncatedTo(ChronoUnit.SECONDS);

    CcProgramInterval current = findCurrent();

    if (current != null
        && (current.getProgram() == program || !at.isAfter(current.getStartTime()))) {
      return false;
    }

    CcProgramInterval interval = new CcProgramInterval();
    interval.setProgram(program);
    interval.setStartTime(at);

    create(interval);

    return true;
  }

  private Instant validate(TimelineProgram program, Instant start) throws UserFriendlyException {
    if (program == null) {
      throw new UserFriendlyException("Program is required");
    }

    if (start == null) {
      throw new UserFriendlyException("Start is required");
    }

    start = start.truncatedTo(ChronoUnit.SECONDS);

    if (start.isAfter(Instant.now())) {
      throw new UserFriendlyException("Start can't be in the future");
    }

    return start;
  }

  private void stamp(CcProgramInterval interval) {
    interval.setModifiedBy(context.getCallerPrincipal().getName());
    interval.setModifiedDate(new Date());
  }

  private CcProgramInterval findOrThrow(BigInteger id) throws UserFriendlyException {
    CcProgramInterval interval = id == null ? null : find(id);

    if (interval == null) {
      throw new UserFriendlyException("Interval not found; it may have been removed or merged");
    }

    return interval;
  }

  private CcProgramInterval findByStart(Instant start) {
    return first("select i from CcProgramInterval i where i.startTime = :t", start);
  }

  private CcProgramInterval previous(Instant start) {
    return first(
        "select i from CcProgramInterval i where i.startTime < :t order by i.startTime desc",
        start);
  }

  private CcProgramInterval next(Instant start) {
    return first(
        "select i from CcProgramInterval i where i.startTime > :t order by i.startTime asc", start);
  }

  private CcProgramInterval first(String jpql, Instant t) {
    TypedQuery<CcProgramInterval> query = em.createQuery(jpql, CcProgramInterval.class);

    if (t != null) {
      query.setParameter("t", t);
    }

    List<CcProgramInterval> intervals = query.setMaxResults(1).getResultList();

    return intervals.isEmpty() ? null : intervals.get(0);
  }
}
