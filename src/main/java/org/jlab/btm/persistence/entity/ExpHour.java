package org.jlab.btm.persistence.entity;

import org.hibernate.envers.Audited;
import org.jlab.btm.persistence.projection.HallHour;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.enumeration.Hall;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

/**
 * An experimenter hall hour.
 *
 * @author ryans
 */
@Entity
@Table(name = "EXP_HOUR", schema = "BTM_OWNER", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"HALL", "DAY_AND_HOUR"}),
        @UniqueConstraint(columnNames = {"HALL", "EXP_HOUR_ID"})})
@NamedQueries({
        @NamedQuery(name = "ExpHour.findByExpHourId", query = "SELECT e FROM ExpHour e WHERE e.expHourId = :expHallHourId")})
@NamedNativeQueries({
        @NamedNativeQuery(name = "ExpHour.findByHallAndDayAndHourNATIVE", query = "SELECT * FROM EXP_HOUR WHERE hall = :hall AND to_char(DAY_AND_HOUR, 'YYYY-MM-DD HH24 TZD') = :dayAndHour", resultClass = ExpHour.class),
        @NamedNativeQuery(name = "ExpHour.insertNATIVE", query = "INSERT into EXP_HOUR (EXP_HOUR_ID, HALL, DAY_AND_HOUR, ABU_SECONDS, BANU_SECONDS, BNA_SECONDS, ACC_SECONDS, ER_SECONDS, PCC_SECONDS, UED_SECONDS, OFF_SECONDS, REMARK) values (:id, :hall, to_timestamp_tz(:dayAndHour, 'YYYY-MM-DD HH24 TZD'), :abu, :banu, :bna, :acc, :er, :pcc, :ued, :off, :remark)", resultClass = ExpHour.class)})
public class ExpHour extends HallHour {
    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name = "ExpHourId", sequenceName = "EXP_HOUR_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ExpHourId")
    @Basic(optional = false)
    @Column(name = "EXP_HOUR_ID", nullable = false, precision = 38, scale = 0)
    private BigInteger expHourId;
    @Basic(optional = false)
    @Column(name = "HALL", nullable = false, length = 1, columnDefinition = "char(1)")
    @NotNull
    @Enumerated(EnumType.STRING)
    private Hall hall;
    @Basic(optional = false)
    @NotNull
    @Column(name = "DAY_AND_HOUR", nullable = false, insertable = false, updatable = false, columnDefinition = "timestamp(0) with local timezone")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dayAndHour;
    @Basic(optional = false)
    @Column(name = "ABU_SECONDS", nullable = false)
    @NotNull
    @Min(0)
    @Max(value = 3600, message = "{org.jlab.bta.maxTime}")
    private short abuSeconds;
    @Basic(optional = false)
    @Column(name = "BANU_SECONDS", nullable = false)
    @NotNull
    @Min(0)
    @Max(value = 3600, message = "{org.jlab.bta.maxTime}")
    private short banuSeconds;
    @Basic(optional = false)
    @Column(name = "BNA_SECONDS", nullable = false)
    @NotNull
    @Min(0)
    @Max(value = 3600, message = "{org.jlab.bta.maxTime}")
    private short bnaSeconds;
    @Basic(optional = false)
    @Column(name = "ACC_SECONDS", nullable = false)
    @NotNull
    @Min(0)
    @Max(value = 3600, message = "{org.jlab.bta.maxTime}")
    private short accSeconds;
    @Basic(optional = false)
    @Column(name = "ER_SECONDS", nullable = false)
    @NotNull
    @Min(0)
    @Max(value = 3600, message = "{org.jlab.bta.maxTime}")
    private short erSeconds;
    @Basic(optional = false)
    @Column(name = "PCC_SECONDS", nullable = false)
    @NotNull
    @Min(0)
    @Max(value = 3600, message = "{org.jlab.bta.maxTime}")
    private short pccSeconds;
    @Basic(optional = false)
    @Column(name = "UED_SECONDS", nullable = false)
    @NotNull
    @Min(0)
    @Max(value = 3600, message = "{org.jlab.bta.maxTime}")
    private short uedSeconds;
    @Basic(optional = false)
    @Column(name = "OFF_SECONDS", nullable = false)
    @NotNull
    @Min(0)
    @Max(value = 3600, message = "{org.jlab.bta.maxTime}")
    private short offSeconds;
    @Column(name = "REMARK", length = 128)
    @Size(max = 128)
    private String remark;

    public ExpHour() {
    }

    public ExpHour(BigInteger expHourId) {
        this.expHourId = expHourId;
    }

    /**
     * Copies the time accounting data plus the remark
     * from the specified experimenter hall hour into this one.
     *
     * @param other the experimenter hall hour.
     */
    public void copyAccounting(ExpHour other) {
        this.setAbuSeconds(other.getAbuSeconds());
        this.setBanuSeconds(other.getBanuSeconds());
        this.setBnaSeconds(other.getBnaSeconds());
        this.setAccSeconds(other.getAccSeconds());
        this.setErSeconds(other.getErSeconds());
        this.setPccSeconds(other.getPccSeconds());
        this.setUedSeconds(other.getUedSeconds());
        this.setOffSeconds(other.getOffSeconds());
        this.setRemark(other.getRemark());
    }

    public BigInteger getExpHourId() {
        return expHourId;
    }

    public void setExpHourId(BigInteger expHourId) {
        this.expHourId = expHourId;
    }

    @Override
    public Hall getHall() {
        return hall;
    }

    @Override
    public void setHall(Hall hall) {
        this.hall = hall;
    }

    @Override
    public Date getDayAndHour() {
        return dayAndHour;
    }

    @Override
    public void setDayAndHour(Date dayAndHour) {
        this.dayAndHour = dayAndHour;
    }

    public short getAbuSeconds() {
        return abuSeconds;
    }

    public void setAbuSeconds(short abuSeconds) {
        this.abuSeconds = abuSeconds;
    }

    public short getBanuSeconds() {
        return banuSeconds;
    }

    public void setBanuSeconds(short banuSeconds) {
        this.banuSeconds = banuSeconds;
    }

    public short getBnaSeconds() {
        return bnaSeconds;
    }

    public void setBnaSeconds(short bnaSeconds) {
        this.bnaSeconds = bnaSeconds;
    }

    public short getAccSeconds() {
        return accSeconds;
    }

    public void setAccSeconds(short accSeconds) {
        this.accSeconds = accSeconds;
    }

    public short getErSeconds() {
        return erSeconds;
    }

    public void setErSeconds(short erSeconds) {
        this.erSeconds = erSeconds;
    }

    public short getPccSeconds() {
        return pccSeconds;
    }

    public void setPccSeconds(short pccSeconds) {
        this.pccSeconds = pccSeconds;
    }

    public short getUedSeconds() {
        return uedSeconds;
    }

    public void setUedSeconds(short uedSeconds) {
        this.uedSeconds = uedSeconds;
    }

    public short getOffSeconds() {
        return offSeconds;
    }

    public void setOffSeconds(short offSeconds) {
        this.offSeconds = offSeconds;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * Calculates the sum of the accelerator mutual exclusive set.
     * <p>
     * The accelerator mutual exclusive set consists of ABU, BANU, BNA, ACC, and
     * OFF.
     *
     * @return the sum.
     */
    public int calculateAcceleratorTotal() {
        return this.getAbuSeconds() + this.getBanuSeconds() + this.getBnaSeconds() + this.getAccSeconds() + this.getOffSeconds();
    }

    /**
     * Calculates the sum of the experimenter mutual exclusive set.
     * <p>
     * The experimenter mutual exclusive set consists of ER, PCC, UED, and OFF.
     *
     * @return the sum.
     */
    public int calculateExperimentTotal() {
        return this.getErSeconds() + this.getPccSeconds() + this.getUedSeconds() + this.getOffSeconds();
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param object the reference object with which to compare.
     * @return true if this object is the same; false otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ExpHour)) {
            return false;
        }
        return super.equals(object);
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {

        String builder = "ID: " +
                this.getExpHourId() +
                ", Hour: " +
                TimeUtil.formatDatabaseDateTimeTZ(this.getDayAndHour()) +
                ", Hall: " +
                this.getHall() +
                ", ABU: " +
                abuSeconds +
                ", BANU: " +
                banuSeconds +
                ", BNA: " +
                bnaSeconds +
                ", ACC: " +
                accSeconds +
                ", ER: " +
                erSeconds +
                ", PCC: " +
                pccSeconds +
                ", UED: " +
                uedSeconds +
                ", Off: " +
                offSeconds;

        return builder;
    }
}
