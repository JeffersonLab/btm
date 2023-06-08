package org.jlab.btm.persistence.projection;

import org.jlab.btm.persistence.entity.CcAccHour;

import java.util.List;

/**
 * @author ryans
 */
public class AcceleratorShiftAvailability {
    private CcAccShiftTotals shiftTotals;
    private CcAccShiftTotals epicsShiftTotals;
    private CcAccShiftTotals pdShiftTotals;
    private List<CcAccHour> hourList;
    private List<CcAccHour> epicsHourList;
    private List<CcAccHour> dbHourList;

    public CcAccShiftTotals getShiftTotals() {
        return shiftTotals;
    }

    public void setShiftTotals(CcAccShiftTotals shiftTotals) {
        this.shiftTotals = shiftTotals;
    }

    public CcAccShiftTotals getEpicsShiftTotals() {
        return epicsShiftTotals;
    }

    public void setEpicsShiftTotals(CcAccShiftTotals epicsShiftTotals) {
        this.epicsShiftTotals = epicsShiftTotals;
    }

    public CcAccShiftTotals getPdShiftTotals() {
        return pdShiftTotals;
    }

    public void setPdShiftTotals(CcAccShiftTotals pdShiftTotals) {
        this.pdShiftTotals = pdShiftTotals;
    }

    public List<CcAccHour> getHourList() {
        return hourList;
    }

    public void setHourList(List<CcAccHour> hourList) {
        this.hourList = hourList;
    }

    public List<CcAccHour> getEpicsHourList() {
        return epicsHourList;
    }

    public void setEpicsHourList(List<CcAccHour> epicsHourList) {
        this.epicsHourList = epicsHourList;
    }

    public List<CcAccHour> getDbHourList() {
        return dbHourList;
    }

    public void setDbHourList(List<CcAccHour> dbHourList) {
        this.dbHourList = dbHourList;
    }
}
