package org.jlab.btm.persistence.entity;

import org.jlab.smoothness.persistence.enumeration.Hall;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigInteger;

/**
 * An experimenter hall shift purpose.
 *
 * @author ryans
 */
@Entity
@Table(name = "EXP_HALL_SHIFT_PURPOSE", schema = "BTM_OWNER", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"HALL", "NAME"}),
        @UniqueConstraint(columnNames = {"HALL", "EXP_HALL_SHIFT_PURPOSE_ID"})})
@NamedQueries({
        @NamedQuery(name = "ExpHallShiftPurpose.findByExpHallShiftPurposeId", query = "SELECT e FROM ExpHallShiftPurpose e WHERE e.expHallShiftPurposeId = :expHallShiftPurposeId"),
        @NamedQuery(name = "ExpHallShiftPurpose.findByHallAndName", query = "SELECT e FROM ExpHallShiftPurpose e WHERE e.hall = :hall AND e.name = :name"),
        @NamedQuery(name = "ExpHallShiftPurpose.findByHallAndActive", query = "SELECT e FROM ExpHallShiftPurpose e WHERE e.hall = :hall AND e.active = :active"),
        @NamedQuery(name = "ExpHallShiftPurpose.findByHallAndExperiment", query = "SELECT e FROM ExpHallShiftPurpose e WHERE e.hall = :hall AND e.experiment = :experiment")})
public class ExpHallShiftPurpose implements Comparable<ExpHallShiftPurpose>, Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name = "ExpHallShiftPurposeId", sequenceName = "EXP_HALL_SHIFT_PURPOSE_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ExpHallShiftPurposeId")
    @Basic(optional = false)
    @Column(name = "EXP_HALL_SHIFT_PURPOSE_ID", nullable = false, precision = 38, scale = 0)
    private BigInteger expHallShiftPurposeId;
    @Basic(optional = false)
    @Column(name = "HALL", nullable = false, length = 1, columnDefinition = "char(1)")
    @Enumerated(EnumType.STRING)
    @NotNull
    private Hall hall;
    @Basic(optional = false)
    @Column(name = "NAME", nullable = false, length = 64)
    @NotNull
    @Size(max = 64)
    private String name;
    @Basic(optional = true)
    @Column(name = "ALIAS", nullable = true, length = 64)
    @Size(max = 64)
    private String alias;
    @Basic(optional = true)
    @Column(name = "URL", nullable = true, length = 512)
    @Size(max = 512)
    private String url;
    @Basic(optional = false)
    @Column(name = "ACTIVE", nullable = false, length = 1, columnDefinition = "char(1)")
    @NotNull
    private boolean active;
    @Basic(optional = false)
    @Column(name = "EXPERIMENT", nullable = false, length = 1, columnDefinition = "char(1)")
    @NotNull
    private boolean experiment;

    public ExpHallShiftPurpose() {
    }

    public ExpHallShiftPurpose(BigInteger expHallShiftPurposeId) {
        this.expHallShiftPurposeId = expHallShiftPurposeId;
    }

    public ExpHallShiftPurpose(BigInteger expHallShiftPurposeId, Hall hall, String name, boolean active, boolean experiment) {
        this.expHallShiftPurposeId = expHallShiftPurposeId;
        this.hall = hall;
        this.name = name;
        this.active = active;
        this.experiment = experiment;
    }

    public BigInteger getExpHallShiftPurposeId() {
        return expHallShiftPurposeId;
    }

    public void setExpHallShiftPurposeId(BigInteger expHallShiftPurposeId) {
        this.expHallShiftPurposeId = expHallShiftPurposeId;
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

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isExperiment() {
        return experiment;
    }

    public void setExperiment(boolean experiment) {
        this.experiment = experiment;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (expHallShiftPurposeId != null ? expHallShiftPurposeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ExpHallShiftPurpose)) {
            return false;
        }
        ExpHallShiftPurpose other = (ExpHallShiftPurpose) object;
        return (this.expHallShiftPurposeId != null || other.expHallShiftPurposeId == null) && (this.expHallShiftPurposeId == null || this.expHallShiftPurposeId.equals(other.expHallShiftPurposeId));
    }

    @Override
    public int compareTo(ExpHallShiftPurpose o) {
        int result = this.getHall().compareTo(o.getHall());

        if (result == 0) {
            result = this.getName().compareTo(o.getName());
        }

        return result;
    }

    @Override
    public String toString() {

        String builder = "ID: " +
                this.getExpHallShiftPurposeId() +
                ", Hall: " +
                this.getHall() +
                ", Name: " +
                this.getName() +
                ", Experiment: " +
                this.isExperiment() +
                ", Active: " +
                this.isActive();

        return builder;
    }
}
