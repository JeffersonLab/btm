package org.jlab.btm.persistence.projection;

import org.jlab.btm.persistence.enumeration.DurationUnits;
import org.jlab.btm.presentation.util.BtmFunctions;

/**
 * @author ryans
 */
public class CcDowntimeCrossCheck {

  private static final int TEN_MINUTES_OF_SECONDS = 600;

  private final boolean passed;
  private final boolean lowProgramPassed;
  private final boolean highTuningPassed;

  private final String lowProgramMessage;
  private final String highTuningMessage;

  public CcDowntimeCrossCheck(CcAccShiftTotals acc, long dtmEventDownSeconds, long tuningSeconds) {

    int possibleDowntimeSeconds = acc.calculatePossibleDowntimeSeconds();
    int upSeconds = acc.getUpSeconds();

    lowProgramMessage =
        "DTM blocked event down ("
            + BtmFunctions.formatDuration((int) dtmEventDownSeconds, DurationUnits.HOURS)
            + " hours) is significantly greater than BTM possible down time [PHYSICS + INTERNAL DOWN] ("
            + BtmFunctions.formatDuration(possibleDowntimeSeconds, DurationUnits.HOURS)
            + " hours)";

    highTuningMessage =
        "DTM tuning event down ("
            + BtmFunctions.formatDuration((int) tuningSeconds, DurationUnits.HOURS)
            + " hours) is significantly greater than BTM possible Tuning [PHYSICS] ("
            + BtmFunctions.formatDuration(upSeconds, DurationUnits.HOURS)
            + " hours)";

    lowProgramPassed = possibleDowntimeSeconds >= dtmEventDownSeconds - TEN_MINUTES_OF_SECONDS;

    highTuningPassed = upSeconds >= tuningSeconds - TEN_MINUTES_OF_SECONDS;

    passed = lowProgramPassed && highTuningPassed;
  }

  public boolean isPassed() {
    return passed;
  }

  public boolean isLowProgramPassed() {
    return lowProgramPassed;
  }

  public boolean isHighTuningPassed() {
    return highTuningPassed;
  }

  public String getHighTuningMessage() {
    return highTuningMessage;
  }

  public String getLowProgramMessage() {
    return lowProgramMessage;
  }
}
