package org.jlab.btm.persistence.entity.audit;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import org.hibernate.envers.RevisionType;
import org.jlab.btm.persistence.entity.RevisionInfo;
import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * @author ryans
 */
@Entity
@Table(name = "EXP_HOUR_AUD", schema = "BTM_OWNER")
public class ExpHourAud implements Serializable {
  private static final long serialVersionUID = 1L;
  @EmbeddedId protected ExpHourAudPK expHourAudPK;

  //@Enumerated(EnumType.ORDINAL)
  @NotNull
  @Column(name = "REVTYPE")
  private RevisionType type;

  @Basic(optional = false)
  @Column(name = "HALL", nullable = false, length = 1, columnDefinition = "char(1)")
  @Enumerated(EnumType.STRING)
  @NotNull
  private Hall hall;

  @Basic(optional = false)
  @Column(name = "DAY_AND_HOUR", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  @NotNull
  private Date dayAndHour;

  @JoinColumn(
      name = "REV",
      referencedColumnName = "REV",
      insertable = false,
      updatable = false,
      nullable = false)
  @NotNull
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private RevisionInfo revision;

  @Basic(optional = false)
  @Column(name = "ABU_SECONDS", nullable = false)
  @NotNull
  @Min(0)
  @Max(value = 3600, message = "{org.jlab.bta.maxTime}")
  private short abuSeconds;

  @Basic(optional = false)
  @Column(name = "BANU_SECONDS", nullable = false)
  @NotNull
  @Min(0)
  @Max(value = 3600, message = "{org.jlab.bta.maxTime}")
  private short banuSeconds;

  @Basic(optional = false)
  @Column(name = "BNA_SECONDS", nullable = false)
  @NotNull
  @Min(0)
  @Max(value = 3600, message = "{org.jlab.bta.maxTime}")
  private short bnaSeconds;

  @Basic(optional = false)
  @Column(name = "ACC_SECONDS", nullable = false)
  @NotNull
  @Min(0)
  @Max(value = 3600, message = "{org.jlab.bta.maxTime}")
  private short accSeconds;

  @Basic(optional = false)
  @Column(name = "ER_SECONDS", nullable = false)
  @NotNull
  @Min(0)
  @Max(value = 3600, message = "{org.jlab.bta.maxTime}")
  private short erSeconds;

  @Basic(optional = false)
  @Column(name = "PCC_SECONDS", nullable = false)
  @NotNull
  @Min(0)
  @Max(value = 3600, message = "{org.jlab.bta.maxTime}")
  private short pccSeconds;

  @Basic(optional = false)
  @Column(name = "UED_SECONDS", nullable = false)
  @NotNull
  @Min(0)
  @Max(value = 3600, message = "{org.jlab.bta.maxTime}")
  private short uedSeconds;

  @Basic(optional = false)
  @Column(name = "OFF_SECONDS", nullable = false)
  @NotNull
  @Min(0)
  @Max(value = 3600, message = "{org.jlab.bta.maxTime}")
  private short offSeconds;

  @Column(name = "REMARK", length = 128)
  @Size(max = 128)
  private String remark;

  public ExpHourAud() {}

  public Date getDayAndHour() {
    return dayAndHour;
  }

  public void setDayAndHour(Date startDayAndHour) {
    this.dayAndHour = startDayAndHour;
  }

  public Hall getHall() {
    return hall;
  }

  public void setHall(Hall hall) {
    this.hall = hall;
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

  public short getAbuSeconds() {
    return abuSeconds;
  }

  public void setAbuSeconds(short abuSeconds) {
    this.abuSeconds = abuSeconds;
  }

  public short getBanuSeconds() {
    return banuSeconds;
  }

  public void setBanuSeconds(short banuSeconds) {
    this.banuSeconds = banuSeconds;
  }

  public short getBnaSeconds() {
    return bnaSeconds;
  }

  public void setBnaSeconds(short bnaSeconds) {
    this.bnaSeconds = bnaSeconds;
  }

  public short getAccSeconds() {
    return accSeconds;
  }

  public void setAccSeconds(short accSeconds) {
    this.accSeconds = accSeconds;
  }

  public short getErSeconds() {
    return erSeconds;
  }

  public void setErSeconds(short erSeconds) {
    this.erSeconds = erSeconds;
  }

  public short getPccSeconds() {
    return pccSeconds;
  }

  public void setPccSeconds(short pccSeconds) {
    this.pccSeconds = pccSeconds;
  }

  public short getUedSeconds() {
    return uedSeconds;
  }

  public void setUedSeconds(short uedSeconds) {
    this.uedSeconds = uedSeconds;
  }

  public short getOffSeconds() {
    return offSeconds;
  }

  public void setOffSeconds(short offSeconds) {
    this.offSeconds = offSeconds;
  }

  public String getRemark() {
    return remark;
  }

  public void setRemark(String remark) {
    this.remark = remark;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (expHourAudPK != null ? expHourAudPK.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof ExpHourAud)) {
      return false;
    }
    ExpHourAud other = (ExpHourAud) object;
    return (this.expHourAudPK != null || other.expHourAudPK == null)
        && (this.expHourAudPK == null || this.expHourAudPK.equals(other.expHourAudPK));
  }
}
