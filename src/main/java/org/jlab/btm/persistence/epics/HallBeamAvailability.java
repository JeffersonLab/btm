package org.jlab.btm.persistence.epics;

import org.jlab.btm.persistence.entity.OpHallHour;
import org.jlab.smoothness.business.util.TimeUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents Accelerator Beam Availability for one week as recorded by an
 * EPICS IOC.
 *
 * @author ryans
 */
public class HallBeamAvailability {

    private double[] time = null;
    private double[] up = null;
    private double[] tune = null;
    private double[] bnr = null;
    private double[] down = null;
    private double[] off = null;

    public double[] getTime() {
        return time;
    }

    public void setTime(double[] time) {
        this.time = time;
    }

    public double[] getUp() {
        return up;
    }

    public void setUp(double[] up) {
        this.up = up;
    }

    public double[] getTune() {
        return tune;
    }

    public void setTune(double[] tune) {
        this.tune = tune;
    }

    public double[] getBnr() {
        return bnr;
    }

    public void setBnr(double[] bnr) {
        this.bnr = bnr;
    }

    public double[] getDown() {
        return down;
    }

    public void setDown(double[] down) {
        this.down = down;
    }

    public double[] getOff() {
        return off;
    }

    public void setOff(double[] off) {
        this.off = off;
    }

    /**
     * Determine if the data is valid.
     * <p>
     * This method checks to ensure that each status array is not null
     * and that number of values in each array is equal to the number of
     * hours in the EPICS history window.
     *
     * @return true if the data is valid.
     */
    public boolean isValidValue() {
        if (time == null || up == null || tune == null || bnr == null
                || down == null || off == null) {
            return false;
        }

        return time.length == Constant.NUMBER_OF_HOURS_IN_HISTORY
                && up.length == Constant.NUMBER_OF_HOURS_IN_HISTORY
                && tune.length == Constant.NUMBER_OF_HOURS_IN_HISTORY
                && bnr.length == Constant.NUMBER_OF_HOURS_IN_HISTORY
                && down.length == Constant.NUMBER_OF_HOURS_IN_HISTORY
                && off.length == Constant.NUMBER_OF_HOURS_IN_HISTORY;
    }

    /**
     * Convert the data into a list of OpAccHours.
     *
     * @return the list of experimenter hall hours.
     */
    public List<OpHallHour> getOpHallHours() {
        List<OpHallHour> hours = new ArrayList<>();

        if (!isValidValue()) {
            throw new IllegalStateException(
                    "EPICS channel access values are uninitialized or invalid");
        }

        for (int i = 0; i < time.length; i++) {
            OpHallHour hour = new OpHallHour();

            // EPICS uses UNIX timestamps
            Date hourOfDay = TimeUtil.convertUNIXTimestampToDate(
                    (long) time[i]);

            // The timestamps are not exactly on the hour!
            hourOfDay = TimeUtil.roundToNearestHour(hourOfDay);

            // The timestamps represent the end of the hour, NOT the start!
            hourOfDay = TimeUtil.addHours(hourOfDay, -1);

            hour.setDayAndHour(hourOfDay);
            hour.setUpSeconds((short) up[i]);
            hour.setTuneSeconds((short) tune[i]);
            hour.setBnrSeconds((short) bnr[i]);
            hour.setDownSeconds((short) down[i]);
            hour.setOffSeconds((short) off[i]);

            hours.add(hour);
        }

        return hours;
    }
}
