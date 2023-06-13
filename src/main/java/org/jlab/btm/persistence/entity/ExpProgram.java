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
@Table(name = "EXP_PROGRAM", schema = "BTM_OWNER", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"HALL", "NAME"}),
        @UniqueConstraint(columnNames = {"HALL", "EXP_PROGRAM_ID"})})
@NamedQueries({
        @NamedQuery(name = "ExpProgram.findByExpProgramId", query = "SELECT e FROM ExpProgram e WHERE e.expProgramId = :expProgramId"),
        @NamedQuery(name = "ExpProgram.findByHallAndName", query = "SELECT e FROM ExpProgram e WHERE e.hall = :hall AND e.name = :name"),
        @NamedQuery(name = "ExpProgram.findByHallAndActive", query = "SELECT e FROM ExpProgram e WHERE e.hall = :hall AND e.active = :active"),
        @NamedQuery(name = "ExpProgram.findByHallAndExperiment", query = "SELECT e FROM ExpProgram e WHERE e.hall = :hall AND e.experiment = :experiment")})
public class ExpProgram implements Comparable<ExpProgram>, Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name = "ExpProgramId", sequenceName = "EXP_PROGRAM_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ExpProgramId")
    @Basic(optional = false)
    @Column(name = "EXP_PROGRAM_ID", nullable = false, precision = 38, scale = 0)
    private BigInteger expProgramId;
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

    public ExpProgram() {
    }

    public ExpProgram(BigInteger expProgramId) {
        this.expProgramId = expProgramId;
    }

    public ExpProgram(BigInteger expProgramId, Hall hall, String name, boolean active, boolean experiment) {
        this.expProgramId = expProgramId;
        this.hall = hall;
        this.name = name;
        this.active = active;
        this.experiment = experiment;
    }

    public BigInteger getExpProgramId() {
        return expProgramId;
    }

    public void setExpProgramId(BigInteger expProgramId) {
        this.expProgramId = expProgramId;
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
        hash += (expProgramId != null ? expProgramId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ExpProgram)) {
            return false;
        }
        ExpProgram other = (ExpProgram) object;
        return (this.expProgramId != null || other.expProgramId == null) && (this.expProgramId == null || this.expProgramId.equals(other.expProgramId));
    }

    @Override
    public int compareTo(ExpProgram o) {
        int result = this.getHall().compareTo(o.getHall());

        if (result == 0) {
            result = this.getName().compareTo(o.getName());
        }

        return result;
    }

    @Override
    public String toString() {

        String builder = "ID: " +
                this.getExpProgramId() +
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
