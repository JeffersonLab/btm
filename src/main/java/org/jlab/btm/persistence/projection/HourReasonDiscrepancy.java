package org.jlab.btm.persistence.projection;

import java.util.Date;

/**
 * Models the difference between reasons not ready and unplanned experiment
 * down.
 *
 * @author ryans
 */
public final class HourReasonDiscrepancy {
    private Date dayAndHour;
    private int uedSeconds;
    private int reasonTotalSeconds;
    private int differenceSeconds;

    public HourReasonDiscrepancy(Date dayAndHour, int uedSeconds, int reasonTotalSeconds) {
        this.dayAndHour = dayAndHour;
        this.uedSeconds = uedSeconds;
        this.reasonTotalSeconds = reasonTotalSeconds;
        this.differenceSeconds = uedSeconds - reasonTotalSeconds;
    }

    public Date getDayAndHour() {
        return dayAndHour;
    }

    public int getUedSeconds() {
        return uedSeconds;
    }

    public int getReasonTotalSeconds() {
        return reasonTotalSeconds;
    }

    public int getDifferenceSeconds() {
        return differenceSeconds;
    }

}
