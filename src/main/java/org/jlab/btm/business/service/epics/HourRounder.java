package org.jlab.btm.business.service.epics;

import org.jlab.btm.persistence.entity.OpAccHour;
import org.jlab.btm.persistence.entity.OpHallHour;
import org.jlab.btm.persistence.entity.OpMultiplicityHour;

import java.util.List;

/**
 * Responsible for rounding mutually exclusive EPICS time accounting statuses such that they sum to
 * exactly one hour.
 * <p>
 * Rounding is limited by a threshold of one minute.
 *
 * @author ryans
 */
public class HourRounder {

    /**
     * The default rounding threshold is one minute.
     */
    public static final int ROUND_THRESHOLD = 60;

    public static final int SECONDS_PER_HOUR = 3600;

    public static final int SECONDS_PER_HUNDRETH_OF_HOUR = 36;

    /**
     * Round a list of accelerator hour time accounting.
     *
     * @param hours the list of hours.
     */
    public void roundAcceleratorHourList(List<OpAccHour> hours) {
        for (OpAccHour hour : hours) {
            roundAcceleratorHour(hour);
        }
    }

    /**
     * Round a list of experimenter hall hour time accounting.
     *
     * @param hours the list of hours.
     */
    public void roundHallHourList(List<OpHallHour> hours) {
        for (OpHallHour hour : hours) {
            roundHallHour(hour);
        }
    }

    public void roundMultiplicityHourList(List<OpMultiplicityHour> multiHours, List<List<OpHallHour>> hallHoursList) {
        for (int i = 0; i < multiHours.size(); i++) {
            OpMultiplicityHour multiHour = multiHours.get(i);
            OpHallHour hallAHour = hallHoursList.get(0).get(i);
            OpHallHour hallBHour = hallHoursList.get(1).get(i);
            OpHallHour hallCHour = hallHoursList.get(2).get(i);
            OpHallHour hallDHour = hallHoursList.get(3).get(i);
            short maxUp = (short) Math.max(Math.max(hallAHour.getUpSeconds(), hallBHour.getUpSeconds()), Math.max(hallCHour.getUpSeconds(), hallDHour.getUpSeconds()));
            roundMultiplicityHour(multiHour, maxUp);
        }
    }

    /**
     * Add up status seconds.
     *
     * @param statuses the time accounting seconds.
     * @return the total seconds.
     */
    protected int sum(short[] statuses) {
        int sum = 0;

        for (short s : statuses) {
            sum = sum + s;
        }

        return sum;
    }

    /**
     * Truncates time accounting statuses which are outside of the range 0 - 3600 seconds.
     *
     * @param statuses the time accounting statuses in seconds. The array is modified in-place.
     */
    protected void truncateToRange(short[] statuses) {
        // Force statuses within range 0 - 3600
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i] < 0) {
                statuses[i] = 0;
            } else if (statuses[i] > SECONDS_PER_HOUR) {
                statuses[i] = SECONDS_PER_HOUR;
            }
        }
    }

    /**
     * Distributes left-over seconds into or remove extra seconds from a mutually exclusive set of
     * time accounting statuses (buckets).
     * <p>
     * The algorithm for distributing extra seconds evenly is to loop over each status 'bucket' and
     * if the 'bucket' is not exactly full (3600 seconds) or empty (0 seconds) then add/subtract one
     * second. Notice that in this algorithm the order of the statuses in the array is very
     * important as statuses with a lower index will be modified more often.
     *
     * @param statuses the time accounting statuses in seconds. The array is modified in-place.
     * @param amount   the left-over seconds to distribute. A negative number indicates too many
     *                 seconds and subtraction should be used.
     */
    protected void distributeEvenly(short[] statuses, int amount) {
        int sign = (amount < 0) ? -1 : 1;

        // Break out of loop when amount is no longer being distributed (prevent infinite loop).
        // This occurs when amount is too much / statuses don't have enough room
        int skipped = 0;

        // Add amount to statuses relatively evenly not to push status outside of range 0 - 3600
        for (int i = 0; amount != 0; i = (i + 1) % statuses.length) {
            if (statuses[i] != 0 && statuses[i] != SECONDS_PER_HOUR) { // Don't add/substract time to full or empty status
                statuses[i] = (short) (statuses[i] + (1 * sign));
                amount = amount - (1 * sign);
                skipped = 0;
            } else {
                if (skipped > statuses.length) {
                    break;
                }

                skipped++;
            }
        }
    }

    protected void roundMutuallyExclusiveSet(short[] statuses) {
        truncateToRange(statuses);

        int total = sum(statuses);
        int difference = SECONDS_PER_HOUR - total;

        if (difference != 0 && Math.abs(difference) <= ROUND_THRESHOLD) {
            distributeEvenly(statuses, difference);
        }
    }

    protected void roundToNearest100thOfAnHour(short[] statuses) {
        int remainder = 0;
        for (int i = 0; i < statuses.length; i++) {
            remainder = remainder + (statuses[i] % SECONDS_PER_HUNDRETH_OF_HOUR);
            statuses[i] = (short) ((statuses[i] / SECONDS_PER_HUNDRETH_OF_HOUR) * SECONDS_PER_HUNDRETH_OF_HOUR);
        }

        /*System.out.println("Remainder: " + remainder);*/

        for (int i = 0; i < (remainder / SECONDS_PER_HUNDRETH_OF_HOUR); i++) {
            /*System.out.println("Looking for somewhere to put 36 seconds...");*/
            for (int j = 0; j < statuses.length; j++) {
                if (statuses[j] != 0 && statuses[j] < SECONDS_PER_HOUR) {
                    /*System.out.println("Adding 36 to: " + statuses[j]);*/
                    statuses[j] = (short) (statuses[j] + SECONDS_PER_HUNDRETH_OF_HOUR);
                    break;
                }
            }
        }
    }

    /**
     * The "UP" (PHYSICS) measurement is actually using "ANY UP",
     * which fails to account for hall down, tune, and BNR.   If the hour doesn't add up and there is
     * at least some PHYSICS then put unaccounted for time in PHYSICS.
     *
     * @param statuses
     */
    protected void adjustForBadPhysicsMeasurement(short[] statuses) {
        int total = sum(statuses);
        int difference = SECONDS_PER_HOUR - total;

        if (statuses[0] > 0 && difference > 0) {
            statuses[0] = (short) (statuses[0] + difference);
        }

        if (statuses[0] > SECONDS_PER_HOUR) {
            statuses[0] = SECONDS_PER_HOUR;
        }
    }

    protected short adjustLeadingValueWithinThresholdMaxOneHour(short leadingValue, short followingValue) {
        /*System.out.println("leading: " + leadingValue + ", following: " + followingValue);*/

        int difference = Math.abs(leadingValue - followingValue);
        if (leadingValue < followingValue && difference <= (ROUND_THRESHOLD * 5)) { // THRESHOLD IS 5 MINUTES
            leadingValue = (short) (leadingValue + difference);
            if (leadingValue > SECONDS_PER_HOUR) {
                leadingValue = SECONDS_PER_HOUR;
            }
        }

        /*System.out.println("adjusted leading: " + leadingValue);*/

        return leadingValue;
    }
    
    /*protected short roundToNearestMinute(short value) {
        short newValue;

        int remander = value % 60;
        newValue = (short) ((value / 60) * 60);
        if (remander >= 30) {
            newValue = (short) (newValue + 60);
        }

        if (value > SECONDS_PER_HOUR) {
            newValue = SECONDS_PER_HOUR;
        } else if (value < 0) {
            newValue = 0;
        }

        return newValue;
    }*/

    protected short truncateToHourAndToNearest100thOfAnHour(short value) {
        short newValue = value;

        if (value > SECONDS_PER_HOUR) {
            newValue = SECONDS_PER_HOUR;
        } else if (value < 0) {
            newValue = 0;
        }

        newValue = (short) ((newValue / SECONDS_PER_HUNDRETH_OF_HOUR) * SECONDS_PER_HUNDRETH_OF_HOUR);

        return newValue;
    }

    /**
     * Rounds only the accelerator mutual exclusive set of statuses for an hour.
     *
     * @param hour the experimenter hall hour.
     */
    public void roundAcceleratorHour(OpAccHour hour) {
        // Won't modify off since it is a shared status
        short[] statuses = new short[6];
        statuses[0] = hour.getUpSeconds();
        statuses[1] = hour.getStudiesSeconds();
        statuses[2] = hour.getRestoreSeconds();
        statuses[3] = hour.getAccSeconds();
        statuses[4] = hour.getDownSeconds();
        statuses[5] = hour.getSadSeconds();

        /* Address fact that hours from EPICS don't add up out-of-the-box */
        roundMutuallyExclusiveSet(statuses);

        /* Address fact that Crew Chief works in decimal hours and prefers to keep times within 100ths of an hour*/
        roundToNearest100thOfAnHour(statuses);

        /* Address fact that PHYSICS (UP) is not measured correctly */
        adjustForBadPhysicsMeasurement(statuses);

        hour.setUpSeconds(statuses[0]);
        hour.setStudiesSeconds(statuses[1]);
        hour.setRestoreSeconds(statuses[2]);
        hour.setAccSeconds(statuses[3]);
        hour.setDownSeconds(statuses[4]);
        hour.setSadSeconds(statuses[5]);
    }

    /**
     * Rounds only the experimenter mutual exclusive set of statuses for an hour.
     *
     * @param hour the experimenter hall hour.
     */
    public void roundHallHour(OpHallHour hour) {
        // Won't modify off since it is a shared status
        short[] statuses = new short[5];
        statuses[0] = hour.getUpSeconds();
        statuses[1] = hour.getTuneSeconds();
        statuses[2] = hour.getBnrSeconds();
        statuses[3] = hour.getDownSeconds();
        statuses[4] = hour.getOffSeconds();

        /* Address fact that hours from EPICS don't add up out-of-the-box */
        roundMutuallyExclusiveSet(statuses);

        /* Address fact that Crew Chief works in decimal hours and prefers to keep times within 100ths of an hour*/
        roundToNearest100thOfAnHour(statuses);

        hour.setUpSeconds(statuses[0]);
        hour.setTuneSeconds(statuses[1]);
        hour.setBnrSeconds(statuses[2]);
        hour.setDownSeconds(statuses[3]);
        hour.setOffSeconds(statuses[4]);
    }

    /**
     * We need to make sure the values don't independently exceed an hour.
     *
     * @param hour
     */
    private void roundMultiplicityHour(OpMultiplicityHour hour, short maxUp) {
        /*System.out.println("maxUp: " + maxUp);*/

        hour.setOneHallUpSeconds(truncateToHourAndToNearest100thOfAnHour(hour.getOneHallUpSeconds()));
        hour.setTwoHallUpSeconds(truncateToHourAndToNearest100thOfAnHour(hour.getTwoHallUpSeconds()));
        hour.setThreeHallUpSeconds(truncateToHourAndToNearest100thOfAnHour(hour.getThreeHallUpSeconds()));
        hour.setFourHallUpSeconds(truncateToHourAndToNearest100thOfAnHour(hour.getFourHallUpSeconds()));
        hour.setAnyHallUpSeconds(adjustLeadingValueWithinThresholdMaxOneHour(truncateToHourAndToNearest100thOfAnHour(hour.getAnyHallUpSeconds()), maxUp));
        hour.setAllHallUpSeconds(adjustLeadingValueWithinThresholdMaxOneHour(truncateToHourAndToNearest100thOfAnHour(hour.getAllHallUpSeconds()), maxUp));
        hour.setDownHardSeconds(truncateToHourAndToNearest100thOfAnHour(hour.getDownHardSeconds()));
    }
}
