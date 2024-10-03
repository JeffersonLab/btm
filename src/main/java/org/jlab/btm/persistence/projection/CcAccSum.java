package org.jlab.btm.persistence.projection;

import java.util.Date;

/**
 * Sum of Crew Chief Accelerator Programs.
 *
 * @author ryans
 */
public class CcAccSum {

  private final Date start;
  private final Date end;

  private final long possibleDownSeconds; // Physics and Internal Down
  private final long programSeconds; // anything but OFF (SAD) or implied OFF
  private final long upSeconds;
  private final long sadSeconds;
  private final long downSeconds;
  private final long studiesSeconds;
  private final long restoreSeconds;
  private final long accSeconds;

  private final double periodHours;

  private final double implicitOffHours;

  private final double totalOffHours;

  public CcAccSum(
      Date start,
      Date end,
      Number upSeconds,
      Number sadSeconds,
      Number downSeconds,
      Number studiesSeconds,
      Number restoreSeconds,
      Number accSeconds) {
    this.start = start;
    this.end = end;
    this.upSeconds = upSeconds.longValue();
    this.sadSeconds = sadSeconds.longValue();
    this.downSeconds = downSeconds.longValue();
    this.studiesSeconds = studiesSeconds.longValue();
    this.restoreSeconds = restoreSeconds.longValue();
    this.accSeconds = accSeconds.longValue();

    this.programSeconds =
        this.getUpSeconds()
            + this.getStudiesSeconds()
            + this.getRestoreSeconds()
            + this.getAccSeconds()
            + this.getDownSeconds();

    this.possibleDownSeconds = this.getUpSeconds() + this.getDownSeconds();

    this.periodHours = (end.getTime() - start.getTime()) / 1000.0 / 60 / 60;

    this.implicitOffHours = this.periodHours - ((this.programSeconds + this.sadSeconds) / 3600.0);
    this.totalOffHours = (this.sadSeconds / 3600.0) + this.implicitOffHours;
  }

  public long getUpSeconds() {
    return upSeconds;
  }

  public long getSadSeconds() {
    return sadSeconds;
  }

  public long getDownSeconds() {
    return downSeconds;
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

  public long getPossibleDownSeconds() {
    return possibleDownSeconds;
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
