package org.jlab.btm.persistence.projection;

/**
 * A Sum of PAC schedule accelerator program days.
 *
 * In practice only PHYSICS, RESTORE, and OFF (plus implied OFF) are really used by PAC as Studies and ACC is often hand-wavy
 * allowance (perhaps covered by double schedule slop factor).
 */
public class PacAccSum {
    public int accProgram = 0; // Anything but OFF or implied OFF
    public int restore = 0;
    public int physics = 0;
    public int studies = 0;
    public int acc = 0;
    public int off = 0;

    public int getAccProgram() {
        return accProgram;
    }

    public void setAccProgram(int accProgram) {
        this.accProgram = accProgram;
    }

    public int getRestore() {
        return restore;
    }

    public void setRestore(int restore) {
        this.restore = restore;
    }

    public int getPhysics() {
        return physics;
    }

    public void setPhysics(int physics) {
        this.physics = physics;
    }

    public int getStudies() {
        return studies;
    }

    public void setStudies(int studies) {
        this.studies = studies;
    }

    public int getAcc() {
        return acc;
    }

    public void setAcc(int acc) {
        this.acc = acc;
    }

    public int getOff() {
        return off;
    }

    public void setOff(int off) {
        this.off = off;
    }
}
