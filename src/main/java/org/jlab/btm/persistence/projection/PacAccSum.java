package org.jlab.btm.persistence.projection;

/**
 * A Sum of PAC schedule accelerator program days.
 *
 * In practice only PHYSICS, RESTORE, and OFF (plus implied OFF) are really used by PAC as Studies and ACC is often hand-wavy
 * allowance (perhaps covered by double schedule slop factor).
 */
public class PacAccSum {
    public int programDays = 0; // Anything but OFF or implied OFF
    public int restoreDays = 0;
    public int physicsDays = 0;
    public int studiesDays = 0;
    public int accDays = 0;
    public int offDays = 0;

    public int getProgramDays() {
        return programDays;
    }

    public void setProgramDays(int programDays) {
        this.programDays = programDays;
    }

    public int getRestoreDays() {
        return restoreDays;
    }

    public void setRestoreDays(int restoreDays) {
        this.restoreDays = restoreDays;
    }

    public int getPhysicsDays() {
        return physicsDays;
    }

    public void setPhysicsDays(int physicsDays) {
        this.physicsDays = physicsDays;
    }

    public int getStudiesDays() {
        return studiesDays;
    }

    public void setStudiesDays(int studiesDays) {
        this.studiesDays = studiesDays;
    }

    public int getAccDays() {
        return accDays;
    }

    public void setAccDays(int accDays) {
        this.accDays = accDays;
    }

    public int getOffDays() {
        return offDays;
    }

    public void setOffDays(int offDays) {
        this.offDays = offDays;
    }
}
