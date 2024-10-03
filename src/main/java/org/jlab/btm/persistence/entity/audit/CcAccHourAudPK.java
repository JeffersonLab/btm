package org.jlab.btm.persistence.entity.audit;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 * @author ryans
 */
@Embeddable
public class CcAccHourAudPK implements Serializable {
  @Basic(optional = false)
  @NotNull
  @Column(name = "CC_ACC_HOUR_ID", nullable = false)
  private BigInteger ccAccHourId;

  @Basic(optional = false)
  @NotNull
  @Column(name = "REV", nullable = false)
  private BigInteger rev;

  public CcAccHourAudPK() {}

  public CcAccHourAudPK(BigInteger ccAccHourId, BigInteger rev) {
    this.ccAccHourId = ccAccHourId;
    this.rev = rev;
  }

  public BigInteger getCcAccHourId() {
    return ccAccHourId;
  }

  public void setCcAccHourId(BigInteger ccAccHourId) {
    this.ccAccHourId = ccAccHourId;
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
    hash = 23 * hash + (this.ccAccHourId != null ? this.ccAccHourId.hashCode() : 0);
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
    final CcAccHourAudPK other = (CcAccHourAudPK) obj;
    if (!Objects.equals(this.ccAccHourId, other.ccAccHourId)) {
      return false;
    }
    return Objects.equals(this.rev, other.rev);
  }
}
