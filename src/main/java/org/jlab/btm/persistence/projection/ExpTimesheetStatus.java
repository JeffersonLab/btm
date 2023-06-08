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

    public void setReasonDiscrepancyList(List<HourReasonDiscrepancy> discrepancyList) {
        this.reasonDiscrepancyList = discrepancyList;
    }

    public List<HourReasonDiscrepancy> getReasonDiscrepancyList() {
        return reasonDiscrepancyList;
    }
}
