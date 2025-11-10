package org.jlab.btm.persistence.projection;

import java.util.Date;
import org.jlab.btm.persistence.entity.CcAccHour;
import org.jlab.btm.persistence.enumeration.DurationUnits;
import org.jlab.btm.presentation.util.BtmFunctions;

/**
 * @author ryans
 */
public class DowntimeHourCrossCheck {

  private static final int HALF_HOUR_OF_SECONDS = 1800;
  private static final int TEN_MINUTES_OF_SECONDS = 600;

  private final boolean passed;
  private final boolean lowProgramPassed;
  private final boolean highTuningPassed;

  private final String lowProgramMessage;
  private final String highTuningMessage;

  private final Date dayAndHour;

  public DowntimeHourCrossCheck(Date dayAndHour, CcAccHour accHour, DtmHour dtmHour) {
    this.dayAndHour = dayAndHour;

    int possibleDowntimeSeconds = accHour.getUpSeconds() + accHour.getDownSeconds();
    int upSeconds = accHour.getUpSeconds();

    int blockedSeconds = dtmHour.getBlockedSeconds();
    int tuneSeconds = dtmHour.getTuneSeconds();

    lowProgramMessage =
        "DTM blocked event down ("
            + BtmFunctions.formatDuration((int) blockedSeconds, DurationUnits.HOURS)
            + " hours) is significantly greater than BTM possible down time [PHYSICS + INTERNAL DOWN] ("
            + BtmFunctions.formatDuration(possibleDowntimeSeconds, DurationUnits.HOURS)
            + " hours)";

    highTuningMessage =
        "DTM tuning event down ("
            + BtmFunctions.formatDuration((int) tuneSeconds, DurationUnits.HOURS)
            + " hours) is significantly greater than BTM possible Tuning [PHYSICS] ("
            + BtmFunctions.formatDuration(upSeconds, DurationUnits.HOURS)
            + " hours)";

    lowProgramPassed = possibleDowntimeSeconds >= blockedSeconds - TEN_MINUTES_OF_SECONDS;

    highTuningPassed = upSeconds >= tuneSeconds - TEN_MINUTES_OF_SECONDS;

    passed = lowProgramPassed && highTuningPassed;
  }

  public Date getDayAndHour() {
    return dayAndHour;
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
