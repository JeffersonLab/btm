package org.jlab.btm.persistence.projection;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * A Sum of PAC schedule accelerator program days.
 *
 * <p>In practice only PHYSICS, RESTORE, and OFF (plus implied OFF) are really used by PAC as
 * Studies and ACC is often hand-wavy allowance (perhaps covered by double schedule slop factor).
 *
 * <p>Day counts are whole scheduled days. Hours are elapsed hours, like the period: each scheduled
 * day adds the length of its local calendar day, which is 23 or 25 hours on a daylight saving time
 * change, so a fully scheduled range has no implicit OFF.
 */
public class PacAccSum {
  private int programDays = 0; // Anything but OFF or implied OFF
  private int restoreDays = 0;
  private int physicsDays = 0;
  private int studiesDays = 0;
  private int accDays = 0;
  private int offDays = 0;

  private double programHours = 0;
  private double restoreHours = 0;
  private double physicsHours = 0;
  private double studiesHours = 0;
  private double accHours = 0;
  private double offHours = 0;

  private Date start;
  private Date end;
  private double periodHours;
  private double implicitOffHours;
  private double totalOffHours;

  /**
   * Add a scheduled day.
   *
   * @param day A time on the scheduled day, normally its local midnight
   * @param accProgram The day's accelerator program; OFF counts as explicit OFF and anything else
   *     as program
   */
  public void addDay(Date day, String accProgram) {
    double hours = hoursInDay(day);

    if ("OFF".equals(accProgram)) {
      offDays++;
      offHours += hours;
      return;
    }

    programDays++;
    programHours += hours;

    if (accProgram == null) {
      return;
    }

    switch (accProgram) {
      case "PHYSICS":
        physicsDays++;
        physicsHours += hours;
        break;
      case "RESTORE":
        restoreDays++;
        restoreHours += hours;
        break;
      case "STUDIES":
        studiesDays++;
        studiesHours += hours;
        break;
      case "ACC":
        accDays++;
        accHours += hours;
        break;
    }
  }

  /**
   * Length in hours of the local calendar day that contains the given time.
   *
   * @param day A time on the day
   * @return 24, or 23 or 25 on a daylight saving time change
   */
  static double hoursInDay(Date day) {
    ZoneId zone = ZoneId.systemDefault();
    ZonedDateTime startOfDay = day.toInstant().atZone(zone).toLocalDate().atStartOfDay(zone);
    return Duration.between(startOfDay, startOfDay.plusDays(1)).toSeconds() / 3600.0;
  }

  public int getProgramDays() {
    return programDays;
  }

  public int getRestoreDays() {
    return restoreDays;
  }

  public int getPhysicsDays() {
    return physicsDays;
  }

  public int getStudiesDays() {
    return studiesDays;
  }

  public int getAccDays() {
    return accDays;
  }

  public int getOffDays() {
    return offDays;
  }

  public double getProgramHours() {
    return programHours;
  }

  public double getRestoreHours() {
    return restoreHours;
  }

  public double getPhysicsHours() {
    return physicsHours;
  }

  public double getStudiesHours() {
    return studiesHours;
  }

  public double getAccHours() {
    return accHours;
  }

  /**
   * Explicit OFF hours: the scheduled OFF days' hours.
   *
   * @return The hours
   */
  public double getOffHours() {
    return offHours;
  }

  public void setRangeAndCalculateOff(Date start, Date end) {
    this.start = start;
    this.end = end;

    this.periodHours = (end.getTime() - start.getTime()) / 1000.0 / 60 / 60;

    this.implicitOffHours = this.periodHours - this.programHours - this.offHours;
    this.totalOffHours = this.offHours + this.implicitOffHours;
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
