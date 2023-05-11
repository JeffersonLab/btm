package org.jlab.btm.business.service;

import org.jlab.btm.persistence.entity.ExpHallHour;
import org.jlab.btm.persistence.entity.OpAccHour;
import org.jlab.btm.persistence.entity.OpHallHour;
import org.jlab.btm.persistence.entity.OpMultiplicityHour;
import org.jlab.btm.persistence.projection.*;
import org.jlab.smoothness.persistence.enumeration.Hall;

import javax.annotation.security.PermitAll;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ryans
 */
public class HourlyCrossCheckService {

    @PermitAll
    public List<HallHourCrossCheck> getHourList(Hall hall, AcceleratorShiftAvailability accAvailability, MultiplicityShiftAvailability multiplicityAvailability, OpHallShiftAvailability opsHallAvail, ExpHallShiftAvailability expHallAvail) {
        List<HallHourCrossCheck> checkList = new ArrayList<>();

        List<OpAccHour> opAccHourList = accAvailability.getHourList();
        List<OpMultiplicityHour> opMultiHourList = multiplicityAvailability.getHourList();
        List<OpHallHour> opHallHourList = opsHallAvail.getHourList();
        List<ExpHallHour> expHallHourList = expHallAvail.getHourList();

        for (int i = 0; i < opAccHourList.size(); i++) {
            OpAccHour opAccHour = opAccHourList.get(i);
            OpMultiplicityHour opMultiHour = opMultiHourList.get(i);
            OpHallHour opHallHour = opHallHourList.get(i);
            ExpHallHour expHallHour = expHallHourList.get(i);

            HallHourCrossCheck checkHour = new HallHourCrossCheck(hall, opAccHour.getDayAndHour(), opAccHour, opMultiHour, opHallHour, expHallHour);
            checkList.add(checkHour);
        }

        return checkList;
    }

}
