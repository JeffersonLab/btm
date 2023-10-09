package org.jlab.btm.persistence.projection;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ryans
 */
public class ExpTimesheetStatus {
    private boolean availabilityComplete = false;
    private boolean reasonsNotReadyComplete = false;
    private boolean shiftInfoComplete = false;
    private boolean signatureComplete = false;
    private boolean ccHoursComplete = false;
    private boolean previousLastHourComplete = false;
    private List<HourReasonDiscrepancy> reasonDiscrepancyList = new ArrayList<>();

    public boolean isAvailabilityComplete() {
        return availabilityComplete;
    }

    public void setAvailabilityComplete(boolean availabilityComplete) {
        this.availabilityComplete = availabilityComplete;
    }

    public boolean isReasonsNotReadyComplete() {
        return reasonsNotReadyComplete;
    }

    public void setReasonsNotReadyComplete(boolean reasonsNotReadyComplete) {
        this.reasonsNotReadyComplete = reasonsNotReadyComplete;
    }

    public boolean isShiftInfoComplete() {
        return shiftInfoComplete;
    }

    public void setShiftInfoComplete(boolean shiftInfoComplete) {
        this.shiftInfoComplete = shiftInfoComplete;
    }

    public boolean isSignatureComplete() {
        return signatureComplete;
    }

    public void setSignatureComplete(boolean signature) {
        this.signatureComplete = signature;
    }

    public boolean isCcHoursComplete() {
        return ccHoursComplete;
    }

    public void setCcHoursComplete(boolean ccHoursComplete) {
        this.ccHoursComplete = ccHoursComplete;
    }

    public boolean isPreviousLastHourComplete() {
        return previousLastHourComplete;
    }

    public void setPreviousLastHourComplete(boolean previousLastHourComplete) {
        this.previousLastHourComplete = previousLastHourComplete;
    }

    public void setReasonDiscrepancyList(List<HourReasonDiscrepancy> discrepancyList) {
        this.reasonDiscrepancyList = discrepancyList;
    }

    public List<HourReasonDiscrepancy> getReasonDiscrepancyList() {
        return reasonDiscrepancyList;
    }

    /**
     * Since the CC previous hour is tied to another timesheet you can't just look to see if current timesheet signature exists.
     **/
    public boolean isTimesheetComplete() {
        return signatureComplete && shiftInfoComplete && reasonsNotReadyComplete && availabilityComplete && previousLastHourComplete;
    }
}
