package org.jlab.btm.persistence.entity.audit;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

/**
 *
 * @author ryans
 */
@Embeddable
public class CcShiftAudPK implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Column(name = "CC_SHIFT_ID", nullable = false)
    private BigInteger ccShiftId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "REV", nullable = false)
    private BigInteger rev;

    public CcShiftAudPK() {

    }

    public CcShiftAudPK(BigInteger ccShiftId, BigInteger rev) {
        this.ccShiftId = ccShiftId;
        this.rev = rev;
    }

    public BigInteger getCcShiftId() {
        return ccShiftId;
    }

    public void setCcShiftId(BigInteger ccShiftId) {
        this.ccShiftId = ccShiftId;
    }

    public BigInteger getRev() {
        return rev;
    }

    public void setRev(BigInteger rev) {
        this.rev = rev;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + (this.ccShiftId != null ? this.ccShiftId.hashCode() : 0);
        hash = 23 * hash + (this.rev != null ? this.rev.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CcShiftAudPK other = (CcShiftAudPK) obj;
        if (!Objects.equals(this.ccShiftId, other.ccShiftId)) {
            return false;
        }
        return Objects.equals(this.rev, other.rev);
    }
}
