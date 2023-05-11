package org.jlab.btm.persistence.entity;

import org.jlab.btm.persistence.enumeration.DataSource;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author ryans
 */
@Entity
@Table(name = "OP_SHIFT", schema = "BTM_OWNER", uniqueConstraints
        = {
        @UniqueConstraint(columnNames = {"START_DAY_AND_HOUR"})})
@NamedQueries({
        @NamedQuery(name = "OpShift.findAll", query
                = "SELECT o FROM OpShift o")})
public class OpShift implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name = "OpShiftId", sequenceName = "OP_SHIFT_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "OpShiftId")
    @Basic(optional = false)
    @NotNull
    @Column(name = "OP_SHIFT_ID", nullable = false, precision = 38, scale = 0)
    private BigDecimal opShiftId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "START_DAY_AND_HOUR", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDayAndHour;
    @Size(max = 64)
    @Column(name = "CREW_CHIEF", length = 64)
    private String crewChief;
    @Size(max = 256)
    @Column(length = 256)
    private String operators;
    @Size(max = 2048)
    @Column(length = 2048)
    private String remark;
    @Size(max = 64)
    @Column(length = 64)
    private String program;
    @Size(max = 64)
    @Column(name = "PROGRAM_DEPUTY", length = 64)
    private String programDeputy;
    @Transient
    private DataSource source;

    public OpShift() {
    }

    public OpShift(BigDecimal opShiftId) {
        this.opShiftId = opShiftId;
    }

    public OpShift(BigDecimal opShiftId, Date startDayAndHour) {
        this.opShiftId = opShiftId;
        this.startDayAndHour = startDayAndHour;
    }

    public BigDecimal getOpShiftId() {
        return opShiftId;
    }

    public void setOpShiftId(BigDecimal opShiftId) {
        this.opShiftId = opShiftId;
    }

    public Date getStartDayAndHour() {
        return startDayAndHour;
    }

    public void setStartDayAndHour(Date startDayAndHour) {
        this.startDayAndHour = startDayAndHour;
    }

    public String getCrewChief() {
        return crewChief;
    }

    public void setCrewChief(String crewChief) {
        this.crewChief = crewChief;
    }

    public String getOperators() {
        return operators;
    }

    public void setOperators(String operators) {
        this.operators = operators;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getProgramDeputy() {
        return programDeputy;
    }

    public void setProgramDeputy(String programDeputy) {
        this.programDeputy = programDeputy;
    }

    public DataSource getSource() {
        return source;
    }

    public void setSource(DataSource source) {
        this.source = source;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (opShiftId != null ? opShiftId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof OpShift)) {
            return false;
        }
        OpShift other = (OpShift) object;
        return (this.opShiftId != null || other.opShiftId == null) && (this.opShiftId == null || this.opShiftId.equals(other.opShiftId));
    }

    @Override
    public String toString() {
        return "org.jlab.webapp.persistence.entity.OpShift[ opShiftId=" + opShiftId + " ]";
    }

}
