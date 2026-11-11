package org.jlab.btm.business.service.epics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import gov.aps.jca.CAException;
import gov.aps.jca.JCALibrary;
import gov.aps.jca.configuration.Configuration;
import gov.aps.jca.configuration.DefaultConfiguration;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.jlab.btm.business.util.CALoadException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Starts {@link PVMonitorManager} against a {@link SyntheticIoc} on loopback, with the test
 * standing in for the application server's hold on EJB calls during startup and for DNS. No EPICS
 * service outside the test JVM is contacted, and no host name is looked up.
 */
class PVMonitorManagerTest {
  private static final Logger MANAGER_LOGGER = Logger.getLogger(PVMonitorManager.class.getName());
  private static final List<String> PVS = PVMonitorManager.PV_LIST;
  private static final Duration WAIT = Duration.ofSeconds(30);

  /**
   * The one host of the manager's address list; {@link TestManager} decides what it resolves to.
   */
  private static final String HOST = "ioc.test";

  /** The address the synthetic IOC is at. */
  private static final String IOC_ADDRESS = "127.0.0.1";

  /** An address with no IOC: from the range reserved for documentation, and never contacted. */
  private static final String OTHER_ADDRESS = "192.0.2.1";

  private static final String DISABLE_REPEATER = "CA_DISABLE_REPEATER";
  private static String previousDisableRepeater;

  private int serverPort;
  private int repeaterPort;
  private int deadPort;
  private SyntheticIoc ioc;
  private TestManager manager;

  private final List<String> problems = new CopyOnWriteArrayList<>();

  private final Handler problemCollector =
      new Handler() {
        @Override
        public void publish(LogRecord record) {
          if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
            problems.add(MessageFormat.format(record.getMessage(), record.getParameters()));
          }
        }

        @Override
        public void flush() {}

        @Override
        public void close() {}
      };

  /**
   * As in {@link PVMonitorSessionTest#disableRepeater()}: the client would spawn a repeater JVM.
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
  void setUp() throws IOException, CAException {
    serverPort = SyntheticIoc.freePort();
    repeaterPort = SyntheticIoc.freePort();
    deadPort = SyntheticIoc.freePort();

    ioc = new SyntheticIoc(serverPort, SyntheticIoc.freePort());
    manager = new TestManager();
    manager.cache = new PVCache();

    MANAGER_LOGGER.addHandler(problemCollector);
  }

  @AfterEach
  void cleanUp() {
    MANAGER_LOGGER.removeHandler(problemCollector);

    manager.stop();
    ioc.close();
  }

  /**
   * WildFly holds calls to the application's EJBs from other threads until its startup singletons
   * have finished starting. Values delivered through such a call can't arrive while {@code start()}
   * waits for them, so it would wait out its timeout and report every PV as missing.
   */
  @Test
  void startCachesEveryValueWhileCacheCallsFromOtherThreadsAreHeld() {
    HeldCache cache = new HeldCache();
    manager.cache = cache;
    serve(PVS);

    try {
      manager.start();

      assertEquals(List.of(), problems, "problems logged by start()");
      assertEquals(Set.copyOf(PVS), cache.getMap().keySet());
    } finally {
      cache.release();
    }
  }

  /**
   * Channel Access resolves the address list once, when a session's context starts. A host that
   * doesn't resolve then is never looked up again by that context, so its PVs would stay
   * unavailable until a reset.
   */
  @Test
  void restartsMonitoringOnceAnUnresolvedHostResolves() throws Exception {
    serve(PVS);
    manager.addresses = Set.of();
    manager.startupTimeout = Duration.ofSeconds(1);

    manager.start();

    assertEquals(2, problems.size(), problems.toString());
    assertTrue(problems.get(0).startsWith("Unable to resolve"), problems.get(0));
    assertTrue(problems.get(0).contains("[" + HOST + "]"), problems.get(0));
    assertTrue(
        problems.get(1).startsWith("No value yet from " + PVS.size() + " of " + PVS.size()),
        problems.get(1));

    manager.checkAddressList();

    assertEquals(1, manager.opened, "sessions opened while the host is still unresolved");
    assertEquals(Map.of(), manager.cache.getMap());

    manager.addresses = Set.of(IOC_ADDRESS);
    manager.startupTimeout = PVMonitorManager.STARTUP_TIMEOUT;

    manager.checkAddressList();

    assertEquals(2, manager.opened, "sessions opened once the host resolves");
    assertEquals(Set.copyOf(PVS), manager.cache.getMap().keySet());
    await(() -> notifierThreads() == 1, () -> notifierThreads() + " sessions' threads are running");

    manager.checkAddressList();

    assertEquals(2, manager.opened, "sessions opened once every PV is connected");
  }

  @Test
  void restartsMonitoringWhenAHostsAddressChanges() {
    serve(PVS);
    manager.addresses = Set.of(OTHER_ADDRESS);
    manager.startupTimeout = Duration.ofSeconds(1);

    manager.start();
    manager.checkAddressList();

    assertEquals(1, manager.opened, "sessions opened while the address is unchanged");

    manager.addresses = Set.of(IOC_ADDRESS);
    manager.startupTimeout = PVMonitorManager.STARTUP_TIMEOUT;

    manager.checkAddressList();

    assertEquals(2, manager.opened, "sessions opened once the address changed");
    assertEquals(Set.copyOf(PVS), manager.cache.getMap().keySet());
  }

  /** A context that has an address is better off than a new one that couldn't resolve any. */
  @Test
  void keepsTheSessionWhenAHostStopsResolving() throws Exception {
    List<String> served = PVS.subList(1, PVS.size());
    serve(served);
    manager.startupTimeout = Duration.ofSeconds(1);

    manager.start();

    manager.addresses = Set.of();
    manager.checkAddressList();

    assertEquals(1, manager.opened, "sessions opened");
    await(
        () -> manager.cache.getMap().keySet().equals(Set.copyOf(served)),
        () -> "cached " + manager.cache.getMap().keySet());
  }

  @Test
  void keepsTheSessionWhileEveryPvIsConnected() {
    serve(PVS);

    manager.start();

    assertEquals(List.of(), problems, "problems logged by start()");

    manager.addresses = Set.of(OTHER_ADDRESS);
    manager.checkAddressList();

    assertEquals(1, manager.opened, "sessions opened");
  }

  @Test
  void addressListHostsAreItsEntriesWithoutPortsOrRepeats() {
    assertEquals(
        List.of("a.test", "10.0.0.1", "b.test"),
        List.copyOf(PVMonitorManager.addressListHosts(" a.test:5064 10.0.0.1  b.test:x a.test ")));
  }

  private void serve(List<String> pvs) {
    for (String pv : pvs) {
      ioc.add(pv, 1.0);
    }
  }

  /** Live threads that deliver a session's values: one per open session. */
  private static long notifierThreads() {
    return Thread.getAllStackTraces().keySet().stream()
        .filter(thread -> thread.isAlive() && thread.getName().equals("btm-pv-notify"))
        .count();
  }

  private static void await(BooleanSupplier condition, Supplier<String> message)
      throws InterruptedException {
    long deadline = System.nanoTime() + WAIT.toNanos();

    while (!condition.getAsBoolean()) {
      if (System.nanoTime() > deadline) {
        fail("Timed out after " + WAIT + ": " + message.get());
      }

      Thread.sleep(50);
    }
  }

  /**
   * A manager whose address list names {@link #HOST}, with the test standing in for DNS: the host
   * resolves to {@link #addresses}, and a session opened while those include {@link #IOC_ADDRESS}
   * reaches the synthetic IOC, while one opened otherwise searches a port where nothing listens.
   */
  private final class TestManager extends PVMonitorManager {
    volatile Set<String> addresses = Set.of(IOC_ADDRESS);
    volatile Duration startupTimeout = STARTUP_TIMEOUT;

    /** How many sessions have been opened. */
    int opened = 0;

    @Override
    String addressList() {
      return HOST + ":" + serverPort;
    }

    @Override
    Set<String> resolve(String host) {
      assertEquals(HOST, host);
      return addresses;
    }

    @Override
    Duration startupTimeout() {
      return startupTimeout;
    }

    @Override
    Configuration createConfiguration() {
      opened++;

      DefaultConfiguration config = new DefaultConfiguration("test-monitor-context");
      config.setAttribute("class", JCALibrary.CHANNEL_ACCESS_JAVA);
      config.setAttribute("addr_list", "127.0.0.1");
      config.setAttribute("auto_addr_list", "false");
      config.setAttribute(
          "server_port", Integer.toString(addresses.contains(IOC_ADDRESS) ? serverPort : deadPort));
      config.setAttribute("repeater_port", Integer.toString(repeaterPort));
      config.setAttribute("max_search_interval", "0.5");
      return config;
    }
  }

  /**
   * The cache as the application server presents it during startup: a call from any thread but the
   * one starting the application waits until startup has finished.
   */
  private static final class HeldCache extends PVCache {
    private final Thread startupThread = Thread.currentThread();
    private final CountDownLatch started = new CountDownLatch(1);

    void release() {
      started.countDown();
    }

    private void awaitStartup() {
      if (Thread.currentThread() == startupThread) {
        return;
      }

      try {
        started.await();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("Interrupted while held for startup", e);
      }
    }

    @Override
    public PVCacheEntry getOrThrow(String name) throws CALoadException {
      awaitStartup();
      return super.getOrThrow(name);
    }

    @Override
    public PVCacheEntry get(String name) {
      awaitStartup();
      return super.get(name);
    }

    @Override
    public BiConsumer<String, PVCacheEntry> writer() {
      awaitStartup();
      return super.writer();
    }

    @Override
    public Map<String, PVCacheEntry> getMap() {
      awaitStartup();
      return super.getMap();
    }
  }
}
