package org.jlab.btm.persistence.projection;

import java.util.ArrayList;
import java.util.List;

public class HallChargeData {
  public List<DailyCharge> chargeListA = new ArrayList<>();
  public List<DailyCharge> chargeListB = new ArrayList<>();
  public List<DailyCharge> chargeListC = new ArrayList<>();
  public List<DailyCharge> chargeListD = new ArrayList<>();

  public List<DailyCharge> getChargeListA() {
    return chargeListA;
  }

  public List<DailyCharge> getChargeListB() {
    return chargeListB;
  }

  public List<DailyCharge> getChargeListC() {
    return chargeListC;
  }

  public List<DailyCharge> getChargeListD() {
    return chargeListD;
  }
}
