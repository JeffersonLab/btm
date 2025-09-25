package org.jlab.btm.persistence.entity.audit;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import org.hibernate.envers.RevisionType;
import org.jlab.btm.persistence.entity.RevisionInfo;

/**
 * @author ryans
 */
@Entity
@Table(name = "CC_SHIFT_AUD", schema = "BTM_OWNER")
public class CcShiftAud implements Serializable {
  private static final long serialVersionUID = 1L;
  @EmbeddedId protected CcShiftAudPK ccShiftAudPK;

  @Enumerated(EnumType.ORDINAL)
  @NotNull
  @Column(name = "REVTYPE")
  private RevisionType type;

  @Basic(optional = false)
  @NotNull
  @Column(name = "START_DAY_AND_HOUR", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date startDayAndHour;

  @Size(max = 64)
  @Column(name = "CREW_CHIEF", length = 64)
  private String crewChief;

  @Size(max = 256)
  @Column(length = 256)
  private String operators;

  @Size(max = 2048)
  @Column(length = 2048)
  private String remark;

  @Size(max = 64)
  @Column(length = 64)
  private String program;

  @Size(max = 64)
  @Column(name = "PROGRAM_DEPUTY", length = 64)
  private String programDeputy;

  @JoinColumn(
      name = "REV",
      referencedColumnName = "REV",
      insertable = false,
      updatable = false,
      nullable = false)
  @NotNull
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private RevisionInfo revision;

  public CcShiftAud() {}

  public Date getStartDayAndHour() {
    return startDayAndHour;
  }

  public void setStartDayAndHour(Date startDayAndHour) {
    this.startDayAndHour = startDayAndHour;
  }

  public String getCrewChief() {
    return crewChief;
  }

  public void setCrewChief(String crewChief) {
    this.crewChief = crewChief;
  }

  public String getOperators() {
    return operators;
  }

  public void setOperators(String operators) {
    this.operators = operators;
  }

  public String getRemark() {
    return remark;
  }

  public void setRemark(String remark) {
    this.remark = remark;
  }

  public String getProgram() {
    return program;
  }

  public void setProgram(String program) {
    this.program = program;
  }

  public String getProgramDeputy() {
    return programDeputy;
  }

  public void setProgramDeputy(String programDeputy) {
    this.programDeputy = programDeputy;
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
    hash += (ccShiftAudPK != null ? ccShiftAudPK.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof CcShiftAud)) {
      return false;
    }
    CcShiftAud other = (CcShiftAud) object;
    return (this.ccShiftAudPK != null || other.ccShiftAudPK == null)
        && (this.ccShiftAudPK == null || this.ccShiftAudPK.equals(other.ccShiftAudPK));
  }
}
