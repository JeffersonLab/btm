package org.jlab.btm.persistence.projection;

import org.jlab.btm.persistence.entity.ExpHour;
import org.jlab.smoothness.persistence.enumeration.Hall;

import java.util.List;

/**
 * @author ryans
 */
public class ExpShiftAvailability {
    private Hall hall;
    private List<ExpHour> hourList;
    private List<ExpHour> dbHourList;
    private List<ExpHour> epicsHourList;

    private ExpHourTotals hourTotals;

    private ExpHourTotals epicsHourTotals;


    public ExpShiftAvailability() {
    }

    public Hall getHall() {
        return hall;
    }

    public void setHall(Hall hall) {
        this.hall = hall;
    }

    public List<ExpHour> getDbHourList() {
        return dbHourList;
    }

    public void setDbHourList(List<ExpHour> dbHourList) {
        this.dbHourList = dbHourList;
    }

    public List<ExpHour> getHourList() {
        return hourList;
    }

    public void setHourList(List<ExpHour> hourList) {
        this.hourList = hourList;
    }

    public List<ExpHour> getEpicsHourList() {
        return epicsHourList;
    }

    public void setEpicsHourList(List<ExpHour> epicsHourList) {
        this.epicsHourList = epicsHourList;
    }

    public void setShiftTotals(ExpHourTotals totals) {
        this.hourTotals = totals;
    }

    public void setEpicsShiftTotals(ExpHourTotals epicsTotals) {
        this.epicsHourTotals = epicsTotals;
    }

    public ExpHourTotals getShiftTotals() {
        return hourTotals;
    }

    public ExpHourTotals getEpicsShiftTotals() {
        return epicsHourTotals;
    }
}
