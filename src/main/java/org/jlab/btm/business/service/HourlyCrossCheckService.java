package org.jlab.btm.business.service;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.security.PermitAll;
import org.jlab.btm.persistence.entity.CcAccHour;
import org.jlab.btm.persistence.entity.CcHallHour;
import org.jlab.btm.persistence.entity.CcMultiplicityHour;
import org.jlab.btm.persistence.entity.ExpHour;
import org.jlab.btm.persistence.projection.*;
import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * @author ryans
 */
public class HourlyCrossCheckService {

  @PermitAll
  public List<HallHourCrossCheck> getHourList(
      Hall hall,
      AcceleratorShiftAvailability accAvailability,
      MultiplicityShiftAvailability multiplicityAvailability,
      CcHallShiftAvailability opsHallAvail,
      ExpShiftAvailability expHallAvail) {
    List<HallHourCrossCheck> checkList = new ArrayList<>();

    List<CcAccHour> ccAccHourList = accAvailability.getHourList();
    List<CcMultiplicityHour> opMultiHourList = multiplicityAvailability.getHourList();
    List<CcHallHour> ccHallHourList = opsHallAvail.getHourList();
    List<ExpHour> expHourList = expHallAvail.getHourList();

    for (int i = 0; i < ccAccHourList.size(); i++) {
      CcAccHour ccAccHour = ccAccHourList.get(i);
      CcMultiplicityHour opMultiHour = opMultiHourList.get(i);
      CcHallHour ccHallHour = ccHallHourList.get(i);
      ExpHour expHour = expHourList.get(i);

      HallHourCrossCheck checkHour =
          new HallHourCrossCheck(
              hall, ccAccHour.getDayAndHour(), ccAccHour, opMultiHour, ccHallHour, expHour);
      checkList.add(checkHour);
    }

    return checkList;
  }
}
