package org.jlab.btm.persistence.entity.audit;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import org.hibernate.envers.RevisionType;
import org.jlab.btm.persistence.entity.RevisionInfo;

/**
 * @author ryans
 */
@Entity
@Table(name = "CC_ACC_HOUR_AUD", schema = "BTM_OWNER")
public class CcAccHourAud implements Serializable {
  private static final long serialVersionUID = 1L;
  @EmbeddedId protected CcAccHourAudPK ccAccHourAudPK;

  @Enumerated(EnumType.ORDINAL)
  @NotNull
  @Column(name = "REVTYPE")
  private RevisionType type;

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
  @NotNull
  @Column(name = "UP_SECONDS", nullable = false)
  @Max(value = 3600, message = "PHYSICS must be less than or equal to 1 hour")
  @Min(value = 0, message = "PHYSICS must be greater than or equal to 0")
  private short upSeconds;

  @Basic(optional = false)
  @NotNull
  @Column(name = "SAD_SECONDS", nullable = false)
  @Max(value = 3600, message = "OFF must be less than or equal to 1 hour")
  @Min(value = 0, message = "OFF must be greater than or equal to 0")
  private short sadSeconds;

  @Basic(optional = false)
  @NotNull
  @Column(name = "DOWN_SECONDS", nullable = false)
  @Max(value = 3600, message = "DOWN must be less than or equal to 1 hour")
  @Min(value = 0, message = "DOWN must be greater than or equal to 0")
  private short downSeconds;

  @Basic(optional = false)
  @NotNull
  @Column(name = "STUDIES_SECONDS", nullable = false)
  @Max(value = 3600, message = "STUDIES must be less than or equal to 1 hour")
  @Min(value = 0, message = "STUDIES must be greater than or equal to 0")
  private short studiesSeconds;

  @Basic(optional = false)
  @NotNull
  @Column(name = "ACC_SECONDS", nullable = false)
  @Max(value = 3600, message = "ACC must be less than or equal to 1 hour")
  @Min(value = 0, message = "ACC must be greater than or equal to 0")
  private short accSeconds;

  @Basic(optional = false)
  @NotNull
  @Column(name = "RESTORE_SECONDS", nullable = false)
  @Max(value = 3600, message = "RESTORE must be less than or equal to 1 hour")
  @Min(value = 0, message = "RESTORE must be greater than or equal to 0")
  private short restoreSeconds;

  public CcAccHourAud() {}

  public Date getDayAndHour() {
    return dayAndHour;
  }

  public void setDayAndHour(Date startDayAndHour) {
    this.dayAndHour = startDayAndHour;
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

  public short getUpSeconds() {
    return upSeconds;
  }

  public void setUpSeconds(short upSeconds) {
    this.upSeconds = upSeconds;
  }

  public short getSadSeconds() {
    return sadSeconds;
  }

  public void setSadSeconds(short sadSeconds) {
    this.sadSeconds = sadSeconds;
  }

  public short getDownSeconds() {
    return downSeconds;
  }

  public void setDownSeconds(short downSeconds) {
    this.downSeconds = downSeconds;
  }

  public short getStudiesSeconds() {
    return studiesSeconds;
  }

  public void setStudiesSeconds(short studiesSeconds) {
    this.studiesSeconds = studiesSeconds;
  }

  public short getAccSeconds() {
    return accSeconds;
  }

  public void setAccSeconds(short accSeconds) {
    this.accSeconds = accSeconds;
  }

  public short getRestoreSeconds() {
    return restoreSeconds;
  }

  public void setRestoreSeconds(short restoreSeconds) {
    this.restoreSeconds = restoreSeconds;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (ccAccHourAudPK != null ? ccAccHourAudPK.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof CcAccHourAud)) {
      return false;
    }
    CcAccHourAud other = (CcAccHourAud) object;
    return (this.ccAccHourAudPK != null || other.ccAccHourAudPK == null)
        && (this.ccAccHourAudPK == null || this.ccAccHourAudPK.equals(other.ccAccHourAudPK));
  }
}
