package org.jlab.btm.business.service.epics;

import static jakarta.ejb.ConcurrencyManagementType.BEAN;

import jakarta.ejb.ConcurrencyManagement;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import org.jlab.btm.business.util.CALoadException;

/**
 * PV Cache.
 *
 * <p>Multiple threads can call these methods concurrently due to ConcurrencyManagementType.BEAN.
 * But the readers and the writer delegate to a ConcurrentHashMap, so that is where our concurrency
 * management is handled.
 */
@Startup
@Singleton
@ConcurrencyManagement(BEAN)
public class PVCache {
  private final ConcurrentHashMap<String, PVCacheEntry> map = new ConcurrentHashMap<>();

  /**
   * Get a value from the cache or throw a CALoadException if not found.
   *
   * @param name The key
   * @return The value
   * @throws CALoadException If not found
   */
  public PVCacheEntry getOrThrow(String name) throws CALoadException {
    PVCacheEntry entry = map.get(name);

    if (entry == null) {
      throw new CALoadException(name);
    }

    return entry;
  }

  /**
   * Get a value from the cache and returns null if not found.
   *
   * @param name The key
   * @return The value, or null
   */
  public PVCacheEntry get(String name) {
    return map.get(name);
  }

  /**
   * Returns a writer that stores entries in the cache directly, not through a call to this EJB.
   *
   * <p>The monitor session writes from its own thread, starting while the application is still
   * starting. The application server holds EJB calls from other threads until startup has finished,
   * so values written through one couldn't arrive while {@link PVMonitorManager} waits for them.
   *
   * @return The writer, taking a PV name and its entry
   */
  public BiConsumer<String, PVCacheEntry> writer() {
    return map::put;
  }

  /**
   * Returns a new Map with the same entries as those underlying the cache. This new Map can then be
   * iterated or accessed with the full Map API. Since both String keys and PVCacheEntry value
   * objects are immutable, it's safe to expose the entries. The new map keys are sorted naturally
   * (alphabetical).
   *
   * @return A new map
   */
  public Map<String, PVCacheEntry> getMap() {
    // Quickly make a copy without cost of sort.
    HashMap<String, PVCacheEntry> copy = new HashMap<>(map);

    // Now we'll sort separately (insertion order).  At this point, ConcurrentHashMap isn't being
    // touched.
    LinkedHashMap<String, PVCacheEntry> sorted = new LinkedHashMap<>();

    SortedSet<String> keys = new TreeSet<>(copy.keySet());

    for (String key : keys) {
      sorted.put(key, copy.get(key));
    }

    return sorted;
  }
}
