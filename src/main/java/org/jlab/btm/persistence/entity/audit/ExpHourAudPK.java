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
public class ExpHourAudPK implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Column(name = "EXP_HOUR_ID", nullable = false)
    private BigInteger expHourId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "REV", nullable = false)
    private BigInteger rev;

    public ExpHourAudPK() {

    }

    public ExpHourAudPK(BigInteger expHourId, BigInteger rev) {
        this.expHourId = expHourId;
        this.rev = rev;
    }

    public BigInteger getExpHourId() {
        return expHourId;
    }

    public void setExpHourId(BigInteger expHourId) {
        this.expHourId = expHourId;
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
        hash = 23 * hash + (this.expHourId != null ? this.expHourId.hashCode() : 0);
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
        final ExpHourAudPK other = (ExpHourAudPK) obj;
        if (!Objects.equals(this.expHourId, other.expHourId)) {
            return false;
        }
        return Objects.equals(this.rev, other.rev);
    }
}
