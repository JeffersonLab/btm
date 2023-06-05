package org.jlab.btm.persistence.projection;

/**
 * @author ryans
 */
public class ExpTimesheetStatus {
    private boolean availabilityComplete = false;

    private boolean reasonsNotReadyComplete = false;
    private boolean shiftInfoComplete = false;
    private boolean signatureComplete = false;

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
}
