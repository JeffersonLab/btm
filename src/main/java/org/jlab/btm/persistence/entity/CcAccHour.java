package org.jlab.btm.persistence.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.jlab.btm.business.util.HourEntity;
import org.jlab.btm.persistence.enumeration.DataSource;

/**
 * @author ryans
 */
@Entity
@Table(
    name = "CC_ACC_HOUR",
    schema = "BTM_OWNER",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"DAY_AND_HOUR"})})
@NamedQueries({@NamedQuery(name = "CcAccHour.findAll", query = "SELECT o FROM CcAccHour o")})
@NamedNativeQueries({
  @NamedNativeQuery(
      name = "CcAccHour.insertNATIVE",
      query =
          "INSERT into CC_ACC_HOUR (CC_ACC_HOUR_ID, DAY_AND_HOUR, UP_SECONDS, SAD_SECONDS, DOWN_SECONDS, STUDIES_SECONDS, ACC_SECONDS, RESTORE_SECONDS) values (:id, to_timestamp_tz(:dayAndHour, 'YYYY-MM-DD HH24 TZD'), :up, :sad, :down, :studies, :acc, :restore)",
      resultClass = CcAccHour.class)
})
public class CcAccHour implements Serializable, HourEntity {

  private static final long serialVersionUID = 1L;

  @Id
  @SequenceGenerator(name = "AccHourId", sequenceName = "CC_ACC_HOUR_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AccHourId")
  @Basic(optional = false)
  @NotNull
  @Column(name = "CC_ACC_HOUR_ID", nullable = false, precision = 22, scale = 0)
  private BigInteger ccAccHourId;

  @Basic(optional = false)
  @NotNull
  @Column(name = "DAY_AND_HOUR", nullable = false, insertable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date dayAndHour;

  @Basic(optional = false)
  @NotNull
  @Column(name = "UP_SECONDS", nullable = false)
  @Max(value = 3600, message = "PHYSICS must be less than or equal to 1 hour")
  @Min(value = 0, message = "PHYSICS must be greater than or equal to 0")
  private short upSeconds;

  @Basic(optional = false)
  @NotNull
  @Column(name = "TUNING_SECONDS", nullable = false)
  @Max(value = 3600, message = "TUNING must be less than or equal to 1 hour")
  @Min(value = 0, message = "TUNING must be greater than or equal to 0")
  private short tuningSeconds;

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

  @Transient private DataSource source;

  public CcAccHour() {}

  public BigInteger getCcAccHourId() {
    return ccAccHourId;
  }

  public void setCcAccHourId(BigInteger opAccHourId) {
    this.ccAccHourId = opAccHourId;
  }

  @Override
  public Date getDayAndHour() {
    return dayAndHour;
  }

  @Override
  public void setDayAndHour(Date dayAndHour) {
    this.dayAndHour = dayAndHour;
  }

  /*public Date getDayAndHour() {
      return dayAndHour == null ? null : dayAndHour.getTime();
  }

  public void setDayAndHour(Date dayAndHour) {
      if (dayAndHour == null) {
          this.dayAndHour = null;
      } else {
          Calendar cal = Calendar.getInstance();
          cal.setTime(dayAndHour);
          this.dayAndHour = cal;
      }
  }*/

  public short getUpSeconds() {
    return upSeconds;
  }

  public void setUpSeconds(short upSeconds) {
    this.upSeconds = upSeconds;
  }

  public short getTuningSeconds() {
    return tuningSeconds;
  }

  public void setTuningSeconds(short tuningSeconds) {
    this.tuningSeconds = tuningSeconds;
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

  public DataSource getSource() {
    return source;
  }

  @Override
  public void setSource(DataSource source) {
    this.source = source;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (this.getDayAndHour() != null ? this.getDayAndHour().hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof CcAccHour)) {
      return false;
    }
    CcAccHour other = (CcAccHour) object;
    return (this.getDayAndHour() != null || other.getDayAndHour() == null)
        && (this.getDayAndHour() == null || this.getDayAndHour().equals(other.getDayAndHour()));
  }

  @Override
  public String toString() {
    return "org.jlab.btm.persistence.entity.CcAccHour[ ccAccHourId=" + ccAccHourId + " ]";
  }
}
