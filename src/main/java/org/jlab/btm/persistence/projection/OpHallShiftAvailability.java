package org.jlab.btm.persistence.projection;

import org.jlab.btm.persistence.entity.OpHallHour;
import org.jlab.smoothness.persistence.enumeration.Hall;

import java.util.List;

/**
 * @author ryans
 */
public class OpHallShiftAvailability {
    private Hall hall;
    private OpHallShiftTotals shiftTotals;
    private OpHallShiftTotals epicsShiftTotals;
    private OpHallShiftTotals pdShiftTotals;
    private List<OpHallHour> hourList;
    private List<OpHallHour> epicsHourList;
    private List<OpHallHour> dbHourList;

    public Hall getHall() {
        return hall;
    }

    public void setHall(Hall hall) {
        this.hall = hall;
    }

    public OpHallShiftTotals getShiftTotals() {
        return shiftTotals;
    }

    public void setShiftTotals(OpHallShiftTotals shiftTotals) {
        this.shiftTotals = shiftTotals;
    }

    public OpHallShiftTotals getEpicsShiftTotals() {
        return epicsShiftTotals;
    }

    public void setEpicsShiftTotals(OpHallShiftTotals epicsShiftTotals) {
        this.epicsShiftTotals = epicsShiftTotals;
    }

    public OpHallShiftTotals getPdShiftTotals() {
        return pdShiftTotals;
    }

    public void setPdShiftTotals(OpHallShiftTotals pdShiftTotals) {
        this.pdShiftTotals = pdShiftTotals;
    }

    public List<OpHallHour> getHourList() {
        return hourList;
    }

    public void setHourList(List<OpHallHour> hourList) {
        this.hourList = hourList;
    }

    public List<OpHallHour> getEpicsHourList() {
        return epicsHourList;
    }

    public void setEpicsHourList(List<OpHallHour> epicsHourList) {
        this.epicsHourList = epicsHourList;
    }

    public List<OpHallHour> getDbHourList() {
        return dbHourList;
    }

    public void setDbHourList(List<OpHallHour> dbHourList) {
        this.dbHourList = dbHourList;
    }
}
