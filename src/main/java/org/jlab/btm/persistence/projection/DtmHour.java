package org.jlab.btm.persistence.projection;

import java.util.Date;

/**
 * @author ryans
 */
public class DtmHour extends Hour {

  private Date dayAndHour;

  private short blockedSeconds;

  private short tuneSeconds;

  public DtmHour(Date dayAndHour, short blockedSeconds, short tuneSeconds) {
    this.dayAndHour = dayAndHour;
    this.blockedSeconds = blockedSeconds;
    this.tuneSeconds = tuneSeconds;
  }

  @Override
  public Date getDayAndHour() {
    return dayAndHour;
  }

  @Override
  public void setDayAndHour(Date dayAndHour) {
    this.dayAndHour = dayAndHour;
  }

  public short getBlockedSeconds() {
    return blockedSeconds;
  }

  public void setBlockedSeconds(short blockedSeconds) {
    this.blockedSeconds = blockedSeconds;
  }

  public short getTuneSeconds() {
    return tuneSeconds;
  }

  public void setTuneSeconds(short tuneSeconds) {
    this.tuneSeconds = tuneSeconds;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (this.getDayAndHour() != null ? this.getDayAndHour().hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof DtmHour)) {
      return false;
    }
    DtmHour other = (DtmHour) object;
    return (this.getDayAndHour() != null || other.getDayAndHour() == null)
        && (this.getDayAndHour() == null || this.getDayAndHour().equals(other.getDayAndHour()));
  }
}
