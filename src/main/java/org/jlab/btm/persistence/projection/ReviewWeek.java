package org.jlab.btm.persistence.projection;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ryans
 */
public class ReviewWeek {
    List<ReviewDay> dayList = new ArrayList<>();

    public List<ReviewDay> getDayList() {
        return dayList;
    }

    public void setDayList(List<ReviewDay> dayList) {
        this.dayList = dayList;
    }
}
