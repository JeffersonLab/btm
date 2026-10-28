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
import org.jlab.btm.persistence.entity.MonthlySchedule;
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

/** Tests real service queries and entity mappings; EJB security and JTA require WildFly. */
@Testcontainers
class ScheduleServiceIT {
  @Container
  private static final OracleContainer ORACLE =
      new OracleContainer("gvenzl/oracle-xe:21.3.0")
          .withUsername("BTM_OWNER")
          .withPassword("btm_test_password")
          .withEnv("TZ", "America/New_York")
          .withLabel("org.jlab.btm.test-owner", "schedule-integration-tests")
          .withCopyFileToContainer(
              MountableFile.forHostPath("container/oracle/initdb.d/02_ddl.sql"),
              "/container-entrypoint-initdb.d/02_ddl.sql");

  private static SessionFactory factory;
  private EntityManager em;
  private MonthlyScheduleService schedules;
  private ScheduleDayService days;

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
    schedules = new MonthlyScheduleService();
    days = new ScheduleDayService();
    // Supply the real EntityManager normally injected by WildFly, without changing app code.
    injectEntityManager(schedules);
    injectEntityManager(days);
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
  void findsOnlyTheRequestedMonthInDescendingVersionOrder() {
    schedule("2026-01-01", 1, null);
    schedule("2026-01-01", 3, null);
    schedule("2026-01-01", 2, null);
    schedule("2026-02-01", 10, null);
    flushAndClear();

    assertEquals(
        List.of(3, 2, 1),
        schedules.findAll(date("2026-01-01")).stream().map(MonthlySchedule::getVersion).toList());
  }

  @Test
  void selectsTheLatestPublicationDateEvenWhenItsVersionIsLower() {
    MonthlySchedule latestPublished = schedule("2026-01-01", 1, "2026-01-10");
    schedule("2026-01-01", 2, "2026-01-05");
    schedule("2026-01-01", 3, null);
    flushAndClear();

    assertEquals(
        latestPublished.getMonthlyScheduleId(),
        schedules.findMostRecentPublished(date("2026-01-01")).getMonthlyScheduleId());
  }

  @Test
  void fallsBackToTheHighestTentativeVersion() {
    schedule("2026-01-01", 1, null);
    MonthlySchedule latest = schedule("2026-01-01", 4, null);
    schedule("2026-01-01", 2, null);
    flushAndClear();

    assertEquals(
        latest.getMonthlyScheduleId(),
        schedules.findMostRecentPublished(date("2026-01-01")).getMonthlyScheduleId());
  }

  @Test
  void returnsNoScheduleForAnAbsentMonth() {
    schedule("2026-02-01", 1, null);
    flushAndClear();

    assertTrue(schedules.findAll(date("2026-01-01")).isEmpty());
    assertNull(schedules.findMostRecentPublished(date("2026-01-01")));
  }

  @Test
  void findsSchedulesAcrossInclusiveMonthsAndSkipsAbsentMonths() {
    schedule("2025-12-01", 1, null);
    MonthlySchedule january = schedule("2026-01-01", 1, "2026-01-03");
    schedule("2026-01-01", 2, null);
    MonthlySchedule march = schedule("2026-03-01", 1, null);
    schedule("2026-04-01", 1, null);
    flushAndClear();

    assertEquals(
        List.of(january.getMonthlyScheduleId(), march.getMonthlyScheduleId()),
        schedules
            .findMostRecentPublishedInDateRange(date("2026-01-15"), date("2026-03-01"))
            .stream()
            .map(MonthlySchedule::getMonthlyScheduleId)
            .toList());
  }

  @Test
  void fillsMissingDaysAndMapsOracleProjectionValues() {
    MonthlySchedule schedule = schedule("2026-03-01", 1, null);
    ScheduleDay first = day(schedule, "2026-03-07", "PHYSICS");
    first.setKiloVoltsPerPass(1100000);
    first.setMinHallCount(2);
    first.setNote("Synthetic schedule note");
    first.setHallANote("Synthetic hall note");
    first.setHallANanoAmps(50);
    first.setHallAKiloVolts(2200000);
    first.setHallAPasses(2);
    first.setHallAPriority(1);
    day(schedule, "2026-03-09", "STUDIES");
    MonthlySchedule other = schedule("2026-03-01", 2, null);
    day(other, "2026-03-08", "OFF");
    flushAndClear();

    // This range crosses the spring DST transition; both endpoints are included.
    List<ScheduleDay> result =
        days.find(schedule.getMonthlyScheduleId(), date("2026-03-07"), date("2026-03-09"));

    assertEquals(
        List.of(date("2026-03-07"), date("2026-03-08"), date("2026-03-09")),
        result.stream().map(d -> new Date(d.getDayMonthYear().getTime())).toList());
    ScheduleDay projected = result.get(0);
    assertEquals("PHYSICS", projected.getAccProgram());
    assertEquals(1100000, projected.getKiloVoltsPerPass());
    assertEquals(2, projected.getMinHallCount());
    assertEquals("Synthetic schedule note", projected.getNote());
    assertEquals("Synthetic hall note", projected.getHallANote());
    assertEquals(11, projected.getHallAProgramId());
    assertEquals(50, projected.getHallANanoAmps());
    assertEquals(2200000, projected.getHallAKiloVolts());
    assertEquals(2, projected.getHallAPasses());
    assertEquals(1, projected.getHallAPriority());
    assertEquals(Boolean.TRUE, projected.getHallAPolarized());
    assertEquals(Boolean.FALSE, projected.getHallBPolarized());
    assertNull(projected.getHallCNanoAmps());
    assertNull(result.get(1).getAccProgram());
    assertNull(result.get(1).getHallAProgramId());
    assertNull(result.get(1).getHallAPolarized());
    assertEquals("STUDIES", result.get(2).getAccProgram());
  }

  @Test
  void returnsOnePlaceholderForAnEmptySingleDayRange() {
    MonthlySchedule schedule = schedule("2026-01-01", 1, null);
    flushAndClear();

    List<ScheduleDay> result =
        days.find(schedule.getMonthlyScheduleId(), date("2026-01-31"), date("2026-01-31"));

    assertEquals(1, result.size());
    assertEquals(date("2026-01-31").getTime(), result.get(0).getDayMonthYear().getTime());
    assertNull(result.get(0).getAccProgram());
  }

  @Test
  void looksUpPersistedDaysByBothDateAndSchedule() {
    MonthlySchedule first = schedule("2026-01-01", 1, null);
    MonthlySchedule second = schedule("2026-01-01", 2, null);
    ScheduleDay expected = day(first, "2026-01-02", "PHYSICS");
    day(second, "2026-01-02", "OFF");
    flushAndClear();

    ScheduleDay found =
        days.findByDateAndSchedule(date("2026-01-02"), first.getMonthlyScheduleId());
    assertEquals(expected.getScheduleDayId(), found.getScheduleDayId());
    assertEquals("PHYSICS", found.getAccProgram());
    assertEquals(first.getMonthlyScheduleId(), found.getMonthlySchedule().getMonthlyScheduleId());
    assertNull(days.findByDateAndSchedule(date("2026-01-03"), first.getMonthlyScheduleId()));
    assertNull(days.findByDateAndSchedule(date("2026-01-02"), BigInteger.valueOf(-1)));
  }

  @Test
  void createsSuccessiveVersionsAndPersistsPublication() throws UserFriendlyException {
    MonthlySchedule first = schedules.create(date("2026-01-01"));
    flushAndClear();
    MonthlySchedule second = schedules.create(date("2026-01-01"));
    flushAndClear();
    assertNotNull(first.getMonthlyScheduleId());
    assertNotEquals(first.getMonthlyScheduleId(), second.getMonthlyScheduleId());
    assertEquals(1, first.getVersion());
    assertEquals(2, second.getVersion());
    assertNull(second.getPublishedDate());

    schedules.publish(first.getMonthlyScheduleId());
    flushAndClear();

    MonthlySchedule published = schedules.findMostRecentPublished(date("2026-01-01"));
    assertEquals(first.getMonthlyScheduleId(), published.getMonthlyScheduleId());
    assertNotNull(published.getPublishedDate());
    assertThrows(
        UserFriendlyException.class, () -> schedules.publish(first.getMonthlyScheduleId()));
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
