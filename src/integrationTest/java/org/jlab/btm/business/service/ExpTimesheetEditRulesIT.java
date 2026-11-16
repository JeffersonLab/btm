package org.jlab.btm.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.ejb.EJBAccessException;
import jakarta.persistence.EntityManager;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.jlab.btm.persistence.entity.ExpHour;
import org.jlab.btm.persistence.entity.ExpProgram;
import org.jlab.btm.persistence.entity.ExpReason;
import org.jlab.btm.persistence.entity.ExpShift;
import org.jlab.btm.persistence.entity.ExpSignature;
import org.jlab.btm.persistence.entity.ExpUedExplanation;
import org.jlab.btm.persistence.enumeration.Role;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.persistence.enumeration.Hall;
import org.jlab.smoothness.presentation.filter.AuditContext;
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
 * Tests that every change to an experimenter timesheet is checked against the rules of the
 * timesheet it changes: its signatures and the 48-hour limit belong to the shift, whichever of its
 * hours a request names. Runs against Oracle with the project's DDL; the caller and their roles are
 * a stand-in for WildFly's EJB security.
 */
@Testcontainers
class ExpTimesheetEditRulesIT {
  private static final ZoneId ZONE = ZoneId.of("America/New_York");

  /** A Day shift long enough ago that the 48-hour limit applies to it. */
  private static final Date OLD_SHIFT = date(LocalDateTime.of(2026, 1, 5, 8, 0));

  @Container
  private static final OracleContainer ORACLE =
      new OracleContainer("gvenzl/oracle-xe:21.3.0")
          .withUsername("BTM_OWNER")
          .withPassword("btm_test_password")
          .withEnv("TZ", "America/New_York")
          .withLabel("org.jlab.btm.test-owner", "timesheet-rules-integration-tests")
          .withCopyFileToContainer(
              MountableFile.forHostPath("container/oracle/initdb.d/02_ddl.sql"),
              "/container-entrypoint-initdb.d/02_ddl.sql");

  private static SessionFactory factory;
  private EntityManager em;
  private TestCaller caller;
  private ExpSecurityRuleService rules;
  private ExpHourService hours;
  private ExpUedExplanationService explanations;
  private ExpShiftService shifts;

  @BeforeAll
  static void openFactory() {
    StandardServiceRegistry registry =
        new StandardServiceRegistryBuilder()
            .applySetting("hibernate.connection.url", ORACLE.getJdbcUrl())
            .applySetting("hibernate.connection.username", ORACLE.getUsername())
            .applySetting("hibernate.connection.password", ORACLE.getPassword())
            .applySetting("hibernate.hbm2ddl.auto", "none")
            .applySetting("jakarta.persistence.validation.mode", "none")
            // Auditing needs the app's revision entity; these tests do not audit.
            .applySetting("hibernate.integration.envers.enabled", "false")
            .build();
    try {
      factory =
          new MetadataSources(registry)
              .addAnnotatedClass(ExpHour.class)
              .addAnnotatedClass(ExpProgram.class)
              .addAnnotatedClass(ExpReason.class)
              .addAnnotatedClass(ExpShift.class)
              .addAnnotatedClass(ExpSignature.class)
              .addAnnotatedClass(ExpUedExplanation.class)
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
  void wireServices() throws ReflectiveOperationException {
    em = factory.createEntityManager();
    em.getTransaction().begin();
    caller = new TestCaller();

    // Supply what WildFly normally injects, without changing app code.
    ExpSignatureService signatures = new ExpSignatureService();
    signatures.em = em;

    rules = new ExpSecurityRuleService();
    rules.context = caller.context();
    rules.signatureService = signatures;

    hours = new ExpHourService();
    inject(ExpHourService.class, hours, em);
    hours.ruleService = rules;

    explanations = new ExpUedExplanationService();
    inject(ExpUedExplanationService.class, explanations, em);
    explanations.ruleService = rules;

    shifts = new ExpShiftService();
    inject(ExpShiftService.class, shifts, em);
    shifts.ruleService = rules;

    // Saving hours writes an audit revision with the request's address and user
    TestAudit.use(new TestAudit());
  }

  @AfterEach
  void rollback() {
    TestAudit.use(null);
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
  void anHourOfAnAdminSignedShiftIsLockedLikeItsStart() {
    sign(Hall.A, OLD_SHIFT, "admin", Role.OPERABILITY_MANAGER);
    caller.is("jdoe");

    assertNotNull(rules.checkForEditDeniedReason(Hall.A, OLD_SHIFT));
    assertEquals(
        rules.checkForEditDeniedReason(Hall.A, OLD_SHIFT),
        rules.checkForEditDeniedReason(Hall.A, plusHours(OLD_SHIFT, 3)));
  }

  @Test
  void anHourOfAUserSignedShiftIsLockedToItsSignerAfter48Hours() {
    sign(Hall.A, OLD_SHIFT, "alice", Role.USER);

    caller.is("bob");
    assertNotNull(rules.checkForEditDeniedReason(Hall.A, plusHours(OLD_SHIFT, 3)));

    caller.is("alice");
    assertNull(rules.checkForEditDeniedReason(Hall.A, plusHours(OLD_SHIFT, 3)));
  }

  @Test
  void savingOneHourOfALockedTimesheetIsRefused() {
    sign(Hall.A, OLD_SHIFT, "admin", Role.OPERABILITY_MANAGER);
    caller.is("jdoe");

    assertThrows(EJBAccessException.class, () -> saveHours(Hall.A, plusHours(OLD_SHIFT, 3)));
    assertTrue(findHours(Hall.A, OLD_SHIFT).isEmpty());
  }

  @Test
  void savingHoursThatReachIntoALockedTimesheetIsRefused() {
    sign(Hall.A, OLD_SHIFT, "admin", Role.OPERABILITY_MANAGER);
    caller.is("jdoe");

    // The Owl shift's last hour, which isn't locked, and the locked Day shift's first
    assertThrows(
        EJBAccessException.class, () -> saveHours(Hall.A, plusHours(OLD_SHIFT, -1), OLD_SHIFT));
    assertTrue(findHours(Hall.A, plusHours(OLD_SHIFT, -8)).isEmpty());
  }

  @Test
  void savingOneHourOfAnUnlockedTimesheetWorks() throws UserFriendlyException {
    caller.is("jdoe");

    saveHours(Hall.A, plusHours(OLD_SHIFT, 3));

    assertEquals(1, findHours(Hall.A, OLD_SHIFT).size());
  }

  @Test
  void aReasonIsRemovedOnlyThroughItsOwnTimesheet() {
    ExpUedExplanation locked = explanation(Hall.A, plusHours(OLD_SHIFT, 3));
    sign(Hall.A, OLD_SHIFT, "admin", Role.OPERABILITY_MANAGER);
    caller.is("jdoe");

    // Named with another, unlocked, timesheet
    Date unlockedShift = plusHours(OLD_SHIFT, 8);
    assertThrows(
        UserFriendlyException.class,
        () -> explanations.remove(Hall.A, unlockedShift, locked.getExpUedExplanationId()));
    // Named with its own, locked, timesheet
    assertThrows(
        EJBAccessException.class,
        () -> explanations.remove(Hall.A, OLD_SHIFT, locked.getExpUedExplanationId()));

    em.flush();
    em.clear();
    assertNotNull(em.find(ExpUedExplanation.class, locked.getExpUedExplanationId()));
  }

  @Test
  void aReasonOfAnUnlockedTimesheetCanBeRemoved() throws UserFriendlyException {
    ExpUedExplanation unlocked = explanation(Hall.A, plusHours(OLD_SHIFT, 3));
    caller.is("jdoe");

    explanations.remove(Hall.A, OLD_SHIFT, unlocked.getExpUedExplanationId());

    em.flush();
    em.clear();
    assertNull(em.find(ExpUedExplanation.class, unlocked.getExpUedExplanationId()));
  }

  @Test
  void shiftInformationNeedsTheShiftsStart() {
    caller.is("jdoe");

    UserFriendlyException e =
        assertThrows(
            UserFriendlyException.class,
            () -> shifts.editShift(Hall.A, plusHours(OLD_SHIFT, 3), "lead", null, null, null));
    assertEquals("Shift Start hour must be one of 0, 8, 16", e.getUserMessage());
  }

  private void saveHours(Hall hall, Date... hourArray) throws UserFriendlyException {
    int n = hourArray.length;
    Short[] abu = new Short[n];
    Short[] zero = new Short[n];
    Short[] er = new Short[n];
    String[] comments = new String[n];
    for (int i = 0; i < n; i++) {
      abu[i] = 3600;
      zero[i] = 0;
      er[i] = 3600;
    }
    hours.editExpHours(hall, hourArray, abu, zero, zero, zero, er, zero, zero, comments);
    em.flush();
  }

  private List<ExpHour> findHours(Hall hall, Date shiftStart) {
    em.flush();
    em.clear();
    return em.createQuery(
            "select h from ExpHour h where h.hall = :hall and h.dayAndHour >= :start"
                + " and h.dayAndHour < :end",
            ExpHour.class)
        .setParameter("hall", hall)
        .setParameter("start", shiftStart)
        .setParameter("end", plusHours(shiftStart, 8))
        .getResultList();
  }

  private void sign(Hall hall, Date shiftStart, String username, Role role) {
    ExpSignature signature = new ExpSignature();
    signature.setHall(hall);
    signature.setStartDayAndHour(shiftStart);
    signature.setSignedBy(username);
    signature.setSignedRole(role);
    signature.setSignedDate(new Date());
    em.persist(signature);
    em.flush();
  }

  private ExpUedExplanation explanation(Hall hall, Date dayAndHour) {
    // The app inserts hours with SQL too: DAY_AND_HOUR isn't insertable through the entity
    BigInteger id =
        (BigInteger)
            em.createNativeQuery("select EXP_HOUR_ID.nextval from dual", BigInteger.class)
                .getSingleResult();
    em.createNativeQuery(
            "insert into EXP_HOUR (EXP_HOUR_ID, HALL, DAY_AND_HOUR, ABU_SECONDS, UED_SECONDS)"
                + " values (:id, :hall, :hour, 3600, 3600)")
        .setParameter("id", id)
        .setParameter("hall", hall.name())
        .setParameter("hour", new Timestamp(dayAndHour.getTime()))
        .executeUpdate();
    ExpHour hour = em.find(ExpHour.class, id);

    ExpReason reason = new ExpReason();
    reason.setHall(hall);
    reason.setName("Target");
    reason.setActive(true);
    em.persist(reason);

    ExpUedExplanation explanation = new ExpUedExplanation();
    explanation.setHall(hall);
    explanation.setExpHour(hour);
    explanation.setExpReason(reason);
    explanation.setSeconds((short) 3600);
    em.persist(explanation);
    em.flush();
    return explanation;
  }

  private static Date date(LocalDateTime local) {
    return Date.from(local.atZone(ZONE).toInstant());
  }

  private static Date plusHours(Date date, int hours) {
    return new Date(date.getTime() + hours * 3_600_000L);
  }

  private static <T> void inject(Class<T> type, T service, EntityManager em)
      throws ReflectiveOperationException {
    Field field = type.getDeclaredField("em");
    field.setAccessible(true);
    field.set(service, em);
  }

  /** The request's address and user, as Smoothness's audit filter would set them. */
  private static class TestAudit extends AuditContext {
    TestAudit() {
      setIp("127.0.0.1");
      setUsername("jdoe");
    }

    static void use(AuditContext context) {
      setCurrentInstance(context);
    }
  }
}
