package org.jlab.btm.persistence.entity;

import org.jlab.smoothness.persistence.enumeration.Hall;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

/**
 * An experimenter hall shift.
 *
 * @author ryans
 */
@Entity
@Table(name = "EXP_HALL_SHIFT", schema = "BTM_OWNER", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"HALL", "START_DAY_AND_HOUR"})})
public class ExpHallShift implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name = "ExpHallShiftId", sequenceName = "EXP_HALL_SHIFT_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ExpHallShiftId")
    @Basic(optional = false)
    @Column(name = "EXP_HALL_SHIFT_ID", nullable = false, precision = 38, scale = 0)
    private BigInteger expHallShiftId;
    @Basic(optional = false)
    @Column(name = "HALL", nullable = false, length = 1, columnDefinition = "char(1)")
    @Enumerated(EnumType.STRING)
    @NotNull
    private Hall hall;
    @Basic(optional = false)
    @Column(name = "START_DAY_AND_HOUR", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date startDayAndHour;
    @Column(name = "LEADER", length = 64)
    @Size(max = 64)
    private String leader;
    @Column(name = "REMARK", length = 2048)
    @Size(max = 2048)
    private String remark;
    @Column(name = "WORKERS", length = 256)
    @Size(max = 256)
    private String workers;
    @NotNull
    @JoinColumn(name = "PURPOSE_ID", referencedColumnName = "EXP_HALL_SHIFT_PURPOSE_ID", nullable = false)
    @ManyToOne(optional = false)
    private ExpHallShiftPurpose expHallShiftPurpose;

    public ExpHallShift() {
    }

    public ExpHallShift(BigInteger expHallShiftId) {
        this.expHallShiftId = expHallShiftId;
    }

    public ExpHallShift(BigInteger expHallShiftId, Date startDayAndHour) {
        this.expHallShiftId = expHallShiftId;
        this.startDayAndHour = startDayAndHour;
    }

    public BigInteger getExpHallShiftId() {
        return expHallShiftId;
    }

    public void setExpHallShiftId(BigInteger expHallShiftId) {
        this.expHallShiftId = expHallShiftId;
    }

    public Hall getHall() {
        return hall;
    }

    public void setHall(Hall hall) {
        this.hall = hall;
    }

    public ExpHallShiftPurpose getExpHallShiftPurpose() {
        return expHallShiftPurpose;
    }

    public void setExpHallShiftPurpose(ExpHallShiftPurpose expHallShiftPurpose) {
        this.expHallShiftPurpose = expHallShiftPurpose;
    }

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getWorkers() {
        return workers;
    }

    public void setWorkers(String workers) {
        this.workers = workers;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (expHallShiftId != null ? expHallShiftId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ExpHallShift)) {
            return false;
        }
        ExpHallShift other = (ExpHallShift) object;
        return (this.expHallShiftId != null || other.expHallShiftId == null) && (this.expHallShiftId == null || this.expHallShiftId.equals(other.expHallShiftId));
    }

    @Override
    public String toString() {
        return "org.jlab.bta.entity.ExpHallShift[expHallShiftId=" + expHallShiftId + "]";
    }
}
