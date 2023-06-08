package org.jlab.btm.persistence.entity;

import java.io.Serializable;
import java.math.BigInteger;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * An experimenter hall hour reason not ready time.
 *
 * @author ryans
 */
@Entity
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@Table(name = "EXP_HOUR_REASON_TIME", schema = "BTM_OWNER", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"HALL", "EXP_HOUR_ID", "EXP_REASON_ID"})})
@NamedQueries({
        @NamedQuery(name = "ExpHallHourReasonTime.findByExpHallHourReasonTimeId", query = "SELECT e FROM ExpHourReasonTime e WHERE e.expHallHourReasonTimeId = :expHallHourReasonTimeId"),
        @NamedQuery(name = "ExpHallHourReasonTime.findByHallAndHourRange", query = "SELECT e FROM ExpHourReasonTime e WHERE e.hall = :hall AND e.expHour.dayAndHourCal BETWEEN :startDayAndHourCal AND :endDayAndHourCal ORDER BY e.expHour.dayAndHourCal ASC"),
        @NamedQuery(name = "ExpHallHourReasonTime.sumByExpHallHourId", query = "SELECT NVL(SUM(e.seconds), 0) FROM ExpHourReasonTime e WHERE e.expHour.expHallHourId = :expHallHourId")})
public class ExpHourReasonTime implements Comparable<ExpHourReasonTime>, Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name="ExpHallHourReasonTimeId", sequenceName="EXP_HOUR_REASON_TIME_ID", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="ExpHallHourReasonTimeId")
    @Basic(optional = false)
    @Column(name = "EXP_HOUR_REASON_TIME_ID", nullable = false, precision = 38, scale = 0)
    private BigInteger expHallHourReasonTimeId;
    @Basic(optional = false)
    @Column(name = "HALL", nullable = false, length = 1, columnDefinition = "char(1)")
    @NotNull
    @Enumerated(EnumType.STRING)
    private Hall hall;
    @Basic(optional = false)
    @Column(name = "SECONDS", nullable = false)
    @NotNull
    @Min(value = 1, message = "{org.jlab.bta.reasonMinTime}")
    @Max(value = 3600, message = "{org.jlab.bta.maxTime}")
    private short seconds;
    @NotNull
    @JoinColumn(name = "EXP_HOUR_ID", referencedColumnName = "EXP_HOUR_ID", nullable = false)
    @ManyToOne(optional = false)
    private ExpHour expHour;
    @NotNull
    @JoinColumn(name = "EXP_REASON_ID", referencedColumnName = "EXP_REASON_ID", nullable = false)
    @ManyToOne(optional = false)
    private ExpReason expReason;

    public ExpHourReasonTime() {
    }

    public ExpHourReasonTime(BigInteger expHallHourReasonTimeId) {
        this.expHallHourReasonTimeId = expHallHourReasonTimeId;
    }

    public ExpHourReasonTime(BigInteger expHallHourReasonTimeId, Hall hall, short seconds) {
        this.expHallHourReasonTimeId = expHallHourReasonTimeId;
        this.hall = hall;
        this.seconds = seconds;
    }

    public BigInteger getExpHallHourReasonTimeId() {
        return expHallHourReasonTimeId;
    }

    public void setExpHallHourReasonTimeId(BigInteger expHallHourReasonTimeId) {
        this.expHallHourReasonTimeId = expHallHourReasonTimeId;
    }

    public Hall getHall() {
        return hall;
    }

    public void setHall(Hall hall) {
        this.hall = hall;
    }

    public short getSeconds() {
        return seconds;
    }

    public void setSeconds(short seconds) {
        this.seconds = seconds;
    }

    public ExpHour getExpHallHour() {
        return expHour;
    }

    public void setExpHallHour(ExpHour expHour) {
        this.expHour = expHour;
    }

    public ExpReason getExpHallReason() {
        return expReason;
    }

    public void setExpHallReason(ExpReason expReason) {
        this.expReason = expReason;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (expHallHourReasonTimeId != null ? expHallHourReasonTimeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ExpHourReasonTime)) {
            return false;
        }
        ExpHourReasonTime other = (ExpHourReasonTime) object;
        if ((this.expHallHourReasonTimeId == null && other.expHallHourReasonTimeId != null) || (this.expHallHourReasonTimeId != null && !this.expHallHourReasonTimeId.equals(other.expHallHourReasonTimeId))) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(ExpHourReasonTime o) {
        int result = this.getHall().compareTo(o.getHall());

        if(result == 0) {
            result = this.getExpHallHour().getDayAndHour().compareTo(o.getExpHallHour().getDayAndHour());
        }

        return result;
    }

    @Override
    public String toString() {
        return "org.jlab.btm.entity.ExpHallHourReasonTime[expHallHourReasonTimeId=" + expHallHourReasonTimeId + "]";
    }
}
