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

    private final String lowProgramMessage;

    public CcDowntimeCrossCheck(CcAccShiftTotals acc, long dtmEventDownSeconds) {

        int possibleDowntimeSeconds = acc.calculatePossibleDowntimeSeconds();

        lowProgramMessage = "DTM event down (" + BtmFunctions.formatDuration((int) dtmEventDownSeconds, DurationUnits.HOURS) + " hours) is significantly greater than BTM possible down time [PHYSICS + INTERNAL DOWN] (" + BtmFunctions.formatDuration(possibleDowntimeSeconds, DurationUnits.HOURS) + " hours)";

        lowProgramPassed = possibleDowntimeSeconds >= dtmEventDownSeconds - TEN_MINUTES_OF_SECONDS;

        passed = lowProgramPassed;
    }

    public boolean isPassed() {
        return passed;
    }

    public String getLowProgramMessage() {
        return lowProgramMessage;
    }
}
