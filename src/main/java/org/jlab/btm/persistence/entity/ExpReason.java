package org.jlab.btm.persistence.entity;

import org.jlab.smoothness.persistence.enumeration.Hall;

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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * An experimenter hall reason not ready.
 *
 * @author ryans
 */
@Entity
@Table(name = "EXP_REASON", schema = "BTM_OWNER", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"HALL", "NAME"}),
        @UniqueConstraint(columnNames = {"HALL", "EXP_REASON_ID"})})
@NamedQueries({
        @NamedQuery(name = "ExpHallReason.findByExpHallReasonId", query = "SELECT e FROM ExpReason e WHERE e.expHallReasonId = :expHallReasonId"),
        @NamedQuery(name = "ExpHallReason.findByHallAndName", query = "SELECT e FROM ExpReason e WHERE e.hall = :hall AND e.name = :name"),
        @NamedQuery(name = "ExpHallReason.findByHallAndActive", query = "SELECT e FROM ExpReason e WHERE e.hall = :hall AND e.active = :active")})
public class ExpReason implements Comparable<ExpReason>, Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name="ExpHallReasonId", sequenceName="EXP_REASON_ID", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="ExpHallReasonId")
    @Basic(optional = false)
    @Column(name = "EXP_REASON_ID", nullable = false, precision = 38, scale = 0)
    private BigInteger expHallReasonId;
    @Basic(optional = false)
    @Column(name = "HALL", nullable = false, length = 1, columnDefinition = "char(1)")
    @Enumerated(EnumType.STRING)
    @NotNull
    private Hall hall;
    @Basic(optional = false)
    @Column(name = "NAME", nullable = false, length = 64)
    @NotNull
    @Size(max=64)
    private String name;
    @Basic(optional = false)
    @Column(name = "ACTIVE", nullable = false, length = 1, columnDefinition = "char(1)")
    @NotNull
    private boolean active;

    public ExpReason() {
    }

    public ExpReason(BigInteger expHallReasonId) {
        this.expHallReasonId = expHallReasonId;
    }

    public ExpReason(BigInteger expHallReasonId, Hall hall, String name, boolean active) {
        this.expHallReasonId = expHallReasonId;
        this.hall = hall;
        this.name = name;
        this.active = active;
    }

    public BigInteger getExpHallReasonId() {
        return expHallReasonId;
    }

    public void setExpHallReasonId(BigInteger expHallReasonId) {
        this.expHallReasonId = expHallReasonId;
    }

    public Hall getHall() {
        return hall;
    }

    public void setHall(Hall hall) {
        this.hall = hall;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (expHallReasonId != null ? expHallReasonId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ExpReason)) {
            return false;
        }
        ExpReason other = (ExpReason) object;
        if ((this.expHallReasonId == null && other.expHallReasonId != null) || (this.expHallReasonId != null && !this.expHallReasonId.equals(other.expHallReasonId))) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(ExpReason o) {
        int result = this.getHall().compareTo(o.getHall());

        if(result == 0) {
            result = this.getName().compareTo(o.getName());
        }

        return result;
    }

    @Override
    public String toString() {
        return "org.jlab.btm.entity.ExpHallReason[expHallReasonId=" + expHallReasonId + "]";
    }
}
