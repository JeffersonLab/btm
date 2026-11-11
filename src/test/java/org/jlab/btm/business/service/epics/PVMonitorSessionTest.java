package org.jlab.btm.business.service.epics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import gov.aps.jca.CAException;
import gov.aps.jca.JCALibrary;
import gov.aps.jca.configuration.Configuration;
import gov.aps.jca.configuration.DefaultConfiguration;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Runs {@link PVMonitorSession} against a {@link SyntheticIoc}, bound to loopback on free ports,
 * with made-up PV names. No EPICS service outside the test JVM is contacted, and the client doesn't
 * use the default CA ports, so a repeater or IOC elsewhere on the machine isn't involved. The
 * client starts no CA repeater either: see {@link #disableRepeater()}.
 */
class PVMonitorSessionTest {
  private static final Duration WAIT = Duration.ofSeconds(30);
  private static final List<String> PVS = List.of("TEST:PV:A", "TEST:PV:B", "TEST:PV:C");

  private int serverPort;
  private int beaconPort;
  private int repeaterPort;
  private final List<PVMonitorSession> sessions = new ArrayList<>();
  private final List<SyntheticIoc> iocs = new ArrayList<>();
  private final List<Update> updates = new CopyOnWriteArrayList<>();
  private final Map<String, Double> latest = new ConcurrentHashMap<>();
  private final Map<String, Boolean> connected = new ConcurrentHashMap<>();

  private record Update(String pv, double value) {}

  private static final String DISABLE_REPEATER = "CA_DISABLE_REPEATER";
  private static String previousDisableRepeater;

  /**
   * A CAJ client context otherwise spawns a CA repeater JVM on its repeater port when none is
   * running there; it outlives the test run, one per test's random port. The tests don't need one:
   * they find channels by searching often, not from beacons.
   */
  @BeforeAll
  static void disableRepeater() {
    previousDisableRepeater = System.setProperty(DISABLE_REPEATER, "true");
  }

  @AfterAll
  static void restoreRepeater() {
    if (previousDisableRepeater == null) {
      System.clearProperty(DISABLE_REPEATER);
    } else {
      System.setProperty(DISABLE_REPEATER, previousDisableRepeater);
    }
  }

  @BeforeEach
  void pickPorts() throws IOException {
    serverPort = SyntheticIoc.freePort();
    beaconPort = SyntheticIoc.freePort();
    repeaterPort = SyntheticIoc.freePort();
  }

  @AfterEach
  void cleanUp() {
    sessions.forEach(PVMonitorSession::close);

    for (SyntheticIoc ioc : iocs) {
      ioc.close();
    }
  }

  @Test
  void monitorsAllPvsWhenTheIocComesUpAfterStartup() throws Exception {
    PVMonitorSession session = open();

    assertEquals(Set.copyOf(PVS), session.awaitInitialValues(Duration.ofSeconds(2)));
    assertEquals(0, session.connectedCount());

    SyntheticIoc ioc = startIoc(PVS, 1.0);

    awaitValues(PVS, 1.0);
    assertEquals(3, session.connectedCount());
    assertTrue(session.missingValues().isEmpty());

    ioc.write("TEST:PV:A", 2.0);

    awaitValues(List.of("TEST:PV:A"), 2.0);
  }

  @Test
  void monitorsAvailablePvsWhileOneIsMissing() throws Exception {
    SyntheticIoc ioc = startIoc(List.of("TEST:PV:A", "TEST:PV:B"), 1.0);

    PVMonitorSession session = open();

    assertEquals(Set.of("TEST:PV:C"), session.awaitInitialValues(Duration.ofSeconds(3)));
    assertEquals(1.0, latest.get("TEST:PV:A"));
    assertEquals(1.0, latest.get("TEST:PV:B"));
    assertEquals(2, session.connectedCount());

    ioc.add("TEST:PV:C", 5.0);

    awaitValues(List.of("TEST:PV:C"), 5.0);
    assertEquals(3, session.connectedCount());
    assertTrue(session.missingValues().isEmpty());
  }

  @Test
  void iocRestartReconnectsWithoutDuplicateMonitors() throws Exception {
    SyntheticIoc first = startIoc(PVS, 1.0);
    PVMonitorSession session = open();

    assertTrue(session.awaitInitialValues(WAIT).isEmpty());

    first.close();

    await(
        () -> PVS.stream().noneMatch(pv -> connected.get(pv)),
        () -> "connected " + connected + ", expected every PV disconnected");
    assertEquals(0, session.connectedCount());
    SyntheticIoc second = startIoc(PVS, 2.0);

    awaitValues(PVS, 2.0);
    assertEquals(3, session.connectedCount());

    second.write("TEST:PV:A", 3.0);

    awaitValues(List.of("TEST:PV:A"), 3.0);
    Thread.sleep(500); // Time for a duplicate subscription's copy of the update to arrive
    assertEquals(
        1,
        updates.stream().filter(u -> u.pv().equals("TEST:PV:A") && u.value() == 3.0).count(),
        "updates for one write");
  }

  @Test
  void repeatedResetsCloseEachContextAndStopItsUpdates() throws Exception {
    SyntheticIoc ioc = startIoc(PVS, 1.0);

    for (int i = 0; i < 5; i++) {
      PVMonitorSession session = open();

      assertTrue(session.awaitInitialValues(WAIT).isEmpty());
      assertEquals(3, session.connectedCount());

      session.close();
      session.close(); // Twice, as reset() after a shutdown might

      assertTrue(session.isContextDestroyed());
    }

    await(
        () -> sessionThreads().isEmpty(),
        () -> "threads left by closed sessions: " + sessionThreads());

    updates.clear();
    ioc.write("TEST:PV:A", 9.0);
    Thread.sleep(500);

    assertEquals(List.of(), updates, "updates after every session closed");
  }

  /** Live threads that a session or its gpclient data source started. */
  private static List<String> sessionThreads() {
    return Thread.getAllStackTraces().keySet().stream()
        .filter(Thread::isAlive)
        .map(Thread::getName)
        .filter(name -> name.startsWith("btm-pv-") || name.contains("CADataSource Worker"))
        .toList();
  }

  private PVMonitorSession open() throws CAException {
    PVMonitorSession session =
        PVMonitorSession.open(
            clientConfiguration(),
            PVS,
            (pv, entry) -> {
              double value = entry.getDoubleArray()[0];
              connected.put(pv, entry.isConnected());
              updates.add(new Update(pv, value));
              latest.put(pv, value);
            });

    sessions.add(session);

    return session;
  }

  private Configuration clientConfiguration() {
    DefaultConfiguration config = new DefaultConfiguration("test-monitor-context");
    config.setAttribute("class", JCALibrary.CHANNEL_ACCESS_JAVA);
    config.setAttribute("addr_list", "127.0.0.1");
    config.setAttribute("auto_addr_list", "false");
    config.setAttribute("server_port", Integer.toString(serverPort));
    config.setAttribute("repeater_port", Integer.toString(repeaterPort));
    // Search again soon for a channel that isn't found, rather than backing off to minutes
    config.setAttribute("max_search_interval", "0.5");
    return config;
  }

  private SyntheticIoc startIoc(List<String> pvs, double value) throws CAException {
    SyntheticIoc ioc = new SyntheticIoc(serverPort, beaconPort);
    iocs.add(ioc);

    for (String pv : pvs) {
      ioc.add(pv, value);
    }

    return ioc;
  }

  private void awaitValues(List<String> pvs, double value) throws InterruptedException {
    await(
        () -> pvs.stream().allMatch(pv -> Double.valueOf(value).equals(latest.get(pv))),
        () -> "values " + latest + ", expected " + value + " for " + pvs);
  }

  private static void await(BooleanSupplier condition, java.util.function.Supplier<String> message)
      throws InterruptedException {
    long deadline = System.nanoTime() + WAIT.toNanos();

    while (!condition.getAsBoolean()) {
      if (System.nanoTime() > deadline) {
        fail("Timed out after " + WAIT + ": " + message.get());
      }

      Thread.sleep(50);
    }
  }
}
