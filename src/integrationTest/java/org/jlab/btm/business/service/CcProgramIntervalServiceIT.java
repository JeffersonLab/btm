package org.jlab.btm.business.service;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.ejb.SessionContext;
import jakarta.persistence.EntityManager;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigInteger;
import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.jlab.btm.business.util.ProgramAccounting.Span;
import org.jlab.btm.persistence.entity.CcAccHour;
import org.jlab.btm.persistence.entity.CcProgramInterval;
import org.jlab.btm.persistence.enumeration.TimelineProgram;
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
 * Tests the State Timeline's queries and changes, DTM's downtime as spans, and the hours calculated
 * from both, against Oracle with the project's DDL. EJB security and JTA require WildFly.
 */
@Testcontainers
class CcProgramIntervalServiceIT {
  private static final BigInteger BLOCKED = BigInteger.ONE;
  private static final BigInteger TUNING = BigInteger.valueOf(9);

  @Container
  private static final OracleContainer ORACLE =
      new OracleContainer("gvenzl/oracle-xe:21.3.0")
          .withUsername("BTM_OWNER")
          .withPassword("btm_test_password")
          .withEnv("TZ", "America/New_York")
          .withLabel("org.jlab.btm.test-owner", "state-timeline-integration-tests")
          .withCopyFileToContainer(
              MountableFile.forHostPath("container/oracle/initdb.d/02_ddl.sql"),
              "/container-entrypoint-initdb.d/02_ddl.sql");

  private static SessionFactory factory;
  private EntityManager em;
  private CcProgramIntervalService timeline;
  private DowntimeService downtime;
  private CcAccHourService accHours;

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
              .addAnnotatedClass(CcProgramInterval.class)
              .addAnnotatedClass(CcAccHour.class)
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

    timeline = new CcProgramIntervalService();
    downtime = new DowntimeService();
    accHours = new CcAccHourService();

    // Supply what WildFly normally injects, without changing app code.
    injectEntityManager(timeline);
    injectEntityManager(downtime);
    injectEntityManager(accHours);
    timeline.context = callerNamed("jdoe");
    accHours.programService = timeline;
    accHours.downtimeService = downtime;
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
  void anEmptyTimelineHasNoCurrentProgram() {
    assertNull(timeline.findCurrent());
    assertEquals(0, timeline.count());
    assertTrue(timeline.findPage(0, 10).isEmpty());
    assertTrue(
        timeline.findOverlapping(at("2026-01-01T00:00:00Z"), at("2026-01-02T00:00:00Z")).isEmpty());
  }

  /**
   * 01:30 happens twice on the night daylight saving time ends. A start stored as a wall clock
   * time, as a Date is through JDBC, would be the same row for both.
   */
  @Test
  void keepsStartsInTheRepeatedHourApart() {
    Instant daylight = at("2026-11-01T05:30:00Z"); // 01:30 EDT
    Instant standard = at("2026-11-01T06:30:00Z"); // 01:30 EST

    assertTrue(timeline.recordMeasured(TimelineProgram.PHYSICS, daylight));
    assertTrue(timeline.recordMeasured(TimelineProgram.STUDIES, standard));
    flushAndClear();

    List<CcProgramInterval> intervals =
        timeline.findOverlapping(at("2026-11-01T05:00:00Z"), at("2026-11-01T07:00:00Z"));

    assertEquals(List.of(daylight, standard), starts(intervals));
    assertEquals(standard, intervals.get(0).getEndTime());
    assertEquals(
        1, count("START_TIME = timestamp '2026-11-01 05:30:00 +00:00' and PROGRAM = 'PHYSICS'"));
    assertEquals(
        1, count("START_TIME = timestamp '2026-11-01 06:30:00 +00:00' and PROGRAM = 'STUDIES'"));
  }

  @Test
  void pagesFromTheLatestIntervalBackWithEachEnd() {
    measured(TimelineProgram.OFF, "2026-01-01T00:00:00Z");
    measured(TimelineProgram.RESTORE, "2026-01-02T00:00:00Z");
    measured(TimelineProgram.PHYSICS, "2026-01-03T00:00:00Z");
    measured(TimelineProgram.STUDIES, "2026-01-04T00:00:00Z");
    flushAndClear();

    assertEquals(4, timeline.count());
    assertEquals(TimelineProgram.STUDIES, timeline.findCurrent().getProgram());

    List<CcProgramInterval> first = timeline.findPage(0, 2);

    assertEquals(List.of(TimelineProgram.STUDIES, TimelineProgram.PHYSICS), programs(first));
    assertNull(first.get(0).getEndTime(), "the current program hasn't ended");
    assertEquals(at("2026-01-04T00:00:00Z"), first.get(1).getEndTime());

    List<CcProgramInterval> second = timeline.findPage(2, 2);

    assertEquals(List.of(TimelineProgram.RESTORE, TimelineProgram.OFF), programs(second));
    assertEquals(at("2026-01-03T00:00:00Z"), second.get(0).getEndTime());
    assertEquals(at("2026-01-02T00:00:00Z"), second.get(1).getEndTime());

    assertTrue(timeline.findPage(4, 2).isEmpty());
  }

  @Test
  void findsTheIntervalInProgressWhenARangeStartsAndThoseStartingInIt() {
    measured(TimelineProgram.OFF, "2026-01-01T00:00:00Z");
    measured(TimelineProgram.RESTORE, "2026-01-02T00:00:00Z");
    measured(TimelineProgram.PHYSICS, "2026-01-02T12:00:00Z");
    measured(TimelineProgram.STUDIES, "2026-01-03T00:00:00Z");
    flushAndClear();

    List<CcProgramInterval> intervals =
        timeline.findOverlapping(at("2026-01-02T06:00:00Z"), at("2026-01-03T00:00:00Z"));

    assertEquals(List.of(TimelineProgram.RESTORE, TimelineProgram.PHYSICS), programs(intervals));
    assertEquals(at("2026-01-02T12:00:00Z"), intervals.get(0).getEndTime());
    assertNull(intervals.get(1).getEndTime());

    // A range from before the timeline starts
    assertEquals(
        List.of(TimelineProgram.OFF),
        programs(timeline.findOverlapping(at("2025-12-31T00:00:00Z"), at("2026-01-01T12:00:00Z"))));
    assertTrue(
        timeline.findOverlapping(at("2025-12-30T00:00:00Z"), at("2025-12-31T00:00:00Z")).isEmpty());
  }

  @Test
  void recordsAMeasuredProgramOnlyWhenItChanges() {
    assertTrue(timeline.recordMeasured(TimelineProgram.PHYSICS, at("2026-01-01T00:00:00.900Z")));
    assertFalse(timeline.recordMeasured(TimelineProgram.PHYSICS, at("2026-01-01T01:00:00Z")));
    assertTrue(timeline.recordMeasured(TimelineProgram.ACC, at("2026-01-01T02:00:00Z")));
    flushAndClear();

    List<CcProgramInterval> intervals = timeline.findPage(0, 10);

    assertEquals(List.of(TimelineProgram.ACC, TimelineProgram.PHYSICS), programs(intervals));
    assertEquals(at("2026-01-01T00:00:00Z"), intervals.get(1).getStartTime(), "whole seconds");
    assertNull(intervals.get(0).getModifiedBy(), "not changed by a person");
  }

  @Test
  void skipsAMeasurementFromBeforeTheLatestInterval() throws UserFriendlyException {
    measured(TimelineProgram.PHYSICS, "2026-01-01T00:00:00Z");
    timeline.add(TimelineProgram.STUDIES, at("2026-01-01T10:00:00Z"));
    flushAndClear();

    assertFalse(timeline.recordMeasured(TimelineProgram.ACC, at("2026-01-01T09:59:58Z")));
    assertFalse(timeline.recordMeasured(TimelineProgram.ACC, at("2026-01-01T10:00:00Z")));
    assertEquals(TimelineProgram.STUDIES, timeline.findCurrent().getProgram());
  }

  @Test
  void addingAChangeEndsTheIntervalItFallsIn() throws UserFriendlyException {
    measured(TimelineProgram.PHYSICS, "2026-01-01T00:00:00Z");
    measured(TimelineProgram.OFF, "2026-01-02T00:00:00Z");
    flushAndClear();

    timeline.add(TimelineProgram.STUDIES, at("2026-01-01T10:00:00Z"));
    flushAndClear();

    List<CcProgramInterval> intervals = timeline.findPage(0, 10);

    assertEquals(
        List.of(TimelineProgram.OFF, TimelineProgram.STUDIES, TimelineProgram.PHYSICS),
        programs(intervals));
    assertEquals(at("2026-01-02T00:00:00Z"), intervals.get(1).getEndTime());
    assertEquals(at("2026-01-01T10:00:00Z"), intervals.get(2).getEndTime());
    assertEquals("jdoe", intervals.get(1).getModifiedBy());
    assertNotNull(intervals.get(1).getModifiedDate());
  }

  @Test
  void addingTheNextIntervalsProgramEarlierMovesItsStart() throws UserFriendlyException {
    measured(TimelineProgram.PHYSICS, "2026-01-01T00:00:00Z");
    measured(TimelineProgram.STUDIES, "2026-01-01T11:30:00Z");
    flushAndClear();

    timeline.add(TimelineProgram.STUDIES, at("2026-01-01T10:00:00Z"));
    flushAndClear();

    List<CcProgramInterval> intervals = timeline.findPage(0, 10);

    assertEquals(List.of(TimelineProgram.STUDIES, TimelineProgram.PHYSICS), programs(intervals));
    assertEquals(at("2026-01-01T10:00:00Z"), intervals.get(0).getStartTime());
  }

  @Test
  void refusesAnAdditionThatChangesNothingOrIsInTheFuture() {
    measured(TimelineProgram.PHYSICS, "2026-01-01T00:00:00Z");
    flushAndClear();

    assertRefused("already starts at", TimelineProgram.OFF, at("2026-01-01T00:00:00Z"));
    assertRefused("already PHYSICS", TimelineProgram.PHYSICS, at("2026-01-01T05:00:00Z"));
    assertRefused("future", TimelineProgram.OFF, Instant.now().plus(1, ChronoUnit.HOURS));
    assertRefused("Program is required", null, at("2026-01-01T05:00:00Z"));
    assertRefused("Start is required", TimelineProgram.OFF, null);
    assertEquals(1, timeline.count());
  }

  @Test
  void editingMovesAStartBetweenItsNeighborsAndChangesTheProgram() throws UserFriendlyException {
    measured(TimelineProgram.PHYSICS, "2026-01-01T00:00:00Z");
    CcProgramInterval studies = measured(TimelineProgram.STUDIES, "2026-01-01T11:30:00Z");
    measured(TimelineProgram.OFF, "2026-01-02T00:00:00Z");
    flushAndClear();

    timeline.edit(
        studies.getCcProgramIntervalId(), TimelineProgram.ACC, at("2026-01-01T10:00:00Z"));
    flushAndClear();

    List<CcProgramInterval> intervals = timeline.findPage(0, 10);

    assertEquals(
        List.of(TimelineProgram.OFF, TimelineProgram.ACC, TimelineProgram.PHYSICS),
        programs(intervals));
    assertEquals(at("2026-01-01T10:00:00Z"), intervals.get(1).getStartTime());
    assertEquals(at("2026-01-01T10:00:00Z"), intervals.get(2).getEndTime());
    assertEquals("jdoe", intervals.get(1).getModifiedBy());

    BigInteger id = studies.getCcProgramIntervalId();

    assertEquals(
        "Start must be after the start of the interval before it, 31-Dec-2025 19:00:00",
        assertThrows(
                UserFriendlyException.class,
                () -> timeline.edit(id, TimelineProgram.ACC, at("2026-01-01T00:00:00Z")))
            .getMessage());
    assertEquals(
        "Start must be before the start of the interval after it, 01-Jan-2026 19:00:00",
        assertThrows(
                UserFriendlyException.class,
                () -> timeline.edit(id, TimelineProgram.ACC, at("2026-01-02T00:00:00Z")))
            .getMessage());
    assertThrows(
        UserFriendlyException.class,
        () ->
            timeline.edit(BigInteger.valueOf(-1), TimelineProgram.ACC, at("2026-01-01T10:00:00Z")));
  }

  @Test
  void editingAnIntervalToANeighborsProgramMergesThem() throws UserFriendlyException {
    measured(TimelineProgram.PHYSICS, "2026-01-01T00:00:00Z");
    CcProgramInterval studies = measured(TimelineProgram.STUDIES, "2026-01-01T10:00:00Z");
    measured(TimelineProgram.PHYSICS, "2026-01-01T12:00:00Z");
    measured(TimelineProgram.OFF, "2026-01-02T00:00:00Z");
    flushAndClear();

    timeline.edit(
        studies.getCcProgramIntervalId(), TimelineProgram.PHYSICS, studies.getStartTime());
    flushAndClear();

    List<CcProgramInterval> intervals = timeline.findPage(0, 10);

    assertEquals(List.of(TimelineProgram.OFF, TimelineProgram.PHYSICS), programs(intervals));
    assertEquals(at("2026-01-01T00:00:00Z"), intervals.get(1).getStartTime());
  }

  @Test
  void removingAnIntervalExtendsTheOneBeforeIt() throws UserFriendlyException {
    measured(TimelineProgram.PHYSICS, "2026-01-01T00:00:00Z");
    CcProgramInterval studies = measured(TimelineProgram.STUDIES, "2026-01-01T10:00:00Z");
    measured(TimelineProgram.PHYSICS, "2026-01-01T12:00:00Z");
    CcProgramInterval off = measured(TimelineProgram.OFF, "2026-01-02T00:00:00Z");
    flushAndClear();

    timeline.remove(studies.getCcProgramIntervalId());
    flushAndClear();

    List<CcProgramInterval> intervals = timeline.findPage(0, 10);

    assertEquals(List.of(TimelineProgram.OFF, TimelineProgram.PHYSICS), programs(intervals));
    assertEquals(at("2026-01-02T00:00:00Z"), intervals.get(1).getEndTime());

    timeline.remove(off.getCcProgramIntervalId());
    flushAndClear();

    assertEquals(TimelineProgram.PHYSICS, timeline.findCurrent().getProgram());
    assertThrows(
        UserFriendlyException.class, () -> timeline.remove(studies.getCcProgramIntervalId()));
  }

  @Test
  void findsDowntimeOfOneEventTypeAsSpansOfExactInstants() {
    event(1, BLOCKED, "2026-11-01 05:30:00 +00:00", "2026-11-01 06:30:00 +00:00");
    event(2, TUNING, "2026-11-01 05:40:00 +00:00", "2026-11-01 05:50:00 +00:00");
    event(3, BLOCKED, "2026-11-01 08:00:00 +00:00", null);
    event(4, BLOCKED, "2026-11-01 12:00:00 +00:00", "2026-11-01 13:00:00 +00:00");
    em.flush();

    assertEquals(
        List.of(
            new Span(at("2026-11-01T05:30:00Z"), at("2026-11-01T06:30:00Z")),
            new Span(at("2026-11-01T08:00:00Z"), null)),
        downtime.findDowntime(at("2026-11-01T06:00:00Z"), at("2026-11-01T12:00:00Z"), BLOCKED));
    assertEquals(
        List.of(new Span(at("2026-11-01T05:40:00Z"), at("2026-11-01T05:50:00Z"))),
        downtime.findDowntime(at("2026-11-01T05:00:00Z"), at("2026-11-01T12:00:00Z"), TUNING));
  }

  @Test
  void calculatesInternalDownFromDowntimeDuringInternalPrograms() {
    measured(TimelineProgram.PHYSICS, "2026-01-01T05:00:00Z");
    measured(TimelineProgram.STUDIES, "2026-01-01T06:30:00Z");
    measured(TimelineProgram.OFF, "2026-01-01T08:00:00Z");
    event(1, BLOCKED, "2026-01-01 05:45:00 +00:00", "2026-01-01 06:45:00 +00:00");
    event(2, BLOCKED, "2026-01-01 07:15:00 +00:00", "2026-01-01 07:45:00 +00:00");
    event(3, TUNING, "2026-01-01 06:45:00 +00:00", "2026-01-01 07:00:00 +00:00");
    flushAndClear();

    List<CcAccHour> hours =
        accHours.findInTimeline(date("2026-01-01T05:00:00Z"), date("2026-01-01T08:00:00Z"));

    assertEquals(4, hours.size());

    // 05: Physics all hour; downtime during Physics isn't internal
    assertHour(hours.get(0), "2026-01-01T05:00:00Z", 3600, 0, 0, 0);
    // 06: Physics until 06:30, then Studies, down for its first 15 minutes
    assertHour(hours.get(1), "2026-01-01T06:00:00Z", 1800, 900, 900, 0);
    // 07: Studies, down for 30 minutes; tuning events don't count
    assertHour(hours.get(2), "2026-01-01T07:00:00Z", 0, 1800, 1800, 0);
    // 08: Off
    assertHour(hours.get(3), "2026-01-01T08:00:00Z", 0, 0, 0, 3600);
  }

  @Test
  void calculatesNoHoursWithoutATimeline() {
    event(1, BLOCKED, "2026-01-01 05:45:00 +00:00", "2026-01-01 06:45:00 +00:00");
    em.flush();

    assertTrue(
        accHours
            .findInTimeline(date("2026-01-01T05:00:00Z"), date("2026-01-01T08:00:00Z"))
            .isEmpty());
  }

  private void assertHour(
      CcAccHour hour, String start, int physics, int studies, int internalDown, int off) {
    assertEquals(date(start), hour.getDayAndHour());
    assertEquals(physics, hour.getUpSeconds(), "physics");
    assertEquals(studies, hour.getStudiesSeconds(), "studies");
    assertEquals(internalDown, hour.getDownSeconds(), "internal down");
    assertEquals(off, hour.getSadSeconds(), "off");
    assertEquals(0, hour.getRestoreSeconds(), "restore");
    assertEquals(0, hour.getAccSeconds(), "acc");
  }

  private void assertRefused(String messagePart, TimelineProgram program, Instant start) {
    UserFriendlyException e =
        assertThrows(UserFriendlyException.class, () -> timeline.add(program, start));

    assertTrue(e.getMessage().contains(messagePart), e.getMessage());
  }

  private CcProgramInterval measured(TimelineProgram program, String start) {
    assertTrue(timeline.recordMeasured(program, at(start)));

    return timeline.findCurrent();
  }

  private void event(int id, BigInteger typeId, String down, String up) {
    em.createNativeQuery(
            "insert into BTM_OWNER.EVENT_FIRST_INCIDENT (EVENT_ID, EVENT_TITLE, TIME_UP,"
                + " EVENT_TYPE_ID, INCIDENT_ID, TITLE, TIME_DOWN, NUMBER_OF_INCIDENTS) values (:id,"
                + " 'Synthetic event', to_timestamp_tz(:up, 'YYYY-MM-DD HH24:MI:SS TZH:TZM'),"
                + " :typeId, :id, 'Synthetic incident',"
                + " to_timestamp_tz(:down, 'YYYY-MM-DD HH24:MI:SS TZH:TZM'), 1)")
        .setParameter("id", id)
        .setParameter("typeId", typeId)
        .setParameter("down", down)
        .setParameter("up", up == null ? "" : up)
        .executeUpdate();
  }

  private long count(String where) {
    return ((Number)
            em.createNativeQuery(
                    "select count(*) from BTM_OWNER.CC_PROGRAM_INTERVAL where " + where)
                .getSingleResult())
        .longValue();
  }

  private void injectEntityManager(Object service) throws ReflectiveOperationException {
    Field field = service.getClass().getDeclaredField("em");
    field.setAccessible(true);
    field.set(service, em);
  }

  private static SessionContext callerNamed(String username) {
    Principal principal = () -> username;

    return (SessionContext)
        Proxy.newProxyInstance(
            SessionContext.class.getClassLoader(),
            new Class<?>[] {SessionContext.class},
            (proxy, method, args) -> {
              if ("getCallerPrincipal".equals(method.getName())) {
                return principal;
              }
              throw new UnsupportedOperationException(method.getName());
            });
  }

  private void flushAndClear() {
    em.flush();
    em.clear();
  }

  private static List<TimelineProgram> programs(List<CcProgramInterval> intervals) {
    return intervals.stream().map(CcProgramInterval::getProgram).toList();
  }

  private static List<Instant> starts(List<CcProgramInterval> intervals) {
    return intervals.stream().map(CcProgramInterval::getStartTime).toList();
  }

  private static Instant at(String instant) {
    return Instant.parse(instant);
  }

  private static Date date(String instant) {
    return Date.from(Instant.parse(instant));
  }
}
