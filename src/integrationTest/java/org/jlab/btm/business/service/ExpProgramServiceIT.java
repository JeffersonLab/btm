package org.jlab.btm.business.service;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityManager;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.jlab.btm.persistence.entity.ExpProgram;
import org.jlab.btm.persistence.entity.ExpShift;
import org.jlab.btm.persistence.entity.MonthlySchedule;
import org.jlab.btm.persistence.entity.ScheduleDay;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.persistence.enumeration.Hall;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

/**
 * Tests which programs can be removed, against Oracle with the project's DDL. Schedule days keep
 * each hall's program ID without a foreign key, so only the service prevents orphaned references.
 * EJB security and JTA require WildFly.
 */
@Testcontainers
class ExpProgramServiceIT {
  @Container
  private static final OracleContainer ORACLE =
      new OracleContainer("gvenzl/oracle-xe:21.3.0")
          .withUsername("BTM_OWNER")
          .withPassword("btm_test_password")
          .withEnv("TZ", "America/New_York")
          .withLabel("org.jlab.btm.test-owner", "program-integration-tests")
          .withCopyFileToContainer(
              MountableFile.forHostPath("container/oracle/initdb.d/02_ddl.sql"),
              "/container-entrypoint-initdb.d/02_ddl.sql");

  private static SessionFactory factory;
  private EntityManager em;
  private ExpProgramService programs;

  @BeforeAll
  static void openFactory() {
    StandardServiceRegistry registry =
        new StandardServiceRegistryBuilder()
            .applySetting("hibernate.connection.url", ORACLE.getJdbcUrl())
            .applySetting("hibernate.connection.username", ORACLE.getUsername())
            .applySetting("hibernate.connection.password", ORACLE.getPassword())
            .applySetting("hibernate.hbm2ddl.auto", "none")
            .applySetting("jakarta.persistence.validation.mode", "none")
            // Shift auditing needs the app's revision entity; these tests do not audit.
            .applySetting("hibernate.integration.envers.enabled", "false")
            .build();
    try {
      factory =
          new MetadataSources(registry)
              .addAnnotatedClass(ExpProgram.class)
              .addAnnotatedClass(ExpShift.class)
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
    programs = new ExpProgramService();
    // Supply the real EntityManager normally injected by WildFly, without changing app code.
    Field field = ExpProgramService.class.getDeclaredField("em");
    field.setAccessible(true);
    field.set(programs, em);
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
  void removesAnUnusedProgram() throws UserFriendlyException {
    ExpProgram unused = program(Hall.A, "Unused");
    // A schedule that refers to other programs does not keep this one.
    day(schedule("2026-01-01", 1, null), "2026-01-02");
    flushAndClear();

    assertFalse(programs.isInUse(unused.getExpProgramId()));
    programs.remove(unused.getExpProgramId());
    flushAndClear();

    assertNull(programs.find(unused.getExpProgramId()));
  }

  @Test
  void rejectsRemovingAProgramUsedByAnExperimenterShift() {
    ExpProgram used = program(Hall.A, "Shift program");
    ExpShift shift = new ExpShift();
    shift.setHall(Hall.A);
    shift.setStartDayAndHour(date("2026-01-02"));
    shift.setExpProgram(used);
    em.persist(shift);
    flushAndClear();

    assertRemovalRejected(used);
  }

  @ParameterizedTest
  @EnumSource(Hall.class)
  void rejectsRemovingAProgramOnlyInATentativeSchedule(Hall hall) {
    ExpProgram scheduled = program(hall, "Tentative " + hall);
    ScheduleDay day = day(schedule("2026-01-01", 1, null), "2026-01-02");
    setProgram(day, hall, scheduled);
    flushAndClear();

    assertRemovalRejected(scheduled);
  }

  @ParameterizedTest
  @EnumSource(Hall.class)
  void rejectsRemovingAProgramOnlyInAPublishedScheduleAndKeepsItsName(Hall hall)
      throws UserFriendlyException {
    ExpProgram scheduled = program(hall, "Published " + hall);
    ScheduleDay day = day(schedule("2025-06-01", 1, "2025-05-20"), "2025-06-15");
    setProgram(day, hall, scheduled);
    flushAndClear();

    assertRemovalRejected(scheduled);

    // A program in use is retired by deactivating it.
    programs.edit(scheduled.getExpProgramId(), "Published " + hall, null, null, true, false);
    flushAndClear();

    ExpProgram kept = programs.findProgramByIdMap().get(programId(day, hall));
    assertNotNull(kept, "Schedule pages look up each hall's program name by its ID");
    assertEquals("Published " + hall, kept.getName());
    assertFalse(kept.isActive());
  }

  private void assertRemovalRejected(ExpProgram program) {
    BigInteger id = program.getExpProgramId();
    assertTrue(programs.isInUse(id));
    UserFriendlyException e = assertThrows(UserFriendlyException.class, () -> programs.remove(id));
    assertTrue(e.getMessage().contains("Update the Active attribute instead"));
    flushAndClear();
    assertNotNull(programs.find(id));
  }

  private ExpProgram program(Hall hall, String name) {
    ExpProgram program = new ExpProgram();
    program.setHall(hall);
    program.setName(name);
    program.setExperiment(true);
    program.setActive(true);
    em.persist(program);
    return program;
  }

  private MonthlySchedule schedule(String month, int version, String publication) {
    MonthlySchedule schedule = new MonthlySchedule();
    schedule.setStartDay(date(month));
    schedule.setVersion(version);
    schedule.setPublishedDate(publication == null ? null : date(publication));
    em.persist(schedule);
    return schedule;
  }

  /** A day whose required halls refer to their own placeholder programs. */
  private ScheduleDay day(MonthlySchedule schedule, String date) {
    ScheduleDay day = new ScheduleDay();
    day.setMonthlySchedule(schedule);
    day.setDayMonthYear(date(date));
    day.setAccProgram("PHYSICS");
    day.setHallAProgramId(program(Hall.A, "Other A " + date).getExpProgramId().intValue());
    day.setHallBProgramId(program(Hall.B, "Other B " + date).getExpProgramId().intValue());
    day.setHallAPolarized(true);
    day.setHallBPolarized(true);
    day.setHallCPolarized(true);
    day.setHallDPolarized(true);
    em.persist(day);
    return day;
  }

  private static void setProgram(ScheduleDay day, Hall hall, ExpProgram program) {
    Integer id = program.getExpProgramId().intValue();
    switch (hall) {
      case A -> day.setHallAProgramId(id);
      case B -> day.setHallBProgramId(id);
      case C -> day.setHallCProgramId(id);
      case D -> day.setHallDProgramId(id);
    }
  }

  private static Integer programId(ScheduleDay day, Hall hall) {
    return switch (hall) {
      case A -> day.getHallAProgramId();
      case B -> day.getHallBProgramId();
      case C -> day.getHallCProgramId();
      case D -> day.getHallDProgramId();
    };
  }

  private void flushAndClear() {
    em.flush();
    em.clear();
  }

  private static Date date(String date) {
    return Date.from(LocalDate.parse(date).atStartOfDay(ZoneId.of("America/New_York")).toInstant());
  }
}
