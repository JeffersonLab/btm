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
@Table(name = "OP_MULTIPLICITY_HOUR", schema = "BTM_OWNER", uniqueConstraints
        = {
        @UniqueConstraint(columnNames = {"DAY_AND_HOUR"})})
@NamedNativeQueries({
        @NamedNativeQuery(name = "OpMultiplicityHour.insertNATIVE", query
                = "INSERT into OP_MULTIPLICITY_HOUR (OP_MULTIPLICITY_HOUR_ID, DAY_AND_HOUR, FOUR_HALL_UP_SECONDS, THREE_HALL_UP_SECONDS, TWO_HALL_UP_SECONDS, ONE_HALL_UP_SECONDS, ANY_HALL_UP_SECONDS, ALL_HALL_UP_SECONDS, DOWN_HARD_SECONDS) values (:id, to_timestamp_tz(:dayAndHour, 'YYYY-MM-DD HH24 TZD'), :fourUp, :threeUp, :twoUp, :oneUp, :anyUp, :allUp, :downHard)", resultClass
                = OpMultiplicityHour.class)})
public class OpMultiplicityHour implements Serializable, HourEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name = "MultiplicityHourId", sequenceName = "OP_MULTIPLICITY_HOUR_ID", allocationSize
            = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MultiplicityHourId")
    @Basic(optional = false)
    @NotNull
    @Column(name = "OP_MULTIPLICITY_HOUR_ID", nullable = false, precision = 38, scale
            = 0)
    private BigInteger opMultiplicityHourId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "DAY_AND_HOUR", nullable = false, insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dayAndHour;
    @Basic(optional = false)
    @NotNull
    @Column(name = "ONE_HALL_UP_SECONDS", nullable = false)
    @Max(value = 3600, message = "ONE UP must be less than or equal to 1 hour")
    @Min(value = 0, message = "ONE UP must be greater than or equal to 0")
    private short oneHallUpSeconds;
    @Basic(optional = false)
    @NotNull
    @Column(name = "TWO_HALL_UP_SECONDS", nullable = false)
    @Max(value = 3600, message = "TWO UP must be less than or equal to 1 hour")
    @Min(value = 0, message = "TWO UP must be greater than or equal to 0")
    private short twoHallUpSeconds;
    @Basic(optional = false)
    @NotNull
    @Column(name = "THREE_HALL_UP_SECONDS", nullable = false)
    @Max(value = 3600, message = "THREE UP must be less than or equal to 1 hour")
    @Min(value = 0, message = "THREE UP must be greater than or equal to 0")
    private short threeHallUpSeconds;
    @Basic(optional = false)
    @NotNull
    @Column(name = "FOUR_HALL_UP_SECONDS", nullable = false)
    @Max(value = 3600, message = "FOUR UP must be less than or equal to 1 hour")
    @Min(value = 0, message = "FOUR UP must be greater than or equal to 0")
    private short fourHallUpSeconds;
    @Basic(optional = false)
    @NotNull
    @Column(name = "ANY_HALL_UP_SECONDS", nullable = false)
    @Max(value = 3600, message = "ANY UP must be less than or equal to 1 hour")
    @Min(value = 0, message = "ANY UP must be greater than or equal to 0")
    private short anyHallUpSeconds;
    @Basic(optional = false)
    @NotNull
    @Column(name = "ALL_HALL_UP_SECONDS", nullable = false)
    @Max(value = 3600, message = "ALL UP must be less than or equal to 1 hour")
    @Min(value = 0, message = "ALL UP must be greater than or equal to 0")
    private short allHallUpSeconds;
    @Basic(optional = false)
    @NotNull
    @Column(name = "DOWN_HARD_SECONDS", nullable = false)
    @Max(value = 3600, message = "DOWN HARD must be less than or equal to 1 hour")
    @Min(value = 0, message = "DOWN HARD must be greater than or equal to 0")
    private short downHardSeconds;
    @Transient
    private DataSource source;

    public BigInteger getOpMultiplicityHourId() {
        return opMultiplicityHourId;
    }

    public void setOpMultiplicityHourId(BigInteger opMultiplicityHourId) {
        this.opMultiplicityHourId = opMultiplicityHourId;
    }

    @Override
    public Date getDayAndHour() {
        return dayAndHour;
    }

    @Override
    public void setDayAndHour(Date dayAndHour) {
        this.dayAndHour = dayAndHour;
    }

    public short getOneHallUpSeconds() {
        return oneHallUpSeconds;
    }

    public void setOneHallUpSeconds(short oneHallUpSeconds) {
        this.oneHallUpSeconds = oneHallUpSeconds;
    }

    public short getTwoHallUpSeconds() {
        return twoHallUpSeconds;
    }

    public void setTwoHallUpSeconds(short twoHallUpSeconds) {
        this.twoHallUpSeconds = twoHallUpSeconds;
    }

    public short getThreeHallUpSeconds() {
        return threeHallUpSeconds;
    }

    public void setThreeHallUpSeconds(short threeHallUpSeconds) {
        this.threeHallUpSeconds = threeHallUpSeconds;
    }

    public short getFourHallUpSeconds() {
        return fourHallUpSeconds;
    }

    public void setFourHallUpSeconds(short fourHallUpSeconds) {
        this.fourHallUpSeconds = fourHallUpSeconds;
    }

    public short getAnyHallUpSeconds() {
        return anyHallUpSeconds;
    }

    public void setAnyHallUpSeconds(short anyHallUpSeconds) {
        this.anyHallUpSeconds = anyHallUpSeconds;
    }

    public short getAllHallUpSeconds() {
        return allHallUpSeconds;
    }

    public void setAllHallUpSeconds(short allHallUpSeconds) {
        this.allHallUpSeconds = allHallUpSeconds;
    }

    public short getDownHardSeconds() {
        return downHardSeconds;
    }

    public void setDownHardSeconds(short downHardSeconds) {
        this.downHardSeconds = downHardSeconds;
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
        hash += (opMultiplicityHourId != null ? opMultiplicityHourId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof OpMultiplicityHour)) {
            return false;
        }
        OpMultiplicityHour other = (OpMultiplicityHour) object;
        return (this.opMultiplicityHourId != null || other.opMultiplicityHourId == null)
                && (this.opMultiplicityHourId == null || this.opMultiplicityHourId.equals(
                other.opMultiplicityHourId));
    }

    @Override
    public String toString() {
        return "org.jlab.webapp.persistence.entity.OpAggHallHour[ opMultiplicityHourId="
                + opMultiplicityHourId + " ]";
    }

}
