package org.jlab.btm.persistence.epics;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jlab.btm.persistence.entity.ExpHour;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * Represents an Experimenter's BTA for one week as recorded by an EPICS IOC.
 *
 * @author ryans
 */
public class ExperimenterAccounting {

  private Hall hall = null;
  private double[] time = null;
  private double[] abu = null;
  private double[] banu = null;
  private double[] bna = null;
  private double[] acc = null;
  private double[] er = null;
  private double[] pcc = null;
  private double[] ued = null;
  private double[] off = null;

  public Hall getHall() {
    return hall;
  }

  public void setHall(Hall hall) {
    this.hall = hall;
  }

  public double[] getTime() {
    return time;
  }

  public void setTime(double[] time) {
    this.time = time;
  }

  public double[] getABU() {
    return abu;
  }

  public void setABU(double[] abu) {
    this.abu = abu;
  }

  public double[] getBANU() {
    return banu;
  }

  public void setBANU(double[] banu) {
    this.banu = banu;
  }

  public double[] getBNA() {
    return bna;
  }

  public void setBNA(double[] bna) {
    this.bna = bna;
  }

  public double[] getACC() {
    return acc;
  }

  public void setACC(double[] acc) {
    this.acc = acc;
  }

  public double[] getER() {
    return er;
  }

  public void setER(double[] er) {
    this.er = er;
  }

  public double[] getPCC() {
    return pcc;
  }

  public void setPCC(double[] pcc) {
    this.pcc = pcc;
  }

  public double[] getUED() {
    return ued;
  }

  public void setUED(double[] ued) {
    this.ued = ued;
  }

  public void setOFF(double[] off) {
    this.off = off;
  }

  public double[] getOFF() {
    return off;
  }

  /**
   * Determine if the data in this ExperimenterAccounting is valid.
   *
   * <p>This method checks to ensure that each status array is not null and that number of values in
   * each array is equal to the number of hours in the EPICS history window.
   *
   * @return true if the data is valid.
   */
  public boolean isValidValue() {
    if (time == null
        || abu == null
        || banu == null
        || bna == null
        || acc == null
        || er == null
        || pcc == null
        || ued == null
        || off == null) {
      return false;
    }

    return time.length == Constant.NUMBER_OF_HOURS_IN_HISTORY
        && abu.length == Constant.NUMBER_OF_HOURS_IN_HISTORY
        && banu.length == Constant.NUMBER_OF_HOURS_IN_HISTORY
        && bna.length == Constant.NUMBER_OF_HOURS_IN_HISTORY
        && acc.length == Constant.NUMBER_OF_HOURS_IN_HISTORY
        && er.length == Constant.NUMBER_OF_HOURS_IN_HISTORY
        && pcc.length == Constant.NUMBER_OF_HOURS_IN_HISTORY
        && ued.length == Constant.NUMBER_OF_HOURS_IN_HISTORY
        && off.length == Constant.NUMBER_OF_HOURS_IN_HISTORY;
  }

  /**
   * Convert the ExperimenterAccounting data into a list of experimenter hall hours.
   *
   * @return the list of experimenter hall hours.
   */
  public List<ExpHour> getExpHallHours() {
    List<ExpHour> hours = new ArrayList<ExpHour>();

    if (!isValidValue()) {
      throw new IllegalStateException("EPICS channel access values are uninitialized or invalid");
    }

    for (int i = 0; i < time.length; i++) {
      ExpHour hour = new ExpHour();

      // EPICS uses UNIX timestamps
      Date hourOfDay = TimeUtil.convertUNIXTimestampToDate((long) time[i]);

      // The timestamps are not exactly on the hour!
      hourOfDay = TimeUtil.roundToNearestHour(hourOfDay);

      // The timestamps represent the end of the hour, NOT the start!
      hourOfDay = TimeUtil.addHours(hourOfDay, -1);

      hour.setHall(hall);

      hour.setDayAndHour(hourOfDay);
      hour.setAbuSeconds((short) abu[i]);
      hour.setBanuSeconds((short) banu[i]);
      hour.setBnaSeconds((short) bna[i]);
      hour.setAccSeconds((short) acc[i]);
      hour.setErSeconds((short) er[i]);
      hour.setPccSeconds((short) pcc[i]);
      hour.setUedSeconds((short) ued[i]);

      // This is really part of CC PVS...
      hour.setOffSeconds((short) off[i]);

      hours.add(hour);
    }

    return hours;
  }
}
