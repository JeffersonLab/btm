package org.jlab.btm.business.service.epics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import gov.aps.jca.JCALibrary;
import gov.aps.jca.configuration.Configuration;
import gov.aps.jca.configuration.DefaultConfiguration;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
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
 * Starts {@link PVMonitorManager} against a {@link SyntheticIoc} serving every PV it monitors, with
 * a cache that holds calls back as the application server does while the application starts. No
 * EPICS service outside the test JVM is contacted.
 */
class PVMonitorManagerTest {
  private static final Logger MANAGER_LOGGER = Logger.getLogger(PVMonitorManager.class.getName());
  private static final String DISABLE_REPEATER = "CA_DISABLE_REPEATER";
  private static String previousDisableRepeater;

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
  void collectProblems() {
    MANAGER_LOGGER.addHandler(problemCollector);
  }

  @AfterEach
  void stopCollectingProblems() {
    MANAGER_LOGGER.removeHandler(problemCollector);
  }

  /**
   * WildFly holds calls to the application's EJBs from other threads until its startup singletons
   * have finished starting. Values delivered through such a call can't arrive while {@code start()}
   * waits for them, so it would wait out its timeout and report every PV as missing.
   */
  @Test
  void startCachesEveryValueWhileCacheCallsFromOtherThreadsAreHeld() throws Exception {
    int serverPort = SyntheticIoc.freePort();
    int beaconPort = SyntheticIoc.freePort();
    int repeaterPort = SyntheticIoc.freePort();

    HeldCache cache = new HeldCache();

    PVMonitorManager manager =
        new PVMonitorManager() {
          @Override
          Configuration createConfiguration() {
            return clientConfiguration(serverPort, repeaterPort);
          }
        };

    manager.cache = cache;

    try (SyntheticIoc ioc = new SyntheticIoc(serverPort, beaconPort)) {
      for (String pv : PVMonitorManager.PV_LIST) {
        ioc.add(pv, 1.0);
      }

      try {
        manager.start();

        assertEquals(List.of(), problems, "problems logged by start()");
        assertEquals(Set.copyOf(PVMonitorManager.PV_LIST), cache.getMap().keySet());
      } finally {
        cache.release();
        manager.stop();
      }
    }
  }

  private static Configuration clientConfiguration(int serverPort, int repeaterPort) {
    DefaultConfiguration config = new DefaultConfiguration("test-monitor-context");
    config.setAttribute("class", JCALibrary.CHANNEL_ACCESS_JAVA);
    config.setAttribute("addr_list", "127.0.0.1");
    config.setAttribute("auto_addr_list", "false");
    config.setAttribute("server_port", Integer.toString(serverPort));
    config.setAttribute("repeater_port", Integer.toString(repeaterPort));
    config.setAttribute("max_search_interval", "0.5");
    return config;
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
