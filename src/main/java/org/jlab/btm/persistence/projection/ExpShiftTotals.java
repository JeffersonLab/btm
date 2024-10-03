package org.jlab.btm.persistence.projection;

import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * @author ryans
 */
public class ExpShiftTotals {
  private Hall hall;
  private Integer hourCount;
  private Integer abuSeconds;
  private Integer banuSeconds;
  private Integer bnaSeconds;
  private Integer accSeconds;
  private Integer offSeconds;

  public ExpShiftTotals(
      Character hall,
      Number hourCount,
      Number abuSeconds,
      Number banuSeconds,
      Number bnaSeconds,
      Number accSeconds,
      Number offSeconds) {
    this.hall = Hall.valueOf(hall.toString());
    this.hourCount = hourCount.intValue();
    this.abuSeconds = abuSeconds.intValue();
    this.banuSeconds = banuSeconds.intValue();
    this.bnaSeconds = bnaSeconds.intValue();
    this.accSeconds = accSeconds.intValue();
    this.offSeconds = offSeconds.intValue();
  }

  public Hall getHall() {
    return hall;
  }

  public void setHall(Hall hall) {
    this.hall = hall;
  }

  public Integer getHourCount() {
    return hourCount;
  }

  public void setHourCount(Integer hourCount) {
    this.hourCount = hourCount;
  }

  public Integer getAbuSeconds() {
    return abuSeconds;
  }

  public void setAbuSeconds(Integer abuSeconds) {
    this.abuSeconds = abuSeconds;
  }

  public Integer getBanuSeconds() {
    return banuSeconds;
  }

  public void setBanuSeconds(Integer banuSeconds) {
    this.banuSeconds = banuSeconds;
  }

  public Integer getBnaSeconds() {
    return bnaSeconds;
  }

  public void setBnaSeconds(Integer bnaSeconds) {
    this.bnaSeconds = bnaSeconds;
  }

  public Integer getAccSeconds() {
    return accSeconds;
  }

  public void setAccSeconds(Integer accSeconds) {
    this.accSeconds = accSeconds;
  }

  public Integer getOffSeconds() {
    return offSeconds;
  }

  public void setOffSeconds(Integer offSeconds) {
    this.offSeconds = offSeconds;
  }
}
