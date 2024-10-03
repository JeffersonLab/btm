package org.jlab.btm.persistence.projection;

/**
 * @author ryans
 */
public class CcHallShiftTotals {
  private Integer upSeconds;
  private Integer tuneSeconds;
  private Integer bnrSeconds;
  private Integer downSeconds;
  private Integer offSeconds;

  public Integer getUpSeconds() {
    return upSeconds;
  }

  public void setUpSeconds(Integer upSeconds) {
    this.upSeconds = upSeconds;
  }

  public Integer getTuneSeconds() {
    return tuneSeconds;
  }

  public void setTuneSeconds(Integer tuneSeconds) {
    this.tuneSeconds = tuneSeconds;
  }

  public Integer getBnrSeconds() {
    return bnrSeconds;
  }

  public void setBnrSeconds(Integer bnrSeconds) {
    this.bnrSeconds = bnrSeconds;
  }

  public Integer getDownSeconds() {
    return downSeconds;
  }

  public void setDownSeconds(Integer downSeconds) {
    this.downSeconds = downSeconds;
  }

  public Integer getOffSeconds() {
    return offSeconds;
  }

  public void setOffSeconds(Integer offSeconds) {
    this.offSeconds = offSeconds;
  }
}
