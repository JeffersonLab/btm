package org.jlab.btm.persistence.projection;

/**
 * @author ryans
 */
public class DowntimeSummaryTotals {
  private long eventSeconds;

  public DowntimeSummaryTotals(Number eventSeconds) {
    this.eventSeconds = eventSeconds.longValue();
  }

  public long getEventSeconds() {
    return eventSeconds;
  }

  public void setEventSeconds(long eventSeconds) {
    this.eventSeconds = eventSeconds;
  }
}
