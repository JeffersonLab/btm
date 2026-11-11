package org.jlab.btm.business.service.epics;

import gov.aps.jca.CAException;
import gov.aps.jca.JCALibrary;
import gov.aps.jca.configuration.Configuration;
import gov.aps.jca.configuration.DefaultConfiguration;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.DependsOn;
import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.btm.persistence.epics.Constant;
import org.jlab.smoothness.persistence.enumeration.Hall;

@Singleton
@Startup
@DependsOn("PVCache")
public class PVMonitorManager {
  private static final Logger LOGGER = Logger.getLogger(PVMonitorManager.class.getName());

  @EJB PVCache cache;

  private PVMonitorSession session;

  /**
   * What each host of the address list resolved to just before the session was opened; no addresses
   * for a host that didn't resolve. Channel Access resolves the list once, when the session's
   * context starts, and keeps those addresses for as long as the context lives.
   */
  private Map<String, Set<String>> resolved = Map.of();

  public static final List<String> PV_LIST;

  static {
    final List<String> fixed =
        List.of(
            Constant.CREW_CHIEF_CHANNEL_NAME,
            Constant.OPERATORS_CHANNEL_NAME,
            Constant.PROGRAM_CHANNEL_NAME,
            Constant.PROGRAM_DEPUTY_CHANNEL_NAME,
            Constant.COMMENTS_CHANNEL_NAME,
            Constant.TIME_CHANNEL_NAME,
            Constant.ACC_UP_CHANNEL_NAME,
            Constant.ACC_SAD_CHANNEL_NAME,
            Constant.ACC_DOWN_CHANNEL_NAME,
            Constant.ACC_STUDIES_CHANNEL_NAME,
            Constant.ACC_RESTORE_CHANNEL_NAME,
            Constant.ACC_ACC_CHANNEL_NAME,
            Constant.MULTI_ONE_UP,
            Constant.MULTI_TWO_UP,
            Constant.MULTI_THREE_UP,
            Constant.MULTI_FOUR_UP,
            Constant.MULTI_ANY_UP,
            Constant.MULTI_ALL_UP,
            Constant.MULTI_DOWN);

    final List<String> dynamic = new ArrayList<>();

    for (Hall hall : Hall.values()) {
      dynamic.add(Constant.HALL_PREFIX + hall + Constant.HALL_UP_SUFFIX);
      dynamic.add(Constant.HALL_PREFIX + hall + Constant.HALL_TUNE_SUFFIX);
      dynamic.add(Constant.HALL_PREFIX + hall + Constant.HALL_BNR_SUFFIX);
      dynamic.add(Constant.HALL_PREFIX + hall + Constant.HALL_DOWN_SUFFIX);
      dynamic.add(Constant.HALL_PREFIX + hall + Constant.HALL_OFF_SUFFIX);

      dynamic.add(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_TIME_SUFFIX);
      dynamic.add(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_ABU_SUFFIX);
      dynamic.add(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_BANU_SUFFIX);
      dynamic.add(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_BNA_SUFFIX);
      dynamic.add(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_ER_SUFFIX);
      dynamic.add(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_PCC_SUFFIX);
      dynamic.add(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_UED_SUFFIX);
      dynamic.add(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_OFF_SUFFIX);

      if ("true".equals(System.getenv("CA_IOC_LIVENESS"))) {
        dynamic.add(
            Constant.EXP_HALL_LIVENESS_PREFIX
                + hall.toString().toLowerCase()
                + Constant.EXP_HALL_LIVENESS_SUFFIX);
      }
    }

    dynamic.addAll(fixed);

    PV_LIST = Collections.unmodifiableList(dynamic);
  }

  /** How long startup waits for the PVs' first values before carrying on without them. */
  static final Duration STARTUP_TIMEOUT = Duration.ofSeconds(10);

  /** How often the address list's hosts are looked up again while PVs aren't connected. */
  static final int ADDRESS_CHECK_SECONDS = 30;

  @PostConstruct
  public void start() {
    try {
      // Looked up before the session's context looks them up. If a host starts resolving in
      // between, the next check restarts monitoring once more than needed; in the other order a
      // host that stops resolving in between would never be seen to change
      resolved = resolveAddressList();

      Set<String> unresolved = new TreeSet<>(resolved.keySet());
      unresolved.removeIf(host -> !resolved.get(host).isEmpty());

      if (!unresolved.isEmpty()) {
        LOGGER.log(
            Level.WARNING,
            "Unable to resolve Channel Access address list hosts {0}; their PVs stay unavailable"
                + " until they resolve, which is checked every {1} seconds",
            new Object[] {unresolved, ADDRESS_CHECK_SECONDS});
      }

      // The writer, not cache::put: the server holds EJB calls from the session's thread until
      // this method returns, so the values awaited below would never arrive in time
      session = PVMonitorSession.open(createConfiguration(), PV_LIST, cache.writer());

      Duration timeout = startupTimeout();
      Set<String> missing = session.awaitInitialValues(timeout);

      if (!missing.isEmpty()) {
        LOGGER.log(
            Level.WARNING,
            "No value yet from {0} of {1} PVs after {2} seconds; monitoring them once they connect:"
                + " {3}",
            new Object[] {missing.size(), PV_LIST.size(), timeout.toSeconds(), missing});
      }
    } catch (CAException e) { // @PostConstruct isn't supposed to throw checked exceptions...
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      LOGGER.log(Level.WARNING, "Interrupted while waiting for initial PV values", e);
    }
  }

  @PreDestroy
  public void stop() {
    if (session != null) {
      session.close();
      session = null;
    }
  }

  /**
   * Restarts monitoring when a host of the address list resolves to an address it didn't have when
   * the session was opened: a host that didn't resolve then, or one whose address has changed. The
   * session's context would go on searching at the addresses it started with, so PVs served from
   * that host would stay unavailable until a reset.
   *
   * <p>Does nothing while every PV is connected, or while such a host doesn't resolve: the context
   * then still has the best addresses there are.
   */
  @Schedule(hour = "*", minute = "*", second = "*/" + ADDRESS_CHECK_SECONDS, persistent = false)
  void checkAddressList() {
    if (session == null || session.connectedCount() == PV_LIST.size()) {
      return;
    }

    Map<String, Set<String>> now = resolveAddressList();
    Map<String, String> changes = changedHosts(resolved, now);

    if (changes.isEmpty()) {
      return;
    }

    LOGGER.log(
        Level.INFO,
        "Restarting EPICS monitoring with {0} of {1} PVs connected: Channel Access address list"
            + " hosts resolve differently than when it started: {2}",
        new Object[] {session.connectedCount(), PV_LIST.size(), changes});

    stop();
    start();
  }

  /**
   * @param opened What each host resolved to when the session was opened
   * @param now What each host resolves to now
   * @return For each host that resolves now, and to something else than then, a description of the
   *     change; sorted by host
   */
  static Map<String, String> changedHosts(
      Map<String, Set<String>> opened, Map<String, Set<String>> now) {
    Map<String, String> changes = new TreeMap<>();

    now.forEach(
        (host, addresses) -> {
          Set<String> before = opened.getOrDefault(host, Set.of());

          if (!addresses.isEmpty() && !addresses.equals(before)) {
            changes.put(host, (before.isEmpty() ? "unresolved" : before) + " -> " + addresses);
          }
        });

    return changes;
  }

  private Map<String, Set<String>> resolveAddressList() {
    Map<String, Set<String>> addresses = new TreeMap<>();

    for (String host : addressListHosts(addressList())) {
      addresses.put(host, resolve(host));
    }

    return addresses;
  }

  /**
   * @param addrList A Channel Access address list: hosts or addresses separated by spaces, each
   *     with an optional {@code :port}
   * @return The hosts and addresses without their ports, in order
   */
  static Set<String> addressListHosts(String addrList) {
    Set<String> hosts = new LinkedHashSet<>();
    StringTokenizer entries = new StringTokenizer(addrList);

    while (entries.hasMoreTokens()) {
      String entry = entries.nextToken();
      int colon = entry.indexOf(':');
      String host = colon < 0 ? entry : entry.substring(0, colon);

      if (!host.isEmpty()) {
        hosts.add(host);
      }
    }

    return hosts;
  }

  /**
   * Package-private so a test can stand in for DNS.
   *
   * @param host A host name or address
   * @return Every address it resolves to, sorted; empty if it doesn't resolve
   */
  Set<String> resolve(String host) {
    Set<String> addresses = new TreeSet<>();

    try {
      for (InetAddress address : InetAddress.getAllByName(host)) {
        addresses.add(address.getHostAddress());
      }
    } catch (UnknownHostException e) {
      // Reported by the caller as a host with no addresses
    }

    return addresses;
  }

  /** Package-private so a test needn't wait this long for PVs it knows are unavailable. */
  Duration startupTimeout() {
    return STARTUP_TIMEOUT;
  }

  /**
   * Package-private so a test can name its own hosts.
   *
   * @return The Channel Access address list
   */
  String addressList() {
    String addrList = System.getenv("BTM_EPICS_ADDR_LIST");

    if (addrList == null || addrList.isBlank()) {
      throw new IllegalArgumentException("BTM_EPICS_ADDR_LIST must not be empty");
    }

    return addrList;
  }

  /** Package-private so a test can point the session at a synthetic IOC. */
  Configuration createConfiguration() {
    DefaultConfiguration config = new DefaultConfiguration("monitor-context");

    config.setAttribute("addr_list", addressList());
    config.setAttribute("auto_addr_list", "false");
    config.setAttribute("class", JCALibrary.CHANNEL_ACCESS_JAVA);

    return config;
  }

  @RolesAllowed({"cc", "btm-admin"})
  public void reset() {
    stop();
    start();
  }
}
