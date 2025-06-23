package org.jlab.btm.persistence.projection;

import java.util.Date;

/**
 * A Sum of PAC schedule accelerator program days.
 *
 * <p>In practice only PHYSICS, RESTORE, and OFF (plus implied OFF) are really used by PAC as
 * Studies and ACC is often hand-wavy allowance (perhaps covered by double schedule slop factor).
 */
public class PacAccSum {
  public int programDays = 0; // Anything but OFF or implied OFF
  public int tuningDays = 0;
  public int restoreDays = 0;
  public int physicsDays = 0;
  public int studiesDays = 0;
  public int accDays = 0;
  public int offDays = 0;

  private Date start;
  private Date end;
  private double periodHours;
  private double implicitOffHours;
  private double totalOffHours;

  public int getProgramDays() {
    return programDays;
  }

  public void setProgramDays(int programDays) {
    this.programDays = programDays;
  }

  public int getRestoreDays() {
    return restoreDays;
  }

  public void setRestoreDays(int restoreDays) {
    this.restoreDays = restoreDays;
  }

  public int getPhysicsDays() {
    return physicsDays;
  }

  public int getTuningDays() {
    return tuningDays;
  }

  public void setTuningDays(int tuningDays) {
    this.tuningDays = tuningDays;
  }

  public void setPhysicsDays(int physicsDays) {
    this.physicsDays = physicsDays;
  }

  public int getStudiesDays() {
    return studiesDays;
  }

  public void setStudiesDays(int studiesDays) {
    this.studiesDays = studiesDays;
  }

  public int getAccDays() {
    return accDays;
  }

  public void setAccDays(int accDays) {
    this.accDays = accDays;
  }

  public int getOffDays() {
    return offDays;
  }

  public void setOffDays(int offDays) {
    this.offDays = offDays;
  }

  public void setRangeAndCalculateOff(Date start, Date end) {
    this.start = start;
    this.end = end;

    this.periodHours = (end.getTime() - start.getTime()) / 1000.0 / 60 / 60;

    this.implicitOffHours = this.periodHours - (this.programDays + this.offDays) * 24;
    this.totalOffHours = (this.offDays * 24) + this.implicitOffHours;
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
