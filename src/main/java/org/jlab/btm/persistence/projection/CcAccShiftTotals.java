package org.jlab.btm.persistence.projection;

/**
 * @author ryans
 */
public class CcAccShiftTotals {
    private Integer upSeconds;
    private Integer sadSeconds;
    private Integer downSeconds;
    private Integer studiesSeconds;
    private Integer restoreSeconds;
    private Integer accSeconds;

    public Integer getUpSeconds() {
        return upSeconds;
    }

    public void setUpSeconds(Integer upSeconds) {
        this.upSeconds = upSeconds;
    }

    public Integer getSadSeconds() {
        return sadSeconds;
    }

    public void setSadSeconds(Integer sadSeconds) {
        this.sadSeconds = sadSeconds;
    }

    public Integer getDownSeconds() {
        return downSeconds;
    }

    public void setDownSeconds(Integer downSeconds) {
        this.downSeconds = downSeconds;
    }

    public Integer getStudiesSeconds() {
        return studiesSeconds;
    }

    public void setStudiesSeconds(Integer studiesSeconds) {
        this.studiesSeconds = studiesSeconds;
    }

    public Integer getRestoreSeconds() {
        return restoreSeconds;
    }

    public void setRestoreSeconds(Integer restoreSeconds) {
        this.restoreSeconds = restoreSeconds;
    }

    public Integer getAccSeconds() {
        return accSeconds;
    }

    public void setAccSeconds(Integer accSeconds) {
        this.accSeconds = accSeconds;
    }

    public Integer calculateProgramSeconds() {
        return restoreSeconds + accSeconds + upSeconds + studiesSeconds + downSeconds;
    }

    public Integer calculatePossibleDowntimeSeconds() {
        return upSeconds + downSeconds;
    }
}
