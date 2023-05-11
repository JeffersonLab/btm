package org.jlab.btm.persistence.projection;

import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * @author ryans
 */
public class PhysicsSummaryTotals {
    private final Hall hall;
    private final long abuSeconds;
    private final long banuSeconds;
    private final long bnaSeconds;
    private final long accSeconds;
    private final long expOffSeconds;
    private final long upSeconds;
    private final long tuningSeconds;
    private final long bnrSeconds;
    private final long downSeconds;
    private final long opOffSeconds;

    public PhysicsSummaryTotals(Character hall, Number abuSeconds, Number banuSeconds, Number bnaSeconds,
                                Number accSeconds, Number expOffSeconds, Number upSeconds, Number tuningSeconds,
                                Number bnrSeconds, Number downSeconds, Number opOffSeconds) {
        this.hall = Hall.valueOf(hall.toString());
        this.abuSeconds = abuSeconds.longValue();
        this.banuSeconds = banuSeconds.longValue();
        this.bnaSeconds = bnaSeconds.longValue();
        this.accSeconds = accSeconds.longValue();
        this.expOffSeconds = expOffSeconds.longValue();
        this.upSeconds = upSeconds.longValue();
        this.tuningSeconds = tuningSeconds.longValue();
        this.bnrSeconds = bnrSeconds.longValue();
        this.downSeconds = downSeconds.longValue();
        this.opOffSeconds = opOffSeconds.longValue();
    }

    public Hall getHall() {
        return hall;
    }

    public long getAbuSeconds() {
        return abuSeconds;
    }

    public long getBanuSeconds() {
        return banuSeconds;
    }

    public long getBnaSeconds() {
        return bnaSeconds;
    }

    public long getAccSeconds() {
        return accSeconds;
    }

    public long getExpOffSeconds() {
        return expOffSeconds;
    }

    public long getUpSeconds() {
        return upSeconds;
    }

    public long getTuningSeconds() {
        return tuningSeconds;
    }

    public long getBnrSeconds() {
        return bnrSeconds;
    }

    public long getDownSeconds() {
        return downSeconds;
    }

    public long getOpOffSeconds() {
        return opOffSeconds;
    }
}
