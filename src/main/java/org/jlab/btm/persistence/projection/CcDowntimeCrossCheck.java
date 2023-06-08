package org.jlab.btm.persistence.projection;

import org.jlab.btm.persistence.enumeration.DurationUnits;
import org.jlab.btm.presentation.util.BtmFunctions;

/**
 * @author ryans
 */
public class CcDowntimeCrossCheck {


    private final boolean passed;
    private final boolean lowProgramPassed;
    private final boolean lowDownHardPassed;

    private final String lowProgramMessage;
    private final String lowDownHardMessage;

    public CcDowntimeCrossCheck(CcAccShiftTotals acc, long dtmEventDownSeconds) {

        int programSeconds = acc.getUpSeconds() + acc.getDownSeconds() + acc.getRestoreSeconds() + acc.getStudiesSeconds() + acc.getAccSeconds();

        lowProgramMessage = "DTM event down (" + BtmFunctions.formatDuration((int) dtmEventDownSeconds, DurationUnits.HOURS) + " hours) is greater than BTM program time [PHYSICS + STUDIES + RESTORE + ACC + DOWN] (" + BtmFunctions.formatDuration(programSeconds, DurationUnits.HOURS) + " hours)";
        lowDownHardMessage = "";

        lowProgramPassed = programSeconds >= dtmEventDownSeconds;
        lowDownHardPassed = true;

        passed = lowProgramPassed && lowDownHardPassed;
    }

    public boolean isPassed() {
        return passed;
    }

    public String getLowProgramMessage() {
        return lowProgramMessage;
    }
}
