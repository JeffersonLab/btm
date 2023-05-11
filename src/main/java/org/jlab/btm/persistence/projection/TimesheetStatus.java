package org.jlab.btm.persistence.projection;

/**
 * @author ryans
 */
public class TimesheetStatus {
    private boolean acceleratorComplete = false;
    private boolean hallAComplete = false;
    private boolean hallBComplete = false;
    private boolean hallCComplete = false;
    private boolean hallDComplete = false;
    private boolean multiplicityComplete = false;
    private boolean shiftInfoComplete = false;
    private boolean signatureComplete = false;

    public boolean isAcceleratorComplete() {
        return acceleratorComplete;
    }

    public void setAcceleratorComplete(boolean acceleratorComplete) {
        this.acceleratorComplete = acceleratorComplete;
    }

    public boolean isHallAComplete() {
        return hallAComplete;
    }

    public void setHallAComplete(boolean hallAComplete) {
        this.hallAComplete = hallAComplete;
    }

    public boolean isHallBComplete() {
        return hallBComplete;
    }

    public void setHallBComplete(boolean hallBComplete) {
        this.hallBComplete = hallBComplete;
    }

    public boolean isHallCComplete() {
        return hallCComplete;
    }

    public void setHallCComplete(boolean hallCComplete) {
        this.hallCComplete = hallCComplete;
    }

    public boolean isHallDComplete() {
        return hallDComplete;
    }

    public void setHallDComplete(boolean hallDComplete) {
        this.hallDComplete = hallDComplete;
    }

    public boolean isMultiplicityComplete() {
        return multiplicityComplete;
    }

    public void setMultiplicityComplete(boolean multiplicityComplete) {
        this.multiplicityComplete = multiplicityComplete;
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
