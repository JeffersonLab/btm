package org.jlab.btm.persistence.projection;

import org.jlab.smoothness.business.util.TimeUtil;

import java.util.Calendar;
import java.util.Date;

/**
 * @author ryans
 */
public class ReviewDay {
    private Date day; // Hours, Minutes, Seconds ignored
    private BeamSummaryTotals accTotal;
    private DowntimeSummaryTotals downTotal;
    private boolean future;

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;

        future = false;

        if (day != null) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            future = day.after(cal.getTime());
        }
    }

    public BeamSummaryTotals getAccTotal() {
        return accTotal;
    }

    public void setAccTotal(BeamSummaryTotals accTotal) {
        this.accTotal = accTotal;
    }

    public DowntimeSummaryTotals getDownTotal() {
        return downTotal;
    }

    public void setDownTotals(DowntimeSummaryTotals downTotal) {
        this.downTotal = downTotal;
    }

    public Date calculateNextDay() {
        return TimeUtil.addDays(day, 1);
    }

    public boolean isFuture() {
        return future;
    }
}
