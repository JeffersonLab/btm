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
@Table(name = "EXP_SHIFT_PURPOSE", schema = "BTM_OWNER", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"HALL", "NAME"}),
        @UniqueConstraint(columnNames = {"HALL", "EXP_SHIFT_PURPOSE_ID"})})
@NamedQueries({
        @NamedQuery(name = "ExpShiftPurpose.findByExpShiftPurposeId", query = "SELECT e FROM ExpShiftPurpose e WHERE e.expShiftPurposeId = :expShiftPurposeId"),
        @NamedQuery(name = "ExpShiftPurpose.findByHallAndName", query = "SELECT e FROM ExpShiftPurpose e WHERE e.hall = :hall AND e.name = :name"),
        @NamedQuery(name = "ExpShiftPurpose.findByHallAndActive", query = "SELECT e FROM ExpShiftPurpose e WHERE e.hall = :hall AND e.active = :active"),
        @NamedQuery(name = "ExpShiftPurpose.findByHallAndExperiment", query = "SELECT e FROM ExpShiftPurpose e WHERE e.hall = :hall AND e.experiment = :experiment")})
public class ExpShiftPurpose implements Comparable<ExpShiftPurpose>, Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name = "ExpShiftPurposeId", sequenceName = "EXP_SHIFT_PURPOSE_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ExpShiftPurposeId")
    @Basic(optional = false)
    @Column(name = "EXP_SHIFT_PURPOSE_ID", nullable = false, precision = 38, scale = 0)
    private BigInteger expShiftPurposeId;
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

    public ExpShiftPurpose() {
    }

    public ExpShiftPurpose(BigInteger expShiftPurposeId) {
        this.expShiftPurposeId = expShiftPurposeId;
    }

    public ExpShiftPurpose(BigInteger expShiftPurposeId, Hall hall, String name, boolean active, boolean experiment) {
        this.expShiftPurposeId = expShiftPurposeId;
        this.hall = hall;
        this.name = name;
        this.active = active;
        this.experiment = experiment;
    }

    public BigInteger getExpShiftPurposeId() {
        return expShiftPurposeId;
    }

    public void setExpShiftPurposeId(BigInteger expShiftPurposeId) {
        this.expShiftPurposeId = expShiftPurposeId;
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
        hash += (expShiftPurposeId != null ? expShiftPurposeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ExpShiftPurpose)) {
            return false;
        }
        ExpShiftPurpose other = (ExpShiftPurpose) object;
        return (this.expShiftPurposeId != null || other.expShiftPurposeId == null) && (this.expShiftPurposeId == null || this.expShiftPurposeId.equals(other.expShiftPurposeId));
    }

    @Override
    public int compareTo(ExpShiftPurpose o) {
        int result = this.getHall().compareTo(o.getHall());

        if (result == 0) {
            result = this.getName().compareTo(o.getName());
        }

        return result;
    }

    @Override
    public String toString() {

        String builder = "ID: " +
                this.getExpShiftPurposeId() +
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
