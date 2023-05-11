package org.jlab.btm.persistence.entity;

import org.jlab.btm.business.util.HourEntity;
import org.jlab.btm.persistence.enumeration.DataSource;
import org.jlab.smoothness.persistence.enumeration.Hall;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

/**
 * @author ryans
 */
@Entity
@Table(name = "OP_HALL_HOUR", schema = "JBTA_OWNER", uniqueConstraints
        = {
        @UniqueConstraint(columnNames = {"HALL", "DAY_AND_HOUR"})})
@NamedNativeQueries({
        @NamedNativeQuery(name = "OpHallHour.insertNATIVE", query = "INSERT into OP_HALL_HOUR (OP_HALL_HOUR_ID, HALL, DAY_AND_HOUR, UP_SECONDS, TUNE_SECONDS, BNR_SECONDS, DOWN_SECONDS, OFF_SECONDS) values (:id, :hall, to_timestamp_tz(:dayAndHour, 'YYYY-MM-DD HH24 TZD'), :up, :tune, :bnr, :down, :off)", resultClass = OpHallHour.class)})
public class OpHallHour implements Serializable, HourEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name = "HallHourId", sequenceName = "OP_HALL_HOUR_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "HallHourId")
    @Basic(optional = false)
    @NotNull
    @Column(name = "OP_HALL_HOUR_ID", nullable = false, precision = 38, scale
            = 0)
    private BigInteger opHallHourId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "DAY_AND_HOUR", nullable = false, insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dayAndHour;
    @Basic(optional = false)
    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Hall hall;
    @Basic(optional = false)
    @NotNull
    @Column(name = "UP_SECONDS", nullable = false)
    @Max(value = 3600, message = "CW must be less than or equal to 1 hour")
    @Min(value = 0, message = "CW must be greater than or equal to 0")
    private short upSeconds;
    @Basic(optional = false)
    @NotNull
    @Column(name = "TUNE_SECONDS", nullable = false)
    @Max(value = 3600, message = "TUNE must be less than or equal to 1 hour")
    @Min(value = 0, message = "TUNE must be greater than or equal to 0")
    private short tuneSeconds;
    @Basic(optional = false)
    @NotNull
    @Column(name = "BNR_SECONDS", nullable = false)
    @Max(value = 3600, message = "BNR must be less than or equal to 1 hour")
    @Min(value = 0, message = "BNR must be greater than or equal to 0")
    private short bnrSeconds;
    @Basic(optional = false)
    @NotNull
    @Column(name = "DOWN_SECONDS", nullable = false)
    @Max(value = 3600, message = "DOWN must be less than or equal to 1 hour")
    @Min(value = 0, message = "DOWN must be greater than or equal to 0")
    private short downSeconds;
    @Basic(optional = false)
    @NotNull
    @Column(name = "OFF_SECONDS", nullable = false)
    @Max(value = 3600, message = "OFF must be less than or equal to 1 hour")
    @Min(value = 0, message = "OFF must be greater than or equal to 0")
    private short offSeconds;
    @Transient
    private DataSource source;

    public BigInteger getOpHallHourId() {
        return opHallHourId;
    }

    public void setOpHallHourId(BigInteger opHallHourId) {
        this.opHallHourId = opHallHourId;
    }

    @Override
    public Date getDayAndHour() {
        return dayAndHour;
    }

    @Override
    public void setDayAndHour(Date dayAndHour) {
        this.dayAndHour = dayAndHour;
    }

    public Hall getHall() {
        return hall;
    }

    public void setHall(Hall hall) {
        this.hall = hall;
    }

    public short getUpSeconds() {
        return upSeconds;
    }

    public void setUpSeconds(short upSeconds) {
        this.upSeconds = upSeconds;
    }

    public short getTuneSeconds() {
        return tuneSeconds;
    }

    public void setTuneSeconds(short tuneSeconds) {
        this.tuneSeconds = tuneSeconds;
    }

    public short getBnrSeconds() {
        return bnrSeconds;
    }

    public void setBnrSeconds(short bnrSeconds) {
        this.bnrSeconds = bnrSeconds;
    }

    public short getDownSeconds() {
        return downSeconds;
    }

    public void setDownSeconds(short downSeconds) {
        this.downSeconds = downSeconds;
    }

    public short getOffSeconds() {
        return offSeconds;
    }

    public void setOffSeconds(short offSeconds) {
        this.offSeconds = offSeconds;
    }

    public DataSource getSource() {
        return source;
    }

    @Override
    public void setSource(DataSource source) {
        this.source = source;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.getDayAndHour() != null
                ? this.getDayAndHour().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof OpAccHour)) {
            return false;
        }
        OpAccHour other = (OpAccHour) object;
        return (this.getDayAndHour() != null || other.getDayAndHour() == null)
                && (this.getDayAndHour() == null || this.getDayAndHour().equals(
                other.getDayAndHour()));
    }

    @Override
    public String toString() {
        return "org.jlab.webapp.persistence.entity.OpHallHour[ opHallHourId=" + opHallHourId + " ]";
    }

}
