package org.jlab.btm.business.service.epics;

import static javax.ejb.ConcurrencyManagementType.BEAN;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.jlab.btm.business.util.CALoadException;

/**
 * PV Cache.
 *
 * <p>Multiple threads can call this method concurrently due to ConcurrencyManagementType.BEAN. But
 * the get/put methods delegate to a ConcurrentHashMap, so that is where our concurrency management
 * is handled.
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

  public void put(String name, PVCacheEntry value) {
    map.put(name, value);
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
