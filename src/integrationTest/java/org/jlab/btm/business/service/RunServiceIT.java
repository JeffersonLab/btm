package org.jlab.btm.business.service;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityManager;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.jlab.btm.business.util.RunCalculator.Bounds;
import org.jlab.btm.persistence.entity.MonthlySchedule;
import org.jlab.btm.persistence.entity.Run;
import org.jlab.btm.persistence.entity.ScheduleDay;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

/**
 * Tests working out the runs from the published schedules and keeping the RUN table up to date with
 * them and with runs set by hand, against Oracle with the project's DDL. EJB security and JTA
 * require WildFly.
 */
@Testcontainers
class RunServiceIT {
  @Container
  private static final OracleContainer ORACLE =
      new OracleContainer("gvenzl/oracle-xe:21.3.0")
          .withUsername("BTM_OWNER")
          .withPassword("btm_test_password")
          .withEnv("TZ", "America/New_York")
          .withLabel("org.jlab.btm.test-owner", "run-integration-tests")
          .withCopyFileToContainer(
              MountableFile.forHostPath("container/oracle/initdb.d/02_ddl.sql"),
              "/container-entrypoint-initdb.d/02_ddl.sql");

  private static SessionFactory factory;
  private EntityManager em;
  private RunService runs;
  private MonthlyScheduleService schedules;

  @BeforeAll
  static void openFactory() {
    StandardServiceRegistry registry =
        new StandardServiceRegistryBuilder()
            .applySetting("hibernate.connection.url", ORACLE.getJdbcUrl())
            .applySetting("hibernate.connection.username", ORACLE.getUsername())
            .applySetting("hibernate.connection.password", ORACLE.getPassword())
            .applySetting("hibernate.hbm2ddl.auto", "none")
            .applySetting("jakarta.persistence.validation.mode", "none")
            .build();
    try {
      factory =
          new MetadataSources(registry)
              .addAnnotatedClass(MonthlySchedule.class)
              .addAnnotatedClass(ScheduleDay.class)
              .addAnnotatedClass(Run.class)
              .buildMetadata()
              .buildSessionFactory();
    } catch (RuntimeException | Error e) {
      StandardServiceRegistryBuilder.destroy(registry);
      throw e;
    }
  }

  @AfterAll
  static void closeFactory() {
    if (factory != null) {
      factory.close();
    }
  }

  @BeforeEach
  void beginTransaction() throws ReflectiveOperationException {
    em = factory.createEntityManager();
    em.getTransaction().begin();
    runs = new RunService();
    schedules = new MonthlyScheduleService();
    // Supply what WildFly normally injects, without changing app code.
    injectEntityManager(runs);
    injectEntityManager(schedules);
    schedules.runService = runs;
  }

  @AfterEach
  void rollback() {
    if (em != null) {
      try {
        if (em.getTransaction().isActive()) {
          em.getTransaction().rollback();
        }
      } finally {
        em.close();
      }
    }
  }

  @Test
  void usesEachMonthsLatestPublishedSchedule() {
    MonthlySchedule old = schedule("2026-01-01", 1, "2025-12-01");
    days(old, "2026-01-01", 31, "PHYSICS");
    MonthlySchedule latest = schedule("2026-01-01", 2, "2025-12-15");
    days(latest, "2026-01-01", 10, "OFF");
    days(latest, "2026-01-11", 21, "PHYSICS");
    MonthlySchedule tentative = schedule("2026-01-01", 3, null);
    days(tentative, "2026-01-01", 31, "OFF");

    // Published later than version 2 although its version is lower
    MonthlySchedule february = schedule("2026-02-01", 1, "2026-01-20");
    days(february, "2026-02-01", 15, "PHYSICS");
    days(february, "2026-02-16", 13, "OFF");
    MonthlySchedule earlierFebruary = schedule("2026-02-01", 2, "2026-01-10");
    days(earlierFebruary, "2026-02-01", 28, "OFF");

    // Only tentative: counts as OFF
    days(schedule("2026-03-01", 1, null), "2026-03-01", 31, "PHYSICS");
    flushAndClear();

    assertEquals(List.of(bounds("2026-01-11", "2026-02-16")), runs.findScheduledRuns());
  }

  @Test
  void ignoresDaysOutsideTheirSchedulesMonth() {
    MonthlySchedule january = schedule("2026-01-01", 1, "2025-12-01");
    days(january, "2026-01-30", 2, "PHYSICS");
    day(january, "2026-02-01", "PHYSICS");
    flushAndClear();

    assertEquals(List.of(bounds("2026-01-30", "2026-02-01")), runs.findScheduledRuns());
  }

  @Test
  void fillsAnEmptyTableAndKeepsRowsOfUnchangedRuns() {
    MonthlySchedule january = schedule("2026-01-01", 1, "2025-12-01");
    days(january, "2026-01-01", 5, "PHYSICS");
    days(january, "2026-01-06", 10, "OFF");
    days(january, "2026-01-16", 16, "PHYSICS");
    flushAndClear();

    runs.recalculate();
    flushAndClear();

    List<Run> first = runs.findAll();
    assertEquals(
        List.of(bounds("2026-01-16", "2026-02-01"), bounds("2026-01-01", "2026-01-06")),
        boundsOf(first));

    // Recalculating again changes nothing
    runs.recalculate();
    flushAndClear();
    assertEquals(ids(first), ids(runs.findAll()));

    // A new version extends the second run into February and drops the first
    MonthlySchedule changed = schedule("2026-01-01", 2, "2025-12-20");
    days(changed, "2026-01-01", 15, "OFF");
    days(changed, "2026-01-16", 16, "PHYSICS");
    MonthlySchedule february = schedule("2026-02-01", 1, "2026-01-20");
    days(february, "2026-02-01", 3, "PHYSICS");
    flushAndClear();

    runs.recalculate();
    flushAndClear();

    List<Run> after = runs.findAll();
    assertEquals(List.of(bounds("2026-01-16", "2026-02-04")), boundsOf(after));
    assertFalse(after.get(0).isManual());
  }

  @Test
  void publishingAScheduleRecalculatesTheRuns() throws UserFriendlyException {
    MonthlySchedule january = schedule("2026-01-01", 1, null);
    days(january, "2026-01-10", 5, "RESTORE");
    flushAndClear();

    runs.recalculate();
    assertEquals(List.of(), runs.findAll());

    schedules.publish(january.getMonthlyScheduleId());
    flushAndClear();

    assertEquals(List.of(bounds("2026-01-10", "2026-01-15")), boundsOf(runs.findAll()));
  }

  /** The case in the issue: NPES version 2 had restore start Aug 28, but it started Aug 26. */
  @Test
  void aRunSetByHandStaysWhenTheSchedulesChange() throws UserFriendlyException {
    MonthlySchedule august = schedule("2026-08-01", 1, "2026-07-01");
    days(august, "2026-08-01", 27, "OFF");
    days(august, "2026-08-28", 4, "RESTORE");
    MonthlySchedule september = schedule("2026-09-01", 1, "2026-08-01");
    days(september, "2026-09-01", 20, "PHYSICS");
    flushAndClear();

    runs.recalculate();
    flushAndClear();
    Run scheduled = runs.findAll().get(0);

    runs.edit(scheduled.getRunId(), day("2026-08-26"), day("2026-09-20"));
    flushAndClear();

    // The schedule then extends the run; the run set by hand doesn't follow it
    MonthlySchedule later = schedule("2026-09-01", 2, "2026-09-10");
    days(later, "2026-09-01", 30, "PHYSICS");
    flushAndClear();
    runs.recalculate();
    flushAndClear();

    List<Run> all = runs.findAllWithScheduled();
    assertEquals(1, all.size());
    Run run = all.get(0);
    assertEquals(scheduled.getRunId(), run.getRunId());
    assertTrue(run.isManual());
    assertEquals(bounds("2026-08-26", "2026-09-21"), boundsOf(all).get(0));
    assertEquals(bounds("2026-08-28", "2026-10-01"), run.getScheduled());
    assertFalse(run.isAsScheduled());
    assertEquals(26, run.getDays());

    // Removing it brings back the run from the schedules
    runs.remove(run.getRunId());
    flushAndClear();

    all = runs.findAllWithScheduled();
    assertEquals(List.of(bounds("2026-08-28", "2026-10-01")), boundsOf(all));
    assertFalse(all.get(0).isManual());
    assertTrue(all.get(0).isAsScheduled());
  }

  @Test
  void aRunAddedByHandMustNotShareDaysWithAnother() throws UserFriendlyException {
    MonthlySchedule january = schedule("2026-01-01", 1, "2025-12-01");
    days(january, "2026-01-10", 10, "PHYSICS");
    flushAndClear();
    runs.recalculate();
    flushAndClear();

    UserFriendlyException overlap =
        assertThrows(
            UserFriendlyException.class, () -> runs.add(day("2026-01-05"), day("2026-01-10")));
    assertEquals(
        "The run from 10-Jan-2026 to 19-Jan-2026 shares days with it; edit that run instead",
        overlap.getMessage());

    runs.add(day("2026-02-01"), day("2026-02-01"));
    flushAndClear();

    List<Run> all = runs.findAllWithScheduled();
    assertEquals(
        List.of(bounds("2026-02-01", "2026-02-02"), bounds("2026-01-10", "2026-01-20")),
        boundsOf(all));
    assertTrue(all.get(0).isManual());
    assertNull(all.get(0).getScheduled());
    assertFalse(all.get(1).isManual());
  }

  @Test
  void rejectsInvalidChanges() {
    MonthlySchedule january = schedule("2026-01-01", 1, "2025-12-01");
    days(january, "2026-01-10", 10, "PHYSICS");
    days(january, "2026-01-20", 1, "OFF");
    days(january, "2026-01-21", 5, "PHYSICS");
    flushAndClear();
    runs.recalculate();
    flushAndClear();

    List<Run> all = runs.findAll();
    BigInteger later = all.get(0).getRunId();
    BigInteger earlier = all.get(1).getRunId();

    assertThrows(UserFriendlyException.class, () -> runs.add(day("2026-03-02"), day("2026-03-01")));
    assertThrows(UserFriendlyException.class, () -> runs.add(null, day("2026-03-01")));
    assertThrows(UserFriendlyException.class, () -> runs.add(day("2026-03-01"), null));
    assertThrows(
        UserFriendlyException.class,
        () -> runs.edit(BigInteger.valueOf(-1), day("2026-03-01"), day("2026-03-02")));
    // Into the next run
    assertThrows(
        UserFriendlyException.class,
        () -> runs.edit(earlier, day("2026-01-10"), day("2026-01-21")));
    // From the schedules: removing it would only bring it back
    UserFriendlyException scheduled =
        assertThrows(UserFriendlyException.class, () -> runs.remove(later));
    assertTrue(scheduled.getMessage().startsWith("This run comes from the published schedules"));
  }

  @Test
  void findsThePreviousRun() {
    MonthlySchedule january = schedule("2026-01-01", 1, "2025-12-01");
    days(january, "2026-01-01", 5, "PHYSICS");
    days(january, "2026-01-06", 5, "OFF");
    days(january, "2026-01-11", 5, "PHYSICS");
    flushAndClear();
    runs.recalculate();
    flushAndClear();

    Run later = runs.findAll().get(0);

    assertEquals(date("2026-01-01"), runs.getRunBefore(later).getStartDay());
    assertNull(runs.getRunBefore(runs.getRunBefore(later)));
  }

  private static List<BigInteger> ids(List<Run> list) {
    return list.stream().map(Run::getRunId).toList();
  }

  private static List<Bounds> boundsOf(List<Run> list) {
    return list.stream().map(RunServiceIT::boundsOf).toList();
  }

  private static Bounds boundsOf(Run run) {
    return new Bounds(
        LocalDate.ofInstant(run.getStartDay().toInstant(), ZoneId.of("America/New_York")),
        LocalDate.ofInstant(run.getEndDay().toInstant(), ZoneId.of("America/New_York")));
  }

  private static Bounds bounds(String start, String end) {
    return new Bounds(LocalDate.parse(start), LocalDate.parse(end));
  }

  private static LocalDate day(String day) {
    return LocalDate.parse(day);
  }

  private void days(MonthlySchedule schedule, String first, int count, String program) {
    LocalDate day = LocalDate.parse(first);
    for (int i = 0; i < count; i++) {
      day(schedule, day.plusDays(i).toString(), program);
    }
  }

  private void injectEntityManager(Object service) throws ReflectiveOperationException {
    Field field = service.getClass().getDeclaredField("em");
    field.setAccessible(true);
    field.set(service, em);
  }

  private MonthlySchedule schedule(String month, int version, String publication) {
    MonthlySchedule schedule = new MonthlySchedule();
    schedule.setStartDay(date(month));
    schedule.setVersion(version);
    schedule.setPublishedDate(publication == null ? null : date(publication));
    em.persist(schedule);
    return schedule;
  }

  private ScheduleDay day(MonthlySchedule schedule, String date, String program) {
    ScheduleDay day = new ScheduleDay();
    day.setMonthlySchedule(schedule);
    day.setDayMonthYear(date(date));
    day.setAccProgram(program);
    day.setHallAProgramId(11);
    day.setHallBProgramId(12);
    day.setHallCProgramId(13);
    day.setHallDProgramId(14);
    day.setHallAPolarized(true);
    day.setHallBPolarized(false);
    day.setHallCPolarized(true);
    day.setHallDPolarized(false);
    em.persist(day);
    return day;
  }

  private void flushAndClear() {
    em.flush();
    em.clear();
  }

  private static Date date(String date) {
    return Date.from(LocalDate.parse(date).atStartOfDay(ZoneId.of("America/New_York")).toInstant());
  }
}
