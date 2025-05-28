package org.jlab.btm.business.service.epics;

import gov.aps.jca.CAStatus;
import gov.aps.jca.dbr.DBR;
import java.util.Date;

/**
 * An immutable record that captures an EPICS CA Monitor update. Unlike the CAJ provided
 * MonitorEvent, this class has a timestamp to capture when the monitor event was triggered locally.
 */
public final class PVCacheEntry {
  /** The monitor update status. */
  private final CAStatus status;

  /** The EPICS CA database record. */
  private final DBR dbr;

  /**
   * This is the local timestamp of when the monitor update was received. This differs from the
   * timestamp provided by IOCs in DBR_TIME type structs (which is also interesting, but often
   * unreliable). We use the legacy Date object because JSTL format expects a Date.
   */
  private final Date ts;

  /**
   * Create a new PVCacheEntry.
   *
   * @param status The CAStatus
   * @param dbr The DBR
   * @param ts The timestamp
   */
  public PVCacheEntry(CAStatus status, DBR dbr, Date ts) {
    this.status = status;
    this.dbr = dbr;
    this.ts = ts;
  }

  /**
   * Get the CAStatus.
   *
   * @return The status
   */
  public CAStatus getStatus() {
    return status;
  }

  /**
   * Get the DBR
   *
   * @return The database record
   */
  public DBR getDbr() {
    return dbr;
  }

  /**
   * Get the timestamp.
   *
   * @return The timestamp
   */
  public Date getTs() {
    return ts;
  }
}
