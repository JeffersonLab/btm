package org.jlab.btm.persistence.entity;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
 * @author ryans
 */
@Entity
@Table(name = "PD_SHIFT_PLAN", schema = "JBTA_OWNER")
public class PdShiftPlan implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column(name = "START_DAY_AND_HOUR")
    @Temporal(TemporalType.TIMESTAMP)
    @Id
    private Date startDayAndHour;
    @Size(max = 0)
    @Column(name = "PHYSICS_SECONDS", length = 0)
    private Integer physicsSeconds;
    @Column(name = "STUDIES_SECONDS")
    private Integer studiesSeconds;
    @Column(name = "RESTORE_SECONDS")
    private Integer restoreSeconds;
    @Column(name = "ACC_SECONDS")
    private Integer accSeconds;
    @Column(name = "DOWN_SECONDS")
    private Integer downSeconds;
    @Size(max = 0)
    @Column(name = "SAD_SECONDS", length = 0)
    private Integer sadSeconds;
    @Column(name = "HALL_A_UP_SECONDS")
    private Integer hallAUpSeconds;
    @Size(max = 0)
    @Column(name = "HALL_A_TUNE_SECONDS", length = 0)
    private Integer hallATuneSeconds;
    @Size(max = 0)
    @Column(name = "HALL_A_BNR_SECONDS", length = 0)
    private Integer hallABnrSeconds;
    @Size(max = 0)
    @Column(name = "HALL_A_DOWN_SECONDS", length = 0)
    private Integer hallADownSeconds;
    @Size(max = 0)
    @Column(name = "HALL_A_OFF_SECONDS", length = 0)
    private Integer hallAOffSeconds;
    @Column(name = "HALL_B_UP_SECONDS")
    private Integer hallBUpSeconds;
    @Size(max = 0)
    @Column(name = "HALL_B_TUNE_SECONDS", length = 0)
    private Integer hallBTuneSeconds;
    @Size(max = 0)
    @Column(name = "HALL_B_BNR_SECONDS", length = 0)
    private Integer hallBBnrSeconds;
    @Size(max = 0)
    @Column(name = "HALL_B_DOWN_SECONDS", length = 0)
    private Integer hallBDownSeconds;
    @Size(max = 0)
    @Column(name = "HALL_B_OFF_SECONDS", length = 0)
    private Integer hallBOffSeconds;
    @Column(name = "HALL_C_UP_SECONDS")
    private Integer hallCUpSeconds;
    @Size(max = 0)
    @Column(name = "HALL_C_TUNE_SECONDS", length = 0)
    private Integer hallCTuneSeconds;
    @Size(max = 0)
    @Column(name = "HALL_C_BNR_SECONDS", length = 0)
    private Integer hallCBnrSeconds;
    @Size(max = 0)
    @Column(name = "HALL_C_DOWN_SECONDS", length = 0)
    private Integer hallCDownSeconds;
    @Size(max = 0)
    @Column(name = "HALL_C_OFF_SECONDS", length = 0)
    private Integer hallCOffSeconds;
    @Column(name = "HALL_D_UP_SECONDS")
    private Integer hallDUpSeconds;
    @Size(max = 0)
    @Column(name = "HALL_D_TUNE_SECONDS", length = 0)
    private Integer hallDTuneSeconds;
    @Size(max = 0)
    @Column(name = "HALL_D_BNR_SECONDS", length = 0)
    private Integer hallDBnrSeconds;
    @Size(max = 0)
    @Column(name = "HALL_D_DOWN_SECONDS", length = 0)
    private Integer hallDDownSeconds;
    @Size(max = 0)
    @Column(name = "HALL_D_OFF_SECONDS", length = 0)
    private Integer hallDOffSeconds;

    public PdShiftPlan() {
    }

    public Date getStartDayAndHour() {
        return startDayAndHour;
    }

    public void setStartDayAndHour(Date startDayAndHour) {
        this.startDayAndHour = startDayAndHour;
    }

    public Integer getPhysicsSeconds() {
        return physicsSeconds;
    }

    public void setPhysicsSeconds(Integer physicsSeconds) {
        this.physicsSeconds = physicsSeconds;
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

    public Integer getDownSeconds() {
        return downSeconds;
    }

    public void setDownSeconds(Integer downSeconds) {
        this.downSeconds = downSeconds;
    }

    public Integer getSadSeconds() {
        return sadSeconds;
    }

    public void setSadSeconds(Integer sadSeconds) {
        this.sadSeconds = sadSeconds;
    }

    public Integer getHallAUpSeconds() {
        return hallAUpSeconds;
    }

    public void setHallAUpSeconds(Integer hallAUpSeconds) {
        this.hallAUpSeconds = hallAUpSeconds;
    }

    public Integer getHallATuneSeconds() {
        return hallATuneSeconds;
    }

    public void setHallATuneSeconds(Integer hallATuneSeconds) {
        this.hallATuneSeconds = hallATuneSeconds;
    }

    public Integer getHallABnrSeconds() {
        return hallABnrSeconds;
    }

    public void setHallABnrSeconds(Integer hallABnrSeconds) {
        this.hallABnrSeconds = hallABnrSeconds;
    }

    public Integer getHallADownSeconds() {
        return hallADownSeconds;
    }

    public void setHallADownSeconds(Integer hallADownSeconds) {
        this.hallADownSeconds = hallADownSeconds;
    }

    public Integer getHallAOffSeconds() {
        return hallAOffSeconds;
    }

    public void setHallAOffSeconds(Integer hallAOffSeconds) {
        this.hallAOffSeconds = hallAOffSeconds;
    }

    public Integer getHallBUpSeconds() {
        return hallBUpSeconds;
    }

    public void setHallBUpSeconds(Integer hallBUpSeconds) {
        this.hallBUpSeconds = hallBUpSeconds;
    }

    public Integer getHallBTuneSeconds() {
        return hallBTuneSeconds;
    }

    public void setHallBTuneSeconds(Integer hallBTuneSeconds) {
        this.hallBTuneSeconds = hallBTuneSeconds;
    }

    public Integer getHallBBnrSeconds() {
        return hallBBnrSeconds;
    }

    public void setHallBBnrSeconds(Integer hallBBnrSeconds) {
        this.hallBBnrSeconds = hallBBnrSeconds;
    }

    public Integer getHallBDownSeconds() {
        return hallBDownSeconds;
    }

    public void setHallBDownSeconds(Integer hallBDownSeconds) {
        this.hallBDownSeconds = hallBDownSeconds;
    }

    public Integer getHallBOffSeconds() {
        return hallBOffSeconds;
    }

    public void setHallBOffSeconds(Integer hallBOffSeconds) {
        this.hallBOffSeconds = hallBOffSeconds;
    }

    public Integer getHallCUpSeconds() {
        return hallCUpSeconds;
    }

    public void setHallCUpSeconds(Integer hallCUpSeconds) {
        this.hallCUpSeconds = hallCUpSeconds;
    }

    public Integer getHallCTuneSeconds() {
        return hallCTuneSeconds;
    }

    public void setHallCTuneSeconds(Integer hallCTuneSeconds) {
        this.hallCTuneSeconds = hallCTuneSeconds;
    }

    public Integer getHallCBnrSeconds() {
        return hallCBnrSeconds;
    }

    public void setHallCBnrSeconds(Integer hallCBnrSeconds) {
        this.hallCBnrSeconds = hallCBnrSeconds;
    }

    public Integer getHallCDownSeconds() {
        return hallCDownSeconds;
    }

    public void setHallCDownSeconds(Integer hallCDownSeconds) {
        this.hallCDownSeconds = hallCDownSeconds;
    }

    public Integer getHallCOffSeconds() {
        return hallCOffSeconds;
    }

    public void setHallCOffSeconds(Integer hallCOffSeconds) {
        this.hallCOffSeconds = hallCOffSeconds;
    }

    public Integer getHallDUpSeconds() {
        return hallDUpSeconds;
    }

    public void setHallDUpSeconds(Integer hallDUpSeconds) {
        this.hallDUpSeconds = hallDUpSeconds;
    }

    public Integer getHallDTuneSeconds() {
        return hallDTuneSeconds;
    }

    public void setHallDTuneSeconds(Integer hallDTuneSeconds) {
        this.hallDTuneSeconds = hallDTuneSeconds;
    }

    public Integer getHallDBnrSeconds() {
        return hallDBnrSeconds;
    }

    public void setHallDBnrSeconds(Integer hallDBnrSeconds) {
        this.hallDBnrSeconds = hallDBnrSeconds;
    }

    public Integer getHallDDownSeconds() {
        return hallDDownSeconds;
    }

    public void setHallDDownSeconds(Integer hallDDownSeconds) {
        this.hallDDownSeconds = hallDDownSeconds;
    }

    public Integer getHallDOffSeconds() {
        return hallDOffSeconds;
    }

    public void setHallDOffSeconds(Integer hallDOffSeconds) {
        this.hallDOffSeconds = hallDOffSeconds;
    }
}
