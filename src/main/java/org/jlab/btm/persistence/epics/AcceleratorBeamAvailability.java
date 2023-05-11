package org.jlab.btm.persistence.epics;

import org.jlab.btm.persistence.entity.OpAccHour;
import org.jlab.smoothness.business.util.TimeUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents Accelerator Beam Availability for one week as recorded by an EPICS
 * IOC.
 *
 * @author ryans
 */
public class AcceleratorBeamAvailability {

    private double[] time = null;
    private double[] up = null;
    private double[] sad = null;
    private double[] down = null;
    private double[] studies = null;
    private double[] restore = null;
    private double[] acc = null;

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

    public double[] getSad() {
        return sad;
    }

    public void setSad(double[] sad) {
        this.sad = sad;
    }

    public double[] getDown() {
        return down;
    }

    public void setDown(double[] down) {
        this.down = down;
    }

    public double[] getStudies() {
        return studies;
    }

    public void setStudies(double[] studies) {
        this.studies = studies;
    }

    public double[] getRestore() {
        return restore;
    }

    public void setRestore(double[] restore) {
        this.restore = restore;
    }

    public double[] getAcc() {
        return acc;
    }

    public void setAcc(double[] acc) {
        this.acc = acc;
    }

    /**
     * Determine if the data is valid.
     * <p>
     * This method checks to ensure that each status array is not null and that
     * number of values in each array is equal to the number of hours in the
     * EPICS history window.
     *
     * @return true if the data is valid.
     */
    public boolean isValidValue() {
        if (time == null || up == null || sad == null || down == null
                || studies == null || restore == null || acc == null) {
            return false;
        }

        return time.length == Constant.NUMBER_OF_HOURS_IN_HISTORY
                && up.length == Constant.NUMBER_OF_HOURS_IN_HISTORY
                && sad.length == Constant.NUMBER_OF_HOURS_IN_HISTORY
                && down.length == Constant.NUMBER_OF_HOURS_IN_HISTORY
                && studies.length == Constant.NUMBER_OF_HOURS_IN_HISTORY
                && restore.length == Constant.NUMBER_OF_HOURS_IN_HISTORY
                && acc.length == Constant.NUMBER_OF_HOURS_IN_HISTORY;
    }

    /**
     * Convert the data into a list of OpAccHours.
     *
     * @return the list of experimenter hall hours.
     */
    public List<OpAccHour> getOpAccHours() {
        List<OpAccHour> hours = new ArrayList<>();

        if (!isValidValue()) {
            throw new IllegalStateException(
                    "EPICS channel access values are uninitialized or invalid");
        }

        for (int i = 0; i < time.length; i++) {
            OpAccHour hour = new OpAccHour();

            // EPICS uses UNIX timestamps
            Date hourOfDay = TimeUtil.convertUNIXTimestampToDate(
                    (long) time[i]);

            // The timestamps are not exactly on the hour!
            hourOfDay = TimeUtil.roundToNearestHour(hourOfDay);

            // The timestamps represent the end of the hour, NOT the start!
            hourOfDay = TimeUtil.addHours(hourOfDay, -1);

            /*try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US);
                Date start = format.parse("2018-11-03 23:00:00");
                Date end = format.parse("2018-11-04 04:00:00");
                if (hourOfDay.after(start) && hourOfDay.before(end)) {
                    System.out.println("Hour: " + format2.format(hourOfDay));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }*/

            hour.setDayAndHour(hourOfDay);
            hour.setUpSeconds((short) up[i]);
            hour.setSadSeconds((short) sad[i]);
            hour.setDownSeconds((short) down[i]);
            hour.setStudiesSeconds((short) studies[i]);
            hour.setRestoreSeconds((short) restore[i]);
            hour.setAccSeconds((short) acc[i]);

            hours.add(hour);
        }

        return hours;
    }
}
