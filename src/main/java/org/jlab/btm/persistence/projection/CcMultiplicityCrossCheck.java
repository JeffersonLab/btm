package org.jlab.btm.persistence.projection;

import org.jlab.btm.persistence.enumeration.DurationUnits;
import org.jlab.btm.presentation.util.BtmFunctions;

/**
 * @author ryans
 */
public class CcMultiplicityCrossCheck {

    private static final int TEN_MINUTES_OF_SECONDS = 600;
    private final String[] halls = new String[]{"A", "B", "C", "D"};
    private final String[] highUpMessage = new String[4];

    private final boolean passed;
    private final boolean[] hallPassed = new boolean[4];
    private final boolean[] highUp = new boolean[4];

    public CcMultiplicityCrossCheck(CcHallShiftTotals opA, CcHallShiftTotals opB,
                                    CcHallShiftTotals opC, CcHallShiftTotals opD, CcMultiplicityShiftTotals multi) {

        CcHallShiftTotals[] opTimes = new CcHallShiftTotals[]{opA, opB, opC, opD};

        for (int i = 0; i < 4; i++) {
            highUpMessage[i] = "Operations reports significantly more UP ("
                    + BtmFunctions.formatDuration(opTimes[i].getUpSeconds(), DurationUnits.HOURS)
                    + " hours) for Hall " + halls[i] + " than ANY UP (" + BtmFunctions.formatDuration(
                    multi.getAnyHallUpSeconds(), DurationUnits.HOURS)
                    //                            + opTimes[i].getTuneSeconds(), DurationUnits.HOURS)
                    + " hours)";
        }

        highUp[0] = opA.getUpSeconds() <= multi.getAnyHallUpSeconds() + TEN_MINUTES_OF_SECONDS;
        highUp[1] = opB.getUpSeconds() <= multi.getAnyHallUpSeconds() + TEN_MINUTES_OF_SECONDS;
        highUp[2] = opC.getUpSeconds() <= multi.getAnyHallUpSeconds() + TEN_MINUTES_OF_SECONDS;
        highUp[3] = opD.getUpSeconds() <= multi.getAnyHallUpSeconds() + TEN_MINUTES_OF_SECONDS;

        hallPassed[0] = true;
        hallPassed[1] = true;
        hallPassed[2] = true;
        hallPassed[3] = true;

        for (int i = 0; i < 4; i++) {
            if (!highUp[i]) {
                hallPassed[i] = false;
            }
        }

        passed = hallPassed[0] && hallPassed[1] && hallPassed[2] && hallPassed[3];
    }

    public boolean isPassed() {
        return passed;
    }

    public boolean[] getHallPassed() {
        return hallPassed;
    }

    public boolean[] getHighUp() {
        return highUp;
    }

    public String[] getHighUpMessage() {
        return highUpMessage;
    }
}
