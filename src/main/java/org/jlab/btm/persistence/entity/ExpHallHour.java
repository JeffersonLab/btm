package org.jlab.btm.persistence.entity;

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
@Table(name = "EXP_HALL_HOUR", schema = "JBTA_OWNER", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"HALL", "DAY_AND_HOUR"}),
        @UniqueConstraint(columnNames = {"HALL", "EXP_HALL_HOUR_ID"})})
@NamedQueries({
        @NamedQuery(name = "ExpHallHour.findByExpHallHourId", query = "SELECT e FROM ExpHallHour e WHERE e.expHallHourId = :expHallHourId"),
        @NamedQuery(name = "ExpHallHour.findByHallAndDayAndHour", query = "SELECT e FROM ExpHallHour e WHERE e.hall = :hall AND e.dayAndHourCal = :dayAndHourCal"),
        @NamedQuery(name = "ExpHallHour.findByHallAndHourRange", query = "SELECT e FROM ExpHallHour e WHERE e.hall = :hall AND e.dayAndHourCal BETWEEN :startDayAndHourCal AND :endDayAndHourCal ORDER BY e.dayAndHourCal ASC")})
@NamedNativeQueries({
        @NamedNativeQuery(name = "ExpHallHour.findByHallAndDayAndHourNATIVE", query = "SELECT * FROM EXP_HALL_HOUR WHERE hall = :hall AND to_char(DAY_AND_HOUR, 'YYYY-MM-DD HH24 TZD') = :dayAndHour", resultClass = ExpHallHour.class),
        @NamedNativeQuery(name = "ExpHallHour.findByHallAndHourRangeNATIVE", query = "SELECT e.* FROM EXP_HALL_HOUR e WHERE e.hall = :hall AND e.DAY_AND_HOUR BETWEEN :startDayAndHour AND :endDayAndHour ORDER BY e.DAY_AND_HOUR ASC", resultClass = ExpHallHour.class),
        @NamedNativeQuery(name = "ExpHallHour.insertNATIVE", query = "INSERT into EXP_HALL_HOUR (EXP_HALL_HOUR_ID, HALL, DAY_AND_HOUR, ABU_SECONDS, BANU_SECONDS, BNA_SECONDS, ACC_SECONDS, ER_SECONDS, PCC_SECONDS, UED_SECONDS, SCHED_SECONDS, STUDIES_SECONDS, OFF_SECONDS, REMARK) values (:id, :hall, to_timestamp_tz(:dayAndHour, 'YYYY-MM-DD HH24 TZD'), :abu, :banu, :bna, :acc, :er, :pcc, :ued, :sched, :studies, :off, :remark)", resultClass = ExpHallHour.class),
        @NamedNativeQuery(name = "ExpHallHour.updateNATIVE", query = "UPDATE EXP_HALL_HOUR SET ABU_SECONDS = :abu, BANU_SECONDS = :banu, BNA_SECONDS = :bna, ACC_SECONDS = :acc, ER_SECONDS = :er, PCC_SECONDS = :pcc, UED_SECONDS = :ued, SCHED_SECONDS = :sched, STUDIES_SECONDS = :studies, OFF_SECONDS = :off, REMARK = :remark WHERE EXP_HALL_HOUR_ID = :id", resultClass = ExpHallHour.class)})
public class ExpHallHour extends HallHour {
    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name = "ExpHallHourId", sequenceName = "EXP_HALL_HOUR_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ExpHallHourId")
    @Basic(optional = false)
    @Column(name = "EXP_HALL_HOUR_ID", nullable = false, precision = 38, scale = 0)
    private BigInteger expHallHourId;
    @Basic(optional = false)
    @Column(name = "HALL", nullable = false, length = 1, columnDefinition = "char(1)")
    @NotNull
    @Enumerated(EnumType.STRING)
    private Hall hall;

    @Transient
    private Date dayAndHour = null;

    @Basic(optional = false)
    @Column(name = "DAY_AND_HOUR", nullable = false, insertable = false, updatable = false, length = 7, columnDefinition = "timestamp(0) with local timezone")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar dayAndHourCal;

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
    @Column(name = "SCHED_SECONDS", nullable = false)
    @NotNull
    @Min(0)
    @Max(value = 3600, message = "{org.jlab.bta.maxTime}")
    private short schedSeconds;
    @Basic(optional = false)
    @Column(name = "STUDIES_SECONDS", nullable = false)
    @NotNull
    @Min(0)
    @Max(value = 3600, message = "{org.jlab.bta.maxTime}")
    private short studiesSeconds;
    @Basic(optional = false)
    @Column(name = "OFF_SECONDS", nullable = false)
    @NotNull
    @Min(0)
    @Max(value = 3600, message = "{org.jlab.bta.maxTime}")
    private short offSeconds;
    @Column(name = "REMARK", length = 128)
    @Size(max = 128)
    private String remark;

    public ExpHallHour() {
    }

    public ExpHallHour(BigInteger expHallHourId) {
        this.expHallHourId = expHallHourId;
    }

    public ExpHallHour(BigInteger expHallHourId, Hall hall, Calendar dayAndHourCal, short abuSeconds, short banuSeconds, short bnaSeconds, short accSeconds, short erSeconds, short pccSeconds, short uedSeconds, short schedSeconds, short studiesSeconds, short offSeconds) {
        this.expHallHourId = expHallHourId;
        this.hall = hall;
        this.dayAndHourCal = dayAndHourCal;
        this.abuSeconds = abuSeconds;
        this.banuSeconds = banuSeconds;
        this.bnaSeconds = bnaSeconds;
        this.accSeconds = accSeconds;
        this.erSeconds = erSeconds;
        this.pccSeconds = pccSeconds;
        this.uedSeconds = uedSeconds;
        this.schedSeconds = schedSeconds;
        this.studiesSeconds = studiesSeconds;
        this.offSeconds = offSeconds;
    }

    /**
     * Copies the time accounting data plus the remark
     * from the specified experimenter hall hour into this one.
     *
     * @param other the experimenter hall hour.
     */
    public void copyAccounting(ExpHallHour other) {
        this.setAbuSeconds(other.getAbuSeconds());
        this.setBanuSeconds(other.getBanuSeconds());
        this.setBnaSeconds(other.getBnaSeconds());
        this.setAccSeconds(other.getAccSeconds());
        this.setErSeconds(other.getErSeconds());
        this.setPccSeconds(other.getPccSeconds());
        this.setUedSeconds(other.getUedSeconds());
        this.setSchedSeconds(other.getSchedSeconds());
        this.setStudiesSeconds(other.getStudiesSeconds());
        this.setOffSeconds(other.getOffSeconds());
        this.setRemark(other.getRemark());
    }

    public BigInteger getExpHallHourId() {
        return expHallHourId;
    }

    public void setExpHallHourId(BigInteger expHallHourId) {
        this.expHallHourId = expHallHourId;
    }

    @Override
    public Hall getHall() {
        return hall;
    }

    @Override
    public void setHall(Hall hall) {
        this.hall = hall;
    }

    public Calendar getDayAndHourCal() {
        return dayAndHourCal;
    }

    public void setDayAndHourCal(Calendar dayAndHourCal) {
        this.dayAndHourCal = dayAndHourCal;
    }

    @Override
    public Date getDayAndHour() {
        if (dayAndHour == null && dayAndHourCal != null) {
            dayAndHour = dayAndHourCal.getTime();
        }
        return dayAndHour;
    }

    @Override
    public void setDayAndHour(Date dayAndHour) {
        this.dayAndHour = dayAndHour;
        Calendar cal = Calendar.getInstance();
        cal.setTime(dayAndHour);
        setDayAndHourCal(cal);
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

    public short getSchedSeconds() {
        return schedSeconds;
    }

    public void setSchedSeconds(short schedSeconds) {
        this.schedSeconds = schedSeconds;
    }

    public short getStudiesSeconds() {
        return studiesSeconds;
    }

    public void setStudiesSeconds(short studiesSeconds) {
        this.studiesSeconds = studiesSeconds;
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
        if (!(object instanceof ExpHallHour)) {
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
        StringBuilder builder = new StringBuilder();

        builder.append("ID: ");
        builder.append(this.getExpHallHourId());
        builder.append(", Hour: ");
        builder.append(TimeUtil.formatDatabaseDateTimeTZ(this.getDayAndHour()));
        builder.append(", Hall: ");
        builder.append(this.getHall());
        builder.append(", ABU: ");
        builder.append(abuSeconds);
        builder.append(", BANU: ");
        builder.append(banuSeconds);
        builder.append(", BNA: ");
        builder.append(bnaSeconds);
        builder.append(", ACC: ");
        builder.append(accSeconds);
        builder.append(", ER: ");
        builder.append(erSeconds);
        builder.append(", PCC: ");
        builder.append(pccSeconds);
        builder.append(", UED: ");
        builder.append(uedSeconds);
        builder.append(", Scheduled: ");
        builder.append(schedSeconds);
        builder.append(", Studies: ");
        builder.append(studiesSeconds);
        builder.append(", Off: ");
        builder.append(offSeconds);

        return builder.toString();
    }
}
