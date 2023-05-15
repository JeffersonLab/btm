package org.jlab.btm.persistence.entity;

import org.jlab.btm.persistence.enumeration.Role;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

/**
 * @author ryans
 */
@Entity
@Table(name = "OP_SIGNATURE", schema = "BTM_OWNER", uniqueConstraints
        = {
        @UniqueConstraint(columnNames = {"START_DAY_AND_HOUR", "SIGNED_BY", "SIGNED_ROLE"})})
@NamedQueries({
        @NamedQuery(name = "OpSignature.findAll", query = "SELECT o FROM OpSignature o")})
public class OpSignature implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name = "OpSignatureId", sequenceName = "OP_SIGNATURE_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "OpSignatureId")
    @Basic(optional = false)
    @NotNull
    @Column(name = "OP_SIGNATURE_ID", nullable = false, precision = 22, scale = 0)
    private BigInteger opSignatureId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "START_DAY_AND_HOUR", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDayAndHour;
    @NotNull
    @Column(name = "SIGNED_BY", nullable = false, length = 64)
    private String signedBy;
    @Basic(optional = false)
    @NotNull
    @Column(name = "SIGNED_ROLE", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Role signedRole;
    @Basic(optional = false)
    @NotNull
    @Column(name = "SIGNED_DATE", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date signedDate;

    public BigInteger getOpSignatureId() {
        return opSignatureId;
    }

    public void setOpSignatureId(BigInteger opSignatureId) {
        this.opSignatureId = opSignatureId;
    }

    public Date getStartDayAndHour() {
        return startDayAndHour;
    }

    public void setStartDayAndHour(Date startDayAndHour) {
        this.startDayAndHour = startDayAndHour;
    }

    public String getSignedBy() {
        return signedBy;
    }

    public void setSignedBy(String signedBy) {
        this.signedBy = signedBy;
    }

    public Role getSignedRole() {
        return signedRole;
    }

    public void setSignedRole(Role signedRole) {
        this.signedRole = signedRole;
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
        hash += (opSignatureId != null ? opSignatureId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof OpSignature)) {
            return false;
        }
        OpSignature other = (OpSignature) object;
        return (this.opSignatureId != null || other.opSignatureId == null) &&
                (this.opSignatureId == null || this.opSignatureId.equals(other.opSignatureId));
    }

    @Override
    public String toString() {
        return "org.jlab.btm.persistence.entity.OpSignature[ opSignatureId=" + opSignatureId + " ]";
    }

}
