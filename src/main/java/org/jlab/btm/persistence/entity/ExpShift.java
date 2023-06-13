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
@Table(name = "EXP_SHIFT", schema = "BTM_OWNER", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"HALL", "START_DAY_AND_HOUR"})})
@NamedQueries({
        @NamedQuery(name = "ExpShift.findByExpShiftId", query = "SELECT e FROM ExpShift e WHERE e.expShiftId = :expShiftId"),
        @NamedQuery(name = "ExpShift.findByHallAndStartDayAndHour", query = "SELECT e FROM ExpShift e WHERE e.expProgram.hall = :hall AND e.startDayAndHour = :startDayAndHour")})
public class ExpShift implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name = "ExpShiftId", sequenceName = "EXP_SHIFT_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ExpShiftId")
    @Basic(optional = false)
    @Column(name = "EXP_SHIFT_ID", nullable = false, precision = 38, scale = 0)
    private BigInteger expShiftId;
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
    @JoinColumn(name = "EXP_PROGRAM_ID", referencedColumnName = "EXP_PROGRAM_ID", nullable = false)
    @ManyToOne(optional = false)
    private ExpProgram expProgram;

    public ExpShift() {
    }

    public BigInteger getExpShiftId() {
        return expShiftId;
    }

    public Date getStartDayAndHour() {
        return startDayAndHour;
    }

    public void setStartDayAndHour(Date startDayAndHour) {
        this.startDayAndHour = startDayAndHour;
    }

    public void setExpShiftId(BigInteger expShiftId) {
        this.expShiftId = expShiftId;
    }

    public Hall getHall() {
        return hall;
    }

    public void setHall(Hall hall) {
        this.hall = hall;
    }

    public ExpProgram getExpProgram() {
        return expProgram;
    }

    public void setExpProgram(ExpProgram expProgram) {
        this.expProgram = expProgram;
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
        hash += (expShiftId != null ? expShiftId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ExpShift)) {
            return false;
        }
        ExpShift other = (ExpShift) object;
        return (this.expShiftId != null || other.expShiftId == null) && (this.expShiftId == null || this.expShiftId.equals(other.expShiftId));
    }

    @Override
    public String toString() {
        return "org.jlab.bta.entity.ExpShift[expHallShiftId=" + expShiftId + "]";
    }
}
