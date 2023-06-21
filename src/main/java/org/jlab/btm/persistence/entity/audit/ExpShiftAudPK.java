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
public class ExpShiftAudPK implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Column(name = "EXP_SHIFT_ID", nullable = false)
    private BigInteger expShiftId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "REV", nullable = false)
    private BigInteger rev;

    public ExpShiftAudPK() {

    }

    public ExpShiftAudPK(BigInteger expShiftId, BigInteger rev) {
        this.expShiftId = expShiftId;
        this.rev = rev;
    }

    public BigInteger getExpShiftId() {
        return expShiftId;
    }

    public void setExpShiftId(BigInteger expShiftId) {
        this.expShiftId = expShiftId;
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
        hash = 23 * hash + (this.expShiftId != null ? this.expShiftId.hashCode() : 0);
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
        final ExpShiftAudPK other = (ExpShiftAudPK) obj;
        if (!Objects.equals(this.expShiftId, other.expShiftId)) {
            return false;
        }
        return Objects.equals(this.rev, other.rev);
    }
}
