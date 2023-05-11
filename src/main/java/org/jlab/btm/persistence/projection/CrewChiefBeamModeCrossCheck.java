package org.jlab.btm.persistence.projection;

import org.jlab.btm.persistence.enumeration.DurationUnits;
import org.jlab.btm.presentation.util.BtmFunctions;

/**
 * @author ryans
 */
public class CrewChiefBeamModeCrossCheck {

    private static final int TEN_MINUTES_OF_SECONDS = 600;
    private final String[] halls = new String[]{"A", "B", "C", "D"};
    private final boolean passed;
    private final boolean[] hallPassed = new boolean[4];
    private final boolean[] highHallPhysics = new boolean[4];

    private final String[] highHallPhysicsMessage = new String[4];

    public CrewChiefBeamModeCrossCheck(OpAccShiftTotals acc, OpHallShiftTotals a,
                                       OpHallShiftTotals b, OpHallShiftTotals c, OpHallShiftTotals d) {

        OpHallShiftTotals[] times = new OpHallShiftTotals[]{a, b, c, d};

        for (int i = 0; i < 4; i++) {
            highHallPhysicsMessage[i] = "Operations Hall " + halls[i] + " reports significantly more UP + TUNE + BNR + PHYSICS DOWN (" + BtmFunctions.formatDuration(times[i].getUpSeconds() + times[i].getTuneSeconds() + times[i].getBnrSeconds() + times[i].getDownSeconds(), DurationUnits.HOURS) + " hours) than the Operations Accelerator reported PHYSICS (" + BtmFunctions.formatDuration(acc.getUpSeconds(), DurationUnits.HOURS) + " hours)";
        }

        int accSeconds = acc.getUpSeconds();

        highHallPhysics[0] = a.getUpSeconds() + a.getTuneSeconds() + a.getBnrSeconds() + a.getDownSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;
        highHallPhysics[1] = b.getUpSeconds() + b.getTuneSeconds() + b.getBnrSeconds() + b.getDownSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;
        highHallPhysics[2] = c.getUpSeconds() + c.getTuneSeconds() + c.getBnrSeconds() + c.getDownSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;
        highHallPhysics[3] = d.getUpSeconds() + d.getTuneSeconds() + d.getBnrSeconds() + d.getDownSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;

        hallPassed[0] = true;
        hallPassed[1] = true;
        hallPassed[2] = true;
        hallPassed[3] = true;

        for (int i = 0; i < 4; i++) {
            if (!highHallPhysics[i]) {
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

    public boolean[] getHighHallPhysics() {
        return highHallPhysics;
    }

    public String[] getHighHallPhysicsMessage() {
        return highHallPhysicsMessage;
    }
}
