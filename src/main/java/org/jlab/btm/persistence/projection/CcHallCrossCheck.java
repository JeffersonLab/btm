package org.jlab.btm.persistence.projection;

import org.jlab.btm.persistence.enumeration.DurationUnits;
import org.jlab.btm.presentation.util.BtmFunctions;

/**
 * @author ryans
 */
public class CcHallCrossCheck {

    private static final int HALF_HOUR_OF_SECONDS = 1800;
    private static final int TEN_MINUTES_OF_SECONDS = 600;

    private static final String[] halls = new String[]{"A", "B", "C", "D"};

    private final String[] highAbuMessage = new String[4];
    private final String[] lowAbuMessage = new String[4];
    private final String[] lowBanuMessage = new String[4];
    private final String[] highBnaMessage = new String[4];
    private final String[] highOffMessage = new String[4];
    private final String[] highAccMessage = new String[4];

    private final boolean passed;
    private final boolean[] hallPassed = new boolean[4];
    private final boolean[] highAbu = new boolean[4];
    private final boolean[] lowAbu = new boolean[4];
    private final boolean[] lowBanu = new boolean[4];
    private final boolean[] highBna = new boolean[4];
    private final boolean[] highOff = new boolean[4];
    private final boolean[] highAcc = new boolean[4];

    public CcHallCrossCheck(CcHallShiftTotals opA, CcHallShiftTotals opB,
                            CcHallShiftTotals opC, CcHallShiftTotals opD, ExpShiftTotals a,
                            ExpShiftTotals b, ExpShiftTotals c, ExpShiftTotals d) {

        CcHallShiftTotals[] opTimes = new CcHallShiftTotals[]{opA, opB, opC, opD};
        ExpShiftTotals[] times = new ExpShiftTotals[]{a, b, c, d};

        for (int i = 0; i < 4; i++) {
            highAbuMessage[i] = "Experimenter Hall " + halls[i]
                    + " reports significantly more ABU + BANU ("
                    + BtmFunctions.formatDuration(times[i].getAbuSeconds() + times[i].getBanuSeconds(),
                    DurationUnits.HOURS)
                    + " hours) than the Operations reported UP + TUNE + BNR ("
                    + BtmFunctions.formatDuration(opTimes[i].getUpSeconds()
                            + opTimes[i].getTuneSeconds() + opTimes[i].getBnrSeconds(),
                    DurationUnits.HOURS)
                    + " hours)";
            lowAbuMessage[i] = "Experimenter Hall " + halls[i]
                    + " reports significantly less ABU + BANU ("
                    + BtmFunctions.formatDuration(times[i].getAbuSeconds() + times[i].getBanuSeconds(),
                    DurationUnits.HOURS)
                    + " hours) than the Operations reported UP + TUNE + BNR ("
                    + BtmFunctions.formatDuration(opTimes[i].getUpSeconds()
                            + opTimes[i].getTuneSeconds() + opTimes[i].getBnrSeconds(),
                    DurationUnits.HOURS)
                    + " hours)";
            lowBanuMessage[i] = "Experimenter Hall " + halls[i] + " reports significantly less BANU ("
                    + BtmFunctions.formatDuration(times[i].getBanuSeconds(), DurationUnits.HOURS)
                    + " hours) vs the Operations reported BNR (" + BtmFunctions.formatDuration(
                    opTimes[i].getBnrSeconds(),
                    DurationUnits.HOURS) + " hours)";
            highBnaMessage[i] = "Experimenter Hall " + halls[i] + " reports significantly more BNA ("
                    + BtmFunctions.formatDuration(times[i].getBnaSeconds(), DurationUnits.HOURS)
                    + " hours) than the Operations reported UP + TUNE + DOWN ("
                    + BtmFunctions.formatDuration(opTimes[i].getUpSeconds()
                            + opTimes[i].getTuneSeconds() + opTimes[i].getDownSeconds(),
                    DurationUnits.HOURS)
                    + " hours)";
            highOffMessage[i] = "Experimenter Hall " + halls[i] + " reports significantly more OFF ("
                    + BtmFunctions.formatDuration(times[i].getOffSeconds(), DurationUnits.HOURS)
                    + " hours) than the Operations reported OFF (" + BtmFunctions.formatDuration(
                    opTimes[i].getOffSeconds(),
                    DurationUnits.HOURS) + " hours)";
            highAccMessage[i] = "Experimenter Hall " + halls[i] + " reports significantly more ACC ("
                    + BtmFunctions.formatDuration(times[i].getAccSeconds(), DurationUnits.HOURS)
                    + " hours) than the Operations reported OFF (" + BtmFunctions.formatDuration(
                    opTimes[i].getOffSeconds(),
                    DurationUnits.HOURS) + " hours)";
        }

        highAbu[0] = ((a.getAbuSeconds() + a.getBanuSeconds()) / 2 <= opA.getUpSeconds()
                + opA.getTuneSeconds() + opA.getBnrSeconds()) || (opA.getUpSeconds()
                <= HALF_HOUR_OF_SECONDS);
        highAbu[1] = ((b.getAbuSeconds() + b.getBanuSeconds()) / 2 <= opB.getUpSeconds()
                + opB.getTuneSeconds() + opB.getBnrSeconds()) || (opB.getUpSeconds()
                <= HALF_HOUR_OF_SECONDS);
        highAbu[2] = ((c.getAbuSeconds() + c.getBanuSeconds()) / 2 <= opC.getUpSeconds()
                + opC.getTuneSeconds() + opC.getBnrSeconds()) || (opC.getUpSeconds()
                <= HALF_HOUR_OF_SECONDS);
        highAbu[3] = ((d.getAbuSeconds() + d.getBanuSeconds()) / 2 <= opD.getUpSeconds()
                + opD.getTuneSeconds() + opD.getBnrSeconds()) || (opD.getUpSeconds()
                <= HALF_HOUR_OF_SECONDS);

        lowAbu[0] = (a.getAbuSeconds() + a.getBanuSeconds() >= (opA.getUpSeconds()
                + opA.getTuneSeconds() + opA.getBnrSeconds()) / 2) || (opA.getUpSeconds()
                <= HALF_HOUR_OF_SECONDS);
        lowAbu[1] = (b.getAbuSeconds() + b.getBanuSeconds() >= (opB.getUpSeconds()
                + opB.getTuneSeconds() + opB.getBnrSeconds()) / 2) || (opB.getUpSeconds()
                <= HALF_HOUR_OF_SECONDS);
        lowAbu[2] = (c.getAbuSeconds() + c.getBanuSeconds() >= (opC.getUpSeconds()
                + opC.getTuneSeconds() + opC.getBnrSeconds()) / 2)
                || (opC.getUpSeconds()
                <= HALF_HOUR_OF_SECONDS);
        lowAbu[3] = (d.getAbuSeconds() + d.getBanuSeconds() >= (opD.getUpSeconds()
                + opD.getTuneSeconds() + opD.getBnrSeconds()) / 2)
                || (opD.getUpSeconds()
                <= HALF_HOUR_OF_SECONDS);

        lowBanu[0] = a.getBanuSeconds() + TEN_MINUTES_OF_SECONDS >= opA.getBnrSeconds();
        lowBanu[1] = b.getBanuSeconds() + TEN_MINUTES_OF_SECONDS >= opB.getBnrSeconds();
        lowBanu[2] = c.getBanuSeconds() + TEN_MINUTES_OF_SECONDS >= opC.getBnrSeconds();
        lowBanu[3] = d.getBanuSeconds() + TEN_MINUTES_OF_SECONDS >= opD.getBnrSeconds();

        highBna[0] = a.getBnaSeconds() <= opA.getDownSeconds()
                + opA.getUpSeconds() + opA.getTuneSeconds() + TEN_MINUTES_OF_SECONDS;
        highBna[1] = b.getBnaSeconds() <= opB.getDownSeconds()
                + opB.getUpSeconds() + opB.getTuneSeconds() + TEN_MINUTES_OF_SECONDS;
        highBna[2] = c.getBnaSeconds() <= opC.getDownSeconds()
                + opC.getUpSeconds() + opC.getTuneSeconds() + TEN_MINUTES_OF_SECONDS;
        highBna[3] = d.getBnaSeconds() <= opD.getDownSeconds()
                + opD.getUpSeconds() + opD.getTuneSeconds() + TEN_MINUTES_OF_SECONDS;

        highOff[0] = a.getOffSeconds() <= opA.getOffSeconds() + TEN_MINUTES_OF_SECONDS;
        highOff[1] = b.getOffSeconds() <= opB.getOffSeconds() + TEN_MINUTES_OF_SECONDS;
        highOff[2] = c.getOffSeconds() <= opC.getOffSeconds() + TEN_MINUTES_OF_SECONDS;
        highOff[3] = d.getOffSeconds() <= opD.getOffSeconds() + TEN_MINUTES_OF_SECONDS;

        highAcc[0] = a.getAccSeconds() <= opA.getOffSeconds() + TEN_MINUTES_OF_SECONDS;
        highAcc[1] = b.getAccSeconds() <= opB.getOffSeconds() + TEN_MINUTES_OF_SECONDS;
        highAcc[2] = c.getAccSeconds() <= opC.getOffSeconds() + TEN_MINUTES_OF_SECONDS;
        highAcc[3] = d.getAccSeconds() <= opD.getOffSeconds() + TEN_MINUTES_OF_SECONDS;

        hallPassed[0] = true;
        hallPassed[1] = true;
        hallPassed[2] = true;
        hallPassed[3] = true;

        for (int i = 0; i < 4; i++) {
            if (!highAbu[i]) {
                hallPassed[i] = false;
            }
            if (!lowAbu[i]) {
                hallPassed[i] = false;
            }
            if (!lowBanu[i]) {
                hallPassed[i] = false;
            }
            if (!highBna[i]) {
                hallPassed[i] = false;
            }
            if (!highOff[i]) {
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

    public boolean[] getLowAbu() {
        return lowAbu;
    }

    public boolean[] getLowBanu() {
        return lowBanu;
    }

    public boolean[] getHighBna() {
        return highBna;
    }

    public boolean[] getHighOff() {
        return highOff;
    }

    public boolean[] getHighAcc() {
        return highAcc;
    }

    public String[] getHighAbuMessage() {
        return highAbuMessage;
    }

    public String[] getLowAbuMessage() {
        return lowAbuMessage;
    }

    public String[] getLowBanuMessage() {
        return lowBanuMessage;
    }

    public String[] getHighBnaMessage() {
        return highBnaMessage;
    }

    public String[] getHighOffMessage() {
        return highOffMessage;
    }

    public String[] getHighAccMessage() {
        return highAccMessage;
    }
}
