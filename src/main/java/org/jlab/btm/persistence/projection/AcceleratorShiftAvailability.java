package org.jlab.btm.persistence.projection;

import org.jlab.btm.persistence.entity.OpAccHour;

import java.util.List;

/**
 * @author ryans
 */
public class AcceleratorShiftAvailability {
    private OpAccShiftTotals shiftTotals;
    private OpAccShiftTotals epicsShiftTotals;
    private OpAccShiftTotals pdShiftTotals;
    private List<OpAccHour> hourList;
    private List<OpAccHour> epicsHourList;
    private List<OpAccHour> dbHourList;

    public OpAccShiftTotals getShiftTotals() {
        return shiftTotals;
    }

    public void setShiftTotals(OpAccShiftTotals shiftTotals) {
        this.shiftTotals = shiftTotals;
    }

    public OpAccShiftTotals getEpicsShiftTotals() {
        return epicsShiftTotals;
    }

    public void setEpicsShiftTotals(OpAccShiftTotals epicsShiftTotals) {
        this.epicsShiftTotals = epicsShiftTotals;
    }

    public OpAccShiftTotals getPdShiftTotals() {
        return pdShiftTotals;
    }

    public void setPdShiftTotals(OpAccShiftTotals pdShiftTotals) {
        this.pdShiftTotals = pdShiftTotals;
    }

    public List<OpAccHour> getHourList() {
        return hourList;
    }

    public void setHourList(List<OpAccHour> hourList) {
        this.hourList = hourList;
    }

    public List<OpAccHour> getEpicsHourList() {
        return epicsHourList;
    }

    public void setEpicsHourList(List<OpAccHour> epicsHourList) {
        this.epicsHourList = epicsHourList;
    }

    public List<OpAccHour> getDbHourList() {
        return dbHourList;
    }

    public void setDbHourList(List<OpAccHour> dbHourList) {
        this.dbHourList = dbHourList;
    }
}
