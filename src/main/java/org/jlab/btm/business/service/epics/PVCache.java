package org.jlab.btm.business.service.epics;

import static javax.ejb.ConcurrencyManagementType.BEAN;

import gov.aps.jca.dbr.DBR;
import java.util.concurrent.ConcurrentHashMap;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.Singleton;
import javax.ejb.Startup;

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
  private final ConcurrentHashMap<String, DBR> map = new ConcurrentHashMap<>();

  public DBR get(String name) {
    return map.get(name);
  }

  public void put(String name, DBR value) {
    map.put(name, value);
  }
}
