package org.jlab.btm.persistence.projection;

/**
 * @author ryans
 */
public class MultiplicitySummaryTotals {
  private final long fourUpSeconds;
  private final long threeUpSeconds;
  private final long twoUpSeconds;
  private final long oneUpSeconds;
  private final long anyUpSeconds;
  private final long allUpSeconds;
  private final long downHardSeconds;

  public MultiplicitySummaryTotals(
      Number fourUpSeconds,
      Number threeUpSeconds,
      Number twoUpSeconds,
      Number oneUpSeconds,
      Number anyUpSeconds,
      Number allUpSeconds,
      Number downHardSeconds) {
    this.fourUpSeconds = fourUpSeconds.longValue();
    this.threeUpSeconds = threeUpSeconds.longValue();
    this.twoUpSeconds = twoUpSeconds.longValue();
    this.oneUpSeconds = oneUpSeconds.longValue();
    this.anyUpSeconds = anyUpSeconds.longValue();
    this.allUpSeconds = allUpSeconds.longValue();
    this.downHardSeconds = downHardSeconds.longValue();
  }

  public long getFourUpSeconds() {
    return fourUpSeconds;
  }

  public long getThreeUpSeconds() {
    return threeUpSeconds;
  }

  public long getTwoUpSeconds() {
    return twoUpSeconds;
  }

  public long getOneUpSeconds() {
    return oneUpSeconds;
  }

  public long getAnyUpSeconds() {
    return anyUpSeconds;
  }

  public long getAllUpSeconds() {
    return allUpSeconds;
  }

  public long getDownHardSeconds() {
    return downHardSeconds;
  }
}
