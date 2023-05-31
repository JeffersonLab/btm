package org.jlab.btm.persistence.projection;

import org.jlab.btm.persistence.entity.ExpHallHour;
import org.jlab.smoothness.persistence.enumeration.Hall;

import java.util.List;

/**
 * @author ryans
 */
public class ExpHallShiftAvailability {
    private Hall hall;
    private List<ExpHallHour> hourList;
    private List<ExpHallHour> dbHourList;
    private List<ExpHallHour> epicsHourList;

    private ExpHallHourTotals hourTotals;

    private ExpHallHourTotals epicsHourTotals;


    public ExpHallShiftAvailability() {
    }

    public Hall getHall() {
        return hall;
    }

    public void setHall(Hall hall) {
        this.hall = hall;
    }

    public List<ExpHallHour> getDbHourList() {
        return dbHourList;
    }

    public void setDbHourList(List<ExpHallHour> dbHourList) {
        this.dbHourList = dbHourList;
    }

    public List<ExpHallHour> getHourList() {
        return hourList;
    }

    public void setHourList(List<ExpHallHour> hourList) {
        this.hourList = hourList;
    }

    public List<ExpHallHour> getEpicsHourList() {
        return epicsHourList;
    }

    public void setEpicsHourList(List<ExpHallHour> epicsHourList) {
        this.epicsHourList = epicsHourList;
    }

    public void setShiftTotals(ExpHallHourTotals totals) {
        this.hourTotals = totals;
    }

    public void setEpicsShiftTotals(ExpHallHourTotals epicsTotals) {
        this.epicsHourTotals = epicsTotals;
    }

    public ExpHallHourTotals getShiftTotals() {
        return hourTotals;
    }

    public ExpHallHourTotals getEpicsShiftTotals() {
        return epicsHourTotals;
    }
}
