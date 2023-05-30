package org.jlab.btm.persistence.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.jlab.btm.persistence.enumeration.Role;
import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * An experimenter hall signature.
 *
 * @author ryans
 */
@Entity
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@Table(name = "EXP_HALL_SIGNATURE", schema = "BTM_OWNER", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"HALL", "START_DAY_AND_HOUR", "SIGNED_BY"})})
@NamedQueries({
        @NamedQuery(name = "ExpHallSignature.findByExpHallSignatureId", query = "SELECT e FROM ExpHallSignature e WHERE e.expHallSignatureId = :expHallSignatureId"),
        @NamedQuery(name = "ExpHallSignature.findByHallStartDayAndHourSignedBy", query = "SELECT e FROM ExpHallSignature e WHERE e.hall = :hall AND e.startDayAndHour = :startDayAndHour AND e.signedBy = :signedBy"),
        @NamedQuery(name = "ExpHallSignature.findByHallStartDayAndHour", query = "SELECT e FROM ExpHallSignature e WHERE e.hall = :hall AND e.startDayAndHour = :startDayAndHour ORDER BY e.signedDate DESC")})
public class ExpHallSignature implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name="ExpHallSignatureId", sequenceName="EXP_HALL_SIGNATURE_ID", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="ExpHallSignatureId")
    @Basic(optional = false)
    @Column(name = "EXP_HALL_SIGNATURE_ID", nullable = false, precision = 38, scale = 0)
    private BigInteger expHallSignatureId;
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
    @Column(name = "SIGNED_ROLE", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull
    private Role signedRole;
    @NotNull
    @Column(name = "SIGNED_BY", nullable = false, length = 64)
    private String signedBy;
    @Column(name = "SIGNED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date signedDate;

    public ExpHallSignature() {
    }

    public ExpHallSignature(BigInteger expHallSignatureId) {
        this.expHallSignatureId = expHallSignatureId;
    }

    public BigInteger getExpHallSignatureId() {
        return expHallSignatureId;
    }

    public void setExpHallShiftId(BigInteger expHallSignatureId) {
        this.expHallSignatureId = expHallSignatureId;
    }

    public Hall getHall() {
        return hall;
    }

    public void setHall(Hall hall) {
        this.hall = hall;
    }

    public Date getStartDayAndHour() {
        return startDayAndHour;
    }

    public void setStartDayAndHour(Date startDayAndHour) {
        this.startDayAndHour = startDayAndHour;
    }

    public Role getSignedRole() {
        return signedRole;
    }

    public void setSignedRole(Role signedRole) {
        this.signedRole = signedRole;
    }

    public String getSignedBy() {
        return signedBy;
    }

    public void setSignedBy(String signedBy) {
        this.signedBy = signedBy;
    }

    public Date getSignedDate() {
        return signedDate;
    }

    public void setSignedDate(Date signedDate) {
        this.signedDate = signedDate;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (expHallSignatureId != null ? expHallSignatureId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ExpHallSignature)) {
            return false;
        }
        ExpHallSignature other = (ExpHallSignature) object;
        if ((this.expHallSignatureId == null && other.expHallSignatureId != null) || (this.expHallSignatureId != null && !this.expHallSignatureId.equals(other.expHallSignatureId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.jlab.btm.entity.ExpHallSignature[expHallSignatureId=" + expHallSignatureId + "]";
    }
}
