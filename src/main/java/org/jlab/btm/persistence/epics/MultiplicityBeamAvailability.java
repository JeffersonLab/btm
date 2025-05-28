package org.jlab.btm.persistence.epics;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jlab.btm.business.util.CALoadException;
import org.jlab.btm.persistence.entity.CcMultiplicityHour;
import org.jlab.smoothness.business.util.TimeUtil;

/**
 * Represents Accelerator Beam Availability for one week as recorded by an EPICS IOC.
 *
 * @author ryans
 */
public class MultiplicityBeamAvailability {

  private double[] time = null;
  private double[] oneUp = null;
  private double[] twoUp = null;
  private double[] threeUp = null;
  private double[] fourUp = null;
  private double[] anyUp = null;
  private double[] allUp = null;
  private double[] downHard = null;

  public double[] getTime() {
    return time;
  }

  public void setTime(double[] time) {
    this.time = time;
  }

  public double[] getOneUp() {
    return oneUp;
  }

  public void setOneUp(double[] oneUp) {
    this.oneUp = oneUp;
  }

  public double[] getTwoUp() {
    return twoUp;
  }

  public void setTwoUp(double[] twoUp) {
    this.twoUp = twoUp;
  }

  public double[] getThreeUp() {
    return threeUp;
  }

  public void setThreeUp(double[] threeUp) {
    this.threeUp = threeUp;
  }

  public double[] getFourUp() {
    return fourUp;
  }

  public void setFourUp(double[] fourUp) {
    this.fourUp = fourUp;
  }

  public double[] getAnyUp() {
    return anyUp;
  }

  public void setAnyUp(double[] anyUp) {
    this.anyUp = anyUp;
  }

  public double[] getAllUp() {
    return allUp;
  }

  public void setAllUp(double[] allUp) {
    this.allUp = allUp;
  }

  public double[] getDownHard() {
    return downHard;
  }

  public void setDownHard(double[] downHard) {
    this.downHard = downHard;
  }

  /**
   * Determine if the data is valid.
   *
   * <p>This method checks to ensure that each status array is not null and that number of values in
   * each array is equal to the number of hours in the EPICS history window.
   *
   * @return true if the data is valid.
   */
  public boolean isValidValue() {
    if (time == null
        || oneUp == null
        || twoUp == null
        || fourUp == null
        || threeUp == null
        || anyUp == null
        || allUp == null
        || downHard == null) {
      return false;
    }

    return time.length == Constant.NUMBER_OF_HOURS_IN_HISTORY
        && oneUp.length == Constant.NUMBER_OF_HOURS_IN_HISTORY
        && twoUp.length == Constant.NUMBER_OF_HOURS_IN_HISTORY
        && fourUp.length == Constant.NUMBER_OF_HOURS_IN_HISTORY
        && threeUp.length == Constant.NUMBER_OF_HOURS_IN_HISTORY
        && anyUp.length == Constant.NUMBER_OF_HOURS_IN_HISTORY
        && allUp.length == Constant.NUMBER_OF_HOURS_IN_HISTORY
        && downHard.length == Constant.NUMBER_OF_HOURS_IN_HISTORY;
  }

  /**
   * Convert the data into a list of OpAccHours.
   *
   * @return the list of experimenter hall hours.
   */
  public List<CcMultiplicityHour> getOpMultiplicityHours() throws CALoadException {
    List<CcMultiplicityHour> hours = new ArrayList<>();

    if (!isValidValue()) {
      throw new CALoadException("EPICS channel access values are uninitialized or invalid");
    }

    for (int i = 0; i < time.length; i++) {
      CcMultiplicityHour hour = new CcMultiplicityHour();

      // EPICS uses UNIX timestamps
      Date hourOfDay = TimeUtil.convertUNIXTimestampToDate((long) time[i]);

      // The timestamps are not exactly on the hour!
      hourOfDay = TimeUtil.roundToNearestHour(hourOfDay);

      // The timestamps represent the end of the hour, NOT the start!
      hourOfDay = TimeUtil.addHours(hourOfDay, -1);

      hour.setDayAndHour(hourOfDay);
      hour.setOneHallUpSeconds((short) oneUp[i]);
      hour.setTwoHallUpSeconds((short) twoUp[i]);
      hour.setThreeHallUpSeconds((short) threeUp[i]);
      hour.setFourHallUpSeconds((short) fourUp[i]);
      hour.setAnyHallUpSeconds((short) anyUp[i]);
      hour.setAllHallUpSeconds((short) allUp[i]);
      hour.setDownHardSeconds((short) downHard[i]);

      hours.add(hour);
    }

    return hours;
  }
}
