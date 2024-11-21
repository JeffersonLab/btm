package org.jlab.btm.persistence.projection;

import java.util.Date;

/**
 * Sum of PD Accelerator Programs.
 *
 * @author ryans
 */
public class PdAccSum {

  private final long programSeconds; // anything but OFF (SAM) or implied OFF
  private final long physicsSeconds;
  private final long offSeconds;
  private final long studiesSeconds;
  private final long restoreSeconds;
  private final long accSeconds;

  private final Date start;
  private final Date end;

  private final double periodHours;
  private final double implicitOffHours;
  private final double totalOffHours;

  public PdAccSum(
      Date start,
      Date end,
      Number physicsSeconds,
      Number offSeconds,
      Number studiesSeconds,
      Number restoreSeconds,
      Number accSeconds) {
    this.start = start;
    this.end = end;
    this.physicsSeconds = physicsSeconds.longValue();
    this.offSeconds = offSeconds.longValue();
    this.studiesSeconds = studiesSeconds.longValue();
    this.restoreSeconds = restoreSeconds.longValue();
    this.accSeconds = accSeconds.longValue();

    this.programSeconds =
        this.getPhysicsSeconds()
            + this.getStudiesSeconds()
            + this.getRestoreSeconds()
            + this.getAccSeconds();

    this.periodHours = (end.getTime() - start.getTime()) / 1000.0 / 60 / 60;

    this.implicitOffHours = this.periodHours - ((this.programSeconds + this.offSeconds) / 3600.0);
    this.totalOffHours = (this.offSeconds / 3600.0) + this.implicitOffHours;
  }

  public long getPhysicsSeconds() {
    return physicsSeconds;
  }

  public long getOffSeconds() {
    return offSeconds;
  }

  public long getStudiesSeconds() {
    return studiesSeconds;
  }

  public long getRestoreSeconds() {
    return restoreSeconds;
  }

  public long getAccSeconds() {
    return accSeconds;
  }

  public long getProgramSeconds() {
    return programSeconds;
  }

  public Date getStart() {
    return start;
  }

  public Date getEnd() {
    return end;
  }

  public double getPeriodHours() {
    return periodHours;
  }

  public double getImplicitOffHours() {
    return implicitOffHours;
  }

  public double getTotalOffHours() {
    return totalOffHours;
  }
}
