package org.jlab.btm.persistence.projection;

import org.jlab.btm.persistence.enumeration.DurationUnits;
import org.jlab.btm.presentation.util.BtmFunctions;

/**
 * @author ryans
 */
public class CcAcceleratorCrossCheck {

    private static final int TEN_MINUTES_OF_SECONDS = 600;
    private final String[] halls = new String[]{"A", "B", "C", "D"};
    private final boolean passed;
    private final boolean[] hallPassed = new boolean[4];
    private final boolean[] highAbu = new boolean[4];
    private final boolean[] highBanu = new boolean[4];
    private final boolean[] highBna = new boolean[4];
    private final boolean[] highAcc = new boolean[4];

    private final String[] highAbuMessage = new String[4];
    private final String[] highBanuMessage = new String[4];
    private final String[] highBnaMessage = new String[4];
    private final String[] highAccMessage = new String[4];

    public CcAcceleratorCrossCheck(CcAccShiftTotals acc, ExpHallShiftTotals a,
                                   ExpHallShiftTotals b, ExpHallShiftTotals c, ExpHallShiftTotals d) {

        ExpHallShiftTotals[] times = new ExpHallShiftTotals[]{a, b, c, d};

        for (int i = 0; i < 4; i++) {
            highAbuMessage[i] = "Experimenter Hall " + halls[i] + " reports significantly more ABU (" + BtmFunctions.formatDuration(times[i].getAbuSeconds(), DurationUnits.HOURS) + " hours) than the Operations reported PHYSICS + STUDIES + RESTORE (" + BtmFunctions.formatDuration(acc.getUpSeconds() + acc.getRestoreSeconds() + acc.getStudiesSeconds(), DurationUnits.HOURS) + " hours)";
            highBanuMessage[i] = "Experimenter Hall " + halls[i] + " reports significantly more BANU (" + BtmFunctions.formatDuration(times[i].getBanuSeconds(), DurationUnits.HOURS) + " hours) than the Operations reported PHYSICS (" + BtmFunctions.formatDuration(acc.getUpSeconds(), DurationUnits.HOURS) + " hours)";
            highBnaMessage[i] = "Experimenter Hall " + halls[i] + " reports significantly more BNA (" + BtmFunctions.formatDuration(times[i].getBnaSeconds(), DurationUnits.HOURS) + " hours) than the Operations reported PHYSICS (" + BtmFunctions.formatDuration(acc.getUpSeconds(), DurationUnits.HOURS) + " hours)";
            highAccMessage[i] = "Experimenter Hall " + halls[i] + " reports significantly more ACC (" + BtmFunctions.formatDuration(times[i].getAccSeconds(), DurationUnits.HOURS) + " hours) than the Operations reported ACC (" + BtmFunctions.formatDuration(acc.getAccSeconds(), DurationUnits.HOURS) + " hours)";
        }

        int accSeconds = acc.getUpSeconds() + acc.getRestoreSeconds()
                + acc.getStudiesSeconds();

        highAbu[0] = a.getAbuSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;
        highAbu[1] = b.getAbuSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;
        highAbu[2] = c.getAbuSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;
        highAbu[3] = d.getAbuSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;

        accSeconds = acc.getUpSeconds();
        highBanu[0] = a.getBanuSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;
        highBanu[1] = b.getBanuSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;
        highBanu[2] = c.getBanuSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;
        highBanu[3] = d.getBanuSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;

        accSeconds = acc.getUpSeconds();
        highBna[0] = a.getBnaSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;
        highBna[1] = b.getBnaSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;
        highBna[2] = c.getBnaSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;
        highBna[3] = d.getBnaSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;

        accSeconds = acc.getAccSeconds();
        highAcc[0] = a.getAccSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;
        highAcc[1] = b.getAccSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;
        highAcc[2] = c.getAccSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;
        highAcc[3] = d.getAccSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;

        hallPassed[0] = true;
        hallPassed[1] = true;
        hallPassed[2] = true;
        hallPassed[3] = true;

        for (int i = 0; i < 4; i++) {
            if (!highAbu[i]) {
                hallPassed[i] = false;
            }
            if (!highBanu[i]) {
                hallPassed[i] = false;
            }
            if (!highBna[i]) {
                hallPassed[i] = false;
            }
            if (!highAcc[i]) {
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

    public boolean[] getHighAbu() {
        return highAbu;
    }

    public boolean[] getHighBanu() {
        return highBanu;
    }

    public boolean[] getHighBna() {
        return highBna;
    }

    public boolean[] getHighAcc() {
        return highAcc;
    }

    public String[] getHighAbuMessage() {
        return highAbuMessage;
    }

    public String[] getHighBanuMessage() {
        return highBanuMessage;
    }

    public String[] getHighBnaMessage() {
        return highBnaMessage;
    }

    public String[] getHighAccMessage() {
        return highAccMessage;
    }
}
