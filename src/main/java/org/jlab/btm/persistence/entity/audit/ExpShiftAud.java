package org.jlab.btm.persistence.entity.audit;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import org.hibernate.envers.RevisionType;
import org.jlab.btm.persistence.entity.ExpProgram;
import org.jlab.btm.persistence.entity.RevisionInfo;
import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * @author ryans
 */
@Entity
@Table(name = "EXP_SHIFT_AUD", schema = "BTM_OWNER")
public class ExpShiftAud implements Serializable {
  private static final long serialVersionUID = 1L;
  @EmbeddedId protected ExpShiftAudPK expShiftAudPK;

  @Enumerated(EnumType.ORDINAL)
  @NotNull
  @Column(name = "REVTYPE")
  private RevisionType type;

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

  @Column(name = "LEADER", length = 64)
  @Size(max = 64)
  private String leader;

  @Column(name = "REMARK", length = 2048)
  @Size(max = 2048)
  private String remark;

  @Column(name = "WORKERS", length = 256)
  @Size(max = 256)
  private String workers;

  @NotNull
  @JoinColumn(name = "EXP_PROGRAM_ID", referencedColumnName = "EXP_PROGRAM_ID", nullable = false)
  @ManyToOne(optional = false)
  private ExpProgram expProgram;

  @JoinColumn(
      name = "REV",
      referencedColumnName = "REV",
      insertable = false,
      updatable = false,
      nullable = false)
  @NotNull
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private RevisionInfo revision;

  public ExpShiftAud() {}

  public Date getStartDayAndHour() {
    return startDayAndHour;
  }

  public void setStartDayAndHour(Date startDayAndHour) {
    this.startDayAndHour = startDayAndHour;
  }

  public Hall getHall() {
    return hall;
  }

  public void setHall(Hall hall) {
    this.hall = hall;
  }

  public ExpProgram getExpProgram() {
    return expProgram;
  }

  public void setExpProgram(ExpProgram expProgram) {
    this.expProgram = expProgram;
  }

  public String getLeader() {
    return leader;
  }

  public void setLeader(String leader) {
    this.leader = leader;
  }

  public String getRemark() {
    return remark;
  }

  public void setRemark(String remark) {
    this.remark = remark;
  }

  public String getWorkers() {
    return workers;
  }

  public void setWorkers(String workers) {
    this.workers = workers;
  }

  public RevisionInfo getRevision() {
    return revision;
  }

  public void setRevision(RevisionInfo revision) {
    this.revision = revision;
  }

  public RevisionType getType() {
    return type;
  }

  public void setType(RevisionType type) {
    this.type = type;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (expShiftAudPK != null ? expShiftAudPK.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof ExpShiftAud)) {
      return false;
    }
    ExpShiftAud other = (ExpShiftAud) object;
    return (this.expShiftAudPK != null || other.expShiftAudPK == null)
        && (this.expShiftAudPK == null || this.expShiftAudPK.equals(other.expShiftAudPK));
  }
}
