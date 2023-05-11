package org.jlab.btm.persistence.entity;

import org.jlab.btm.business.util.HourEntity;
import org.jlab.btm.persistence.enumeration.DataSource;

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
@Table(name = "OP_ACC_HOUR", schema = "JBTA_OWNER", uniqueConstraints
        = {
        @UniqueConstraint(columnNames = {"DAY_AND_HOUR"})})
@NamedQueries({
        @NamedQuery(name = "OpAccHour.findAll", query
                = "SELECT o FROM OpAccHour o")})
@NamedNativeQueries({
        @NamedNativeQuery(name = "OpAccHour.insertNATIVE", query = "INSERT into OP_ACC_HOUR (OP_ACC_HOUR_ID, DAY_AND_HOUR, UP_SECONDS, SAD_SECONDS, DOWN_SECONDS, STUDIES_SECONDS, ACC_SECONDS, RESTORE_SECONDS) values (:id, to_timestamp_tz(:dayAndHour, 'YYYY-MM-DD HH24 TZD'), :up, :sad, :down, :studies, :acc, :restore)", resultClass = OpAccHour.class)})
/*@NamedNativeQuery(name = "OpAccHour.updateNATIVE", query = "UPDATE OP_ACC_HOUR SET UP_SECONDS = :up, SAD_SECONDS = :sad, DOWN_SECONDS = :down, STUDIES_SECONDS = :studies, ACC_SECONDS = :acc, RESTORE_SECONDS = :restore WHERE OP_ACC_HOUR_ID = :id", resultClass=OpAccHour.class)})*/
public class OpAccHour implements Serializable, HourEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name = "AccHourId", sequenceName = "OP_ACC_HOUR_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AccHourId")
    @Basic(optional = false)
    @NotNull
    @Column(name = "OP_ACC_HOUR_ID", nullable = false, precision = 22, scale = 0)
    private BigInteger opAccHourId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "DAY_AND_HOUR", nullable = false, insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dayAndHour;
    @Basic(optional = false)
    @NotNull
    @Column(name = "UP_SECONDS", nullable = false)
    @Max(value = 3600, message = "PHYSICS must be less than or equal to 1 hour")
    @Min(value = 0, message = "PHYSICS must be greater than or equal to 0")
    private short upSeconds;
    @Basic(optional = false)
    @NotNull
    @Column(name = "SAD_SECONDS", nullable = false)
    @Max(value = 3600, message = "OFF must be less than or equal to 1 hour")
    @Min(value = 0, message = "OFF must be greater than or equal to 0")
    private short sadSeconds;
    @Basic(optional = false)
    @NotNull
    @Column(name = "DOWN_SECONDS", nullable = false)
    @Max(value = 3600, message = "DOWN must be less than or equal to 1 hour")
    @Min(value = 0, message = "DOWN must be greater than or equal to 0")
    private short downSeconds;
    @Basic(optional = false)
    @NotNull
    @Column(name = "STUDIES_SECONDS", nullable = false)
    @Max(value = 3600, message = "STUDIES must be less than or equal to 1 hour")
    @Min(value = 0, message = "STUDIES must be greater than or equal to 0")
    private short studiesSeconds;
    @Basic(optional = false)
    @NotNull
    @Column(name = "ACC_SECONDS", nullable = false)
    @Max(value = 3600, message = "ACC must be less than or equal to 1 hour")
    @Min(value = 0, message = "ACC must be greater than or equal to 0")
    private short accSeconds;
    @Basic(optional = false)
    @NotNull
    @Column(name = "RESTORE_SECONDS", nullable = false)
    @Max(value = 3600, message = "RESTORE must be less than or equal to 1 hour")
    @Min(value = 0, message = "RESTORE must be greater than or equal to 0")
    private short restoreSeconds;
    @Transient
    private DataSource source;

    public OpAccHour() {
    }

    public BigInteger getOpAccHourId() {
        return opAccHourId;
    }

    public void setOpAccHourId(BigInteger opAccHourId) {
        this.opAccHourId = opAccHourId;
    }

    @Override
    public Date getDayAndHour() {
        return dayAndHour;
    }

    @Override
    public void setDayAndHour(Date dayAndHour) {
        this.dayAndHour = dayAndHour;
    }
    
    /*public Date getDayAndHour() {
        return dayAndHour == null ? null : dayAndHour.getTime();
    }

    public void setDayAndHour(Date dayAndHour) {
        if (dayAndHour == null) {
            this.dayAndHour = null;
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(dayAndHour);
            this.dayAndHour = cal;
        }
    }*/

    public short getUpSeconds() {
        return upSeconds;
    }

    public void setUpSeconds(short upSeconds) {
        this.upSeconds = upSeconds;
    }

    public short getSadSeconds() {
        return sadSeconds;
    }

    public void setSadSeconds(short sadSeconds) {
        this.sadSeconds = sadSeconds;
    }

    public short getDownSeconds() {
        return downSeconds;
    }

    public void setDownSeconds(short downSeconds) {
        this.downSeconds = downSeconds;
    }

    public short getStudiesSeconds() {
        return studiesSeconds;
    }

    public void setStudiesSeconds(short studiesSeconds) {
        this.studiesSeconds = studiesSeconds;
    }

    public short getAccSeconds() {
        return accSeconds;
    }

    public void setAccSeconds(short accSeconds) {
        this.accSeconds = accSeconds;
    }

    public short getRestoreSeconds() {
        return restoreSeconds;
    }

    public void setRestoreSeconds(short restoreSeconds) {
        this.restoreSeconds = restoreSeconds;
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
        return "org.jlab.webapp.persistence.entity.OpAccHour[ opAccHourId=" + opAccHourId + " ]";
    }

}
