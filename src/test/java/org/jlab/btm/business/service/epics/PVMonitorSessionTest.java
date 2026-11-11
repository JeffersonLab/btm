package org.jlab.btm.business.service.epics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.cosylab.epics.caj.cas.CAJServerContext;
import com.cosylab.epics.caj.cas.util.DefaultServerImpl;
import com.cosylab.epics.caj.cas.util.MemoryProcessVariable;
import gov.aps.jca.CAException;
import gov.aps.jca.JCALibrary;
import gov.aps.jca.configuration.Configuration;
import gov.aps.jca.configuration.ConfigurationException;
import gov.aps.jca.configuration.DefaultConfiguration;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DBR_Double;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Runs {@link PVMonitorSession} against a synthetic IOC: CAJ's in-process Channel Access server,
 * bound to loopback on free ports, with made-up PV names. No EPICS service outside the test JVM is
 * contacted, and the client doesn't use the default CA ports, so a repeater or IOC elsewhere on the
 * machine isn't involved.
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

  private record Update(String pv, double value) {}

  @BeforeEach
  void pickPorts() throws IOException {
    serverPort = freePort();
    beaconPort = freePort();
    repeaterPort = freePort();
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
    assertEquals(0, session.monitorCount());

    SyntheticIoc ioc = startIoc(PVS, 1.0);

    awaitValues(PVS, 1.0);
    assertEquals(3, session.monitorCount());
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
    assertEquals(2, session.monitorCount());

    ioc.add("TEST:PV:C", 5.0);

    awaitValues(List.of("TEST:PV:C"), 5.0);
    assertEquals(3, session.monitorCount());
    assertTrue(session.missingValues().isEmpty());
  }

  @Test
  void iocRestartReconnectsWithoutDuplicateMonitors() throws Exception {
    SyntheticIoc first = startIoc(PVS, 1.0);
    PVMonitorSession session = open();

    assertTrue(session.awaitInitialValues(WAIT).isEmpty());

    first.close();
    SyntheticIoc second = startIoc(PVS, 2.0);

    awaitValues(PVS, 2.0);
    assertEquals(3, session.monitorCount());

    second.write("TEST:PV:A", 3.0);

    awaitValues(List.of("TEST:PV:A"), 3.0);
    Thread.sleep(500); // Time for a duplicate monitor's copy of the update to arrive
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
      assertEquals(3, session.monitorCount());

      session.close();
      session.close(); // Twice, as reset() after a shutdown might

      assertTrue(session.isContextDestroyed());
    }

    updates.clear();
    ioc.write("TEST:PV:A", 9.0);
    Thread.sleep(500);

    assertEquals(List.of(), updates, "updates after every session closed");
  }

  private PVMonitorSession open() throws CAException {
    PVMonitorSession session =
        PVMonitorSession.open(
            clientConfiguration(),
            PVS,
            (pv, entry) -> {
              double value = ((DBR_Double) entry.getDbr()).getDoubleValue()[0];
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

  /** A port free for both TCP and UDP on loopback, as a CA server binds both. */
  private static int freePort() throws IOException {
    for (int attempt = 0; attempt < 20; attempt++) {
      int port;

      try (ServerSocket tcp = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
        port = tcp.getLocalPort();
      }

      try (DatagramSocket udp =
          new DatagramSocket(new InetSocketAddress(InetAddress.getLoopbackAddress(), port))) {
        return port;
      } catch (IOException e) {
        // Taken for UDP; try another
      }
    }

    throw new IOException("No free port");
  }

  /** CAJ's Channel Access server with in-memory double PVs. */
  private static final class SyntheticIoc implements AutoCloseable {
    private final DefaultServerImpl server = new DefaultServerImpl();
    private final Map<String, MemoryProcessVariable> pvs = new ConcurrentHashMap<>();
    private final CAJServerContext context;
    private final Thread thread;
    private boolean closed = false;

    SyntheticIoc(int serverPort, int beaconPort) throws CAException {
      DefaultConfiguration config = new DefaultConfiguration("test-ioc");
      config.setAttribute("server_port", Integer.toString(serverPort));
      config.setAttribute("beacon_addr_list", "127.0.0.1");
      config.setAttribute("auto_beacon_addr_list", "false");
      config.setAttribute("beacon_port", Integer.toString(beaconPort));

      // Configured before initialize(): JCALibrary.createServerContext() configures after it, when
      // the server has already bound the default port 5064.
      context = new CAJServerContext();

      try {
        context.configure(config);
      } catch (ConfigurationException e) {
        throw new CAException("Unable to configure synthetic IOC", e);
      }

      context.initialize(server);

      if (context.getServerPort() != serverPort) {
        context.destroy();
        throw new CAException("Synthetic IOC bound port " + context.getServerPort());
      }

      thread =
          new Thread(
              () -> {
                try {
                  context.run(0);
                } catch (CAException e) {
                  throw new IllegalStateException(e);
                }
              },
              "synthetic-ioc");
      thread.setDaemon(true);
      thread.start();
    }

    void add(String name, double value) {
      pvs.put(name, server.createMemoryProcessVariable(name, DBRType.DOUBLE, new double[] {value}));
    }

    void write(String name, double value) throws CAException {
      pvs.get(name).write(new DBR_Double(new double[] {value}), null);
    }

    @Override
    public void close() {
      try {
        if (closed) {
          return;
        }

        closed = true;
        context.destroy();

        thread.join(WAIT.toMillis());
      } catch (CAException | InterruptedException e) {
        throw new IllegalStateException(e);
      }
    }
  }
}
