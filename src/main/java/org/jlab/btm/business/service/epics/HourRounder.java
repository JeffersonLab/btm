package org.jlab.btm.business.service.epics;

import java.util.List;
import org.jlab.btm.persistence.entity.CcAccHour;
import org.jlab.btm.persistence.entity.CcHallHour;
import org.jlab.btm.persistence.entity.CcMultiplicityHour;
import org.jlab.btm.persistence.entity.ExpHour;
import org.jlab.btm.persistence.projection.Hour;

/**
 * Responsible for rounding mutually exclusive EPICS time accounting statuses such that they sum to
 * exactly one hour.
 *
 * <p>Rounding is limited by a threshold of one minute.
 *
 * @author ryans
 */
public class HourRounder {

  /** The default rounding threshold is one minute. */
  public static final int ROUND_THRESHOLD = 60;

  public static final int SECONDS_PER_HOUR = 3600;

  public static final int SECONDS_PER_HUNDRETH_OF_HOUR = 36;

  /**
   * Round a list of experimenter hall hour time accounting.
   *
   * @param hours the list of hours.
   */
  public void roundExpHourList(List<ExpHour> hours) {
    for (ExpHour hour : hours) {
      roundExpHour(hour);
    }
  }

  /**
   * Round an experimenter hall hour time accounting (both experimenter and accelerator statuses) to
   * a whole hour.
   *
   * <p>The status 'off' is a shared status of both experimenter and accelerator statuses so
   * rounding to correct one set of statuses could affect the other set. Therefore, 'off' is
   * truncated to range, then UED/OFF fix applied, and finally during rounding of each mutually
   * exclusive set OFF is then static.
   *
   * @param hour the experimenter hall hour.
   */
  public void roundExpHour(ExpHour hour) {
    // Make sure shared status Off is within range 0 - 3600
    short[] statuses = new short[1];
    statuses[0] = hour.getOffSeconds();
    truncateToRange(statuses);
    hour.setOffSeconds(statuses[0]);

    // Note: we only adjust for extremes;
    // We only fix scenario when entire hour is erroneously one of these metric OR roughly matches
    // amount of OFF;
    // We don't subtract difference and keep some off and some of these other metrics;
    // There is an order/precedence of cleanup: UED, ER, PCC;
    // Someone should Fix EPICS IOC measure logic!

    // UED
    adjustOffAndExperimentMetric(
        hour,
        new ExperimentHourMetric() {
          @Override
          short getSeconds() {
            return hour.getUedSeconds();
          }

          @Override
          void setSeconds(short seconds) {
            hour.setUedSeconds(seconds);
          }
        });
    // ER
    adjustOffAndExperimentMetric(
        hour,
        new ExperimentHourMetric() {
          @Override
          short getSeconds() {
            return hour.getErSeconds();
          }

          @Override
          void setSeconds(short seconds) {
            hour.setErSeconds(seconds);
          }
        });
    // PCC
    adjustOffAndExperimentMetric(
        hour,
        new ExperimentHourMetric() {
          @Override
          short getSeconds() {
            return hour.getPccSeconds();
          }

          @Override
          void setSeconds(short seconds) {
            hour.setPccSeconds(seconds);
          }
        });

    roundAcceleratorSet(hour);
    roundExperimenterSet(hour);
  }

  abstract class ExperimentHourMetric {

    abstract short getSeconds();

    abstract void setSeconds(short seconds);
  }

  /**
   * The experimenter automated time accounting needs to be corrected for fact that (1) it doesn't
   * actually include a metric for OFF and (2) when really OFF automated accounting often sets ER,
   * PCC, or UED. To count OFF, the Crew Chief measure of Hall Off is used, but needs to REPLACE ER,
   * PCC, or UED so this method does that.
   *
   * @param hour The ExpHour
   */
  private void adjustOffAndExperimentMetric(ExpHour hour, ExperimentHourMetric metric) {
    int SLOP = 60;
    short metSeconds = metric.getSeconds();
    short off = hour.getOffSeconds();

    if (off != 0) {
      int difference = Math.abs(off - metSeconds);

      // If metric and OFF are within 1 minute of matching, then set metric to zero
      if (difference <= SLOP) {
        metric.setSeconds((short) 0);

        // If OFF is within 1 minute of filling entire hour, then set to entire hour
        if ((SECONDS_PER_HOUR - off) < SLOP) {
          hour.setOffSeconds((short) SECONDS_PER_HOUR);
        }
      }
    }
  }

  /**
   * Rounds only the accelerator mutual exclusive set of statuses for an hour.
   *
   * @param hour the experimenter hall hour.
   */
  public void roundAcceleratorSet(ExpHour hour) {
    // Won't modify off since it is a shared status
    short[] statuses = new short[4];
    statuses[0] = hour.getAbuSeconds();
    statuses[1] = hour.getBanuSeconds();
    statuses[2] = hour.getBnaSeconds();
    statuses[3] = hour.getAccSeconds();

    roundMutuallyExclusiveWithOff(statuses, hour.getOffSeconds());

    hour.setAbuSeconds(statuses[0]);
    hour.setBanuSeconds(statuses[1]);
    hour.setBnaSeconds(statuses[2]);
    hour.setAccSeconds(statuses[3]);
  }

  /**
   * Rounds only the experimenter mutual exclusive set of statuses for an hour.
   *
   * @param hour the experimenter hall hour.
   */
  public void roundExperimenterSet(ExpHour hour) {
    // Won't modify off since it is a shared status
    short[] statuses = new short[3];
    statuses[0] = hour.getErSeconds();
    statuses[1] = hour.getPccSeconds();
    statuses[2] = hour.getUedSeconds();

    roundMutuallyExclusiveWithOff(statuses, hour.getOffSeconds());

    hour.setErSeconds(statuses[0]);
    hour.setPccSeconds(statuses[1]);
    hour.setUedSeconds(statuses[2]);
  }

  /**
   * Round a list of accelerator hour time accounting.
   *
   * @param hours the list of hours.
   */
  public void roundAcceleratorHourList(List<CcAccHour> hours) {
    for (CcAccHour hour : hours) {
      roundAcceleratorHour(hour);
    }
  }

  /**
   * Round a list of experimenter hall hour time accounting.
   *
   * @param hours the list of hours.
   */
  public void roundHallHourList(List<CcHallHour> hours) {
    for (CcHallHour hour : hours) {
      roundHallHour(hour);
    }
  }

  public void roundMultiplicityHourList(
      List<CcMultiplicityHour> multiHours, List<List<CcHallHour>> hallHoursList) {
    for (int i = 0; i < multiHours.size(); i++) {
      CcMultiplicityHour multiHour = multiHours.get(i);
      CcHallHour hallAHour = hallHoursList.get(0).get(i);
      CcHallHour hallBHour = hallHoursList.get(1).get(i);
      CcHallHour hallCHour = hallHoursList.get(2).get(i);
      CcHallHour hallDHour = hallHoursList.get(3).get(i);
      short maxUp =
          (short)
              Math.max(
                  Math.max(hallAHour.getUpSeconds(), hallBHour.getUpSeconds()),
                  Math.max(hallCHour.getUpSeconds(), hallDHour.getUpSeconds()));
      roundMultiplicityHour(multiHour, maxUp);
    }
  }

  /**
   * Add up status seconds.
   *
   * @param statuses the time accounting seconds.
   * @return the total seconds.
   */
  protected int sum(short[] statuses) {
    int sum = 0;

    for (short s : statuses) {
      sum = sum + s;
    }

    return sum;
  }

  /**
   * Truncates time accounting statuses which are outside of the range 0 - 3600 seconds.
   *
   * @param statuses the time accounting statuses in seconds. The array is modified in-place.
   */
  protected void truncateToRange(short[] statuses) {
    // Force statuses within range 0 - 3600
    for (int i = 0; i < statuses.length; i++) {
      if (statuses[i] < 0) {
        statuses[i] = 0;
      } else if (statuses[i] > SECONDS_PER_HOUR) {
        statuses[i] = SECONDS_PER_HOUR;
      }
    }
  }

  /**
   * Distributes left-over seconds into or remove extra seconds from a mutually exclusive set of
   * time accounting statuses (buckets).
   *
   * <p>The algorithm for distributing extra seconds evenly is to loop over each status 'bucket' and
   * if the 'bucket' is not exactly full (3600 seconds) or empty (0 seconds) then add/subtract one
   * second. Notice that in this algorithm the order of the statuses in the array is very important
   * as statuses with a lower index will be modified more often.
   *
   * @param statuses the time accounting statuses in seconds. The array is modified in-place.
   * @param amount the left-over seconds to distribute. A negative number indicates too many seconds
   *     and subtraction should be used.
   */
  protected void distributeEvenly(short[] statuses, int amount) {
    int sign = (amount < 0) ? -1 : 1;

    // Break out of loop when amount is no longer being distributed (prevent infinite loop).
    // This occurs when amount is too much / statuses don't have enough room
    int skipped = 0;

    // Add amount to statuses relatively evenly not to push status outside of range 0 - 3600
    for (int i = 0; amount != 0; i = (i + 1) % statuses.length) {
      if (statuses[i] != 0
          && statuses[i] != SECONDS_PER_HOUR) { // Don't add/substract time to full or empty status
        statuses[i] = (short) (statuses[i] + (sign));
        amount = amount - (sign);
        skipped = 0;
      } else {
        if (skipped > statuses.length) {
          break;
        }

        skipped++;
      }
    }
  }

  /**
   * Truncates status seconds in a mutually exclusive set to fit in the allowable range (0 - 3600
   * seconds), then distributes left-overs / removes extras evenly as long as the difference is not
   * more than the threshold. The status off is shared between experimenter and accelerator sets so
   * must be specified independently.
   *
   * @param statuses The time accounting statuses in seconds.
   * @param off The amount of seconds of shared status 'off'.
   */
  protected void roundMutuallyExclusiveWithOff(short[] statuses, short off) {
    truncateToRange(statuses);

    int total = sum(statuses) + off;
    int difference = Hour.SECONDS_PER_HOUR - total;

    if (difference != 0 && Math.abs(difference) <= ROUND_THRESHOLD) {
      distributeEvenly(statuses, difference);
    }
  }

  protected void roundMutuallyExclusiveSet(short[] statuses) {
    truncateToRange(statuses);

    int total = sum(statuses);
    int difference = SECONDS_PER_HOUR - total;

    if (difference != 0 && Math.abs(difference) <= ROUND_THRESHOLD) {
      distributeEvenly(statuses, difference);
    }
  }

  protected void roundToNearest100thOfAnHour(short[] statuses) {
    int remainder = 0;
    for (int i = 0; i < statuses.length; i++) {
      remainder = remainder + (statuses[i] % SECONDS_PER_HUNDRETH_OF_HOUR);
      statuses[i] =
          (short) ((statuses[i] / SECONDS_PER_HUNDRETH_OF_HOUR) * SECONDS_PER_HUNDRETH_OF_HOUR);
    }

    /*System.out.println("Remainder: " + remainder);*/

    for (int i = 0; i < (remainder / SECONDS_PER_HUNDRETH_OF_HOUR); i++) {
      /*System.out.println("Looking for somewhere to put 36 seconds...");*/
      for (int j = 0; j < statuses.length; j++) {
        if (statuses[j] != 0 && statuses[j] < SECONDS_PER_HOUR) {
          /*System.out.println("Adding 36 to: " + statuses[j]);*/
          statuses[j] = (short) (statuses[j] + SECONDS_PER_HUNDRETH_OF_HOUR);
          break;
        }
      }
    }
  }

  /**
   * The "UP" (PHYSICS) measurement is actually using "ANY UP", which fails to account for hall
   * down, tune, and BNR. If the hour doesn't add up and there is at least some PHYSICS then put
   * unaccounted for time in PHYSICS.
   *
   * @param statuses
   */
  protected void adjustForBadPhysicsMeasurement(short[] statuses) {
    int total = sum(statuses);
    int difference = SECONDS_PER_HOUR - total;

    if (statuses[0] > 0 && difference > 0) {
      statuses[0] = (short) (statuses[0] + difference);
    }

    if (statuses[0] > SECONDS_PER_HOUR) {
      statuses[0] = SECONDS_PER_HOUR;
    }
  }

  protected short adjustLeadingValueWithinThresholdMaxOneHour(
      short leadingValue, short followingValue) {
    /*System.out.println("leading: " + leadingValue + ", following: " + followingValue);*/

    int difference = Math.abs(leadingValue - followingValue);
    if (leadingValue < followingValue
        && difference <= (ROUND_THRESHOLD * 5)) { // THRESHOLD IS 5 MINUTES
      leadingValue = (short) (leadingValue + difference);
      if (leadingValue > SECONDS_PER_HOUR) {
        leadingValue = SECONDS_PER_HOUR;
      }
    }

    /*System.out.println("adjusted leading: " + leadingValue);*/

    return leadingValue;
  }

  /*protected short roundToNearestMinute(short value) {
      short newValue;

      int remander = value % 60;
      newValue = (short) ((value / 60) * 60);
      if (remander >= 30) {
          newValue = (short) (newValue + 60);
      }

      if (value > SECONDS_PER_HOUR) {
          newValue = SECONDS_PER_HOUR;
      } else if (value < 0) {
          newValue = 0;
      }

      return newValue;
  }*/

  protected short truncateToHourAndToNearest100thOfAnHour(short value) {
    short newValue = value;

    if (value > SECONDS_PER_HOUR) {
      newValue = SECONDS_PER_HOUR;
    } else if (value < 0) {
      newValue = 0;
    }

    newValue = (short) ((newValue / SECONDS_PER_HUNDRETH_OF_HOUR) * SECONDS_PER_HUNDRETH_OF_HOUR);

    return newValue;
  }

  /**
   * Rounds only the accelerator mutual exclusive set of statuses for an hour.
   *
   * @param hour the experimenter hall hour.
   */
  public void roundAcceleratorHour(CcAccHour hour) {
    // Won't modify off since it is a shared status
    short[] statuses = new short[6];
    statuses[0] = hour.getUpSeconds();
    statuses[1] = hour.getStudiesSeconds();
    statuses[2] = hour.getRestoreSeconds();
    statuses[3] = hour.getAccSeconds();
    statuses[4] = hour.getDownSeconds();
    statuses[5] = hour.getSadSeconds();

    /* Address fact that hours from EPICS don't add up out-of-the-box */
    roundMutuallyExclusiveSet(statuses);

    /* Address fact that Crew Chief works in decimal hours and prefers to keep times within 100ths of an hour*/
    roundToNearest100thOfAnHour(statuses);

    /* Address fact that PHYSICS (UP) is not measured correctly */
    adjustForBadPhysicsMeasurement(statuses);

    hour.setUpSeconds(statuses[0]);
    hour.setStudiesSeconds(statuses[1]);
    hour.setRestoreSeconds(statuses[2]);
    hour.setAccSeconds(statuses[3]);
    hour.setDownSeconds(statuses[4]);
    hour.setSadSeconds(statuses[5]);
  }

  /**
   * Rounds only the experimenter mutual exclusive set of statuses for an hour.
   *
   * @param hour the experimenter hall hour.
   */
  public void roundHallHour(CcHallHour hour) {
    // Won't modify off since it is a shared status
    short[] statuses = new short[5];
    statuses[0] = hour.getUpSeconds();
    statuses[1] = hour.getTuneSeconds();
    statuses[2] = hour.getBnrSeconds();
    statuses[3] = hour.getDownSeconds();
    statuses[4] = hour.getOffSeconds();

    /* Address fact that hours from EPICS don't add up out-of-the-box */
    roundMutuallyExclusiveSet(statuses);

    /* Address fact that Crew Chief works in decimal hours and prefers to keep times within 100ths of an hour*/
    roundToNearest100thOfAnHour(statuses);

    hour.setUpSeconds(statuses[0]);
    hour.setTuneSeconds(statuses[1]);
    hour.setBnrSeconds(statuses[2]);
    hour.setDownSeconds(statuses[3]);
    hour.setOffSeconds(statuses[4]);
  }

  /**
   * We need to make sure the values don't independently exceed an hour.
   *
   * @param hour
   */
  private void roundMultiplicityHour(CcMultiplicityHour hour, short maxUp) {
    /*System.out.println("maxUp: " + maxUp);*/

    hour.setOneHallUpSeconds(truncateToHourAndToNearest100thOfAnHour(hour.getOneHallUpSeconds()));
    hour.setTwoHallUpSeconds(truncateToHourAndToNearest100thOfAnHour(hour.getTwoHallUpSeconds()));
    hour.setThreeHallUpSeconds(
        truncateToHourAndToNearest100thOfAnHour(hour.getThreeHallUpSeconds()));
    hour.setFourHallUpSeconds(truncateToHourAndToNearest100thOfAnHour(hour.getFourHallUpSeconds()));
    hour.setAnyHallUpSeconds(
        adjustLeadingValueWithinThresholdMaxOneHour(
            truncateToHourAndToNearest100thOfAnHour(hour.getAnyHallUpSeconds()), maxUp));
    hour.setAllHallUpSeconds(
        adjustLeadingValueWithinThresholdMaxOneHour(
            truncateToHourAndToNearest100thOfAnHour(hour.getAllHallUpSeconds()), maxUp));
    hour.setDownHardSeconds(truncateToHourAndToNearest100thOfAnHour(hour.getDownHardSeconds()));
  }
}
