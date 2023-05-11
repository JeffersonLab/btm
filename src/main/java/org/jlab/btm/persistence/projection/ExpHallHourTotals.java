package org.jlab.btm.persistence.projection;

/**
 * @author ryans
 */
public class ExpHallHourTotals extends ExpHallShiftTotals {

    private final Integer erSeconds;
    private final Integer pccSeconds;
    private final Integer uedSeconds;

    public ExpHallHourTotals(Character hall, Number hourCount, Number abuSeconds, Number banuSeconds,
                             Number bnaSeconds, Number accSeconds, Number offSeconds, Number erSeconds, Number pccSeconds, Number uedSeconds) {
        super(hall, hourCount, abuSeconds, banuSeconds, bnaSeconds, accSeconds, offSeconds);

        this.erSeconds = erSeconds.intValue();
        this.pccSeconds = pccSeconds.intValue();
        this.uedSeconds = uedSeconds.intValue();
    }

    public Integer getErSeconds() {
        return erSeconds;
    }

    public Integer getPccSeconds() {
        return pccSeconds;
    }

    public Integer getUedSeconds() {
        return uedSeconds;
    }
}
