package org.jlab.btm.persistence.projection;

import org.jlab.btm.persistence.entity.OpMultiplicityHour;

import java.util.List;

/**
 * @author ryans
 */
public class MultiplicityShiftAvailability {
    private OpMultiplicityShiftTotals shiftTotals;
    private OpMultiplicityShiftTotals epicsShiftTotals;
    private List<OpMultiplicityHour> hourList;
    private List<OpMultiplicityHour> epicsHourList;
    private List<OpMultiplicityHour> dbHourList;

    public OpMultiplicityShiftTotals getShiftTotals() {
        return shiftTotals;
    }

    public void setShiftTotals(OpMultiplicityShiftTotals shiftTotals) {
        this.shiftTotals = shiftTotals;
    }

    public OpMultiplicityShiftTotals getEpicsShiftTotals() {
        return epicsShiftTotals;
    }

    public void setEpicsShiftTotals(OpMultiplicityShiftTotals epicsShiftTotals) {
        this.epicsShiftTotals = epicsShiftTotals;
    }

    public List<OpMultiplicityHour> getHourList() {
        return hourList;
    }

    public void setHourList(List<OpMultiplicityHour> hourList) {
        this.hourList = hourList;
    }

    public List<OpMultiplicityHour> getEpicsHourList() {
        return epicsHourList;
    }

    public void setEpicsHourList(List<OpMultiplicityHour> epicsHourList) {
        this.epicsHourList = epicsHourList;
    }

    public List<OpMultiplicityHour> getDbHourList() {
        return dbHourList;
    }

    public void setDbHourList(List<OpMultiplicityHour> dbHourList) {
        this.dbHourList = dbHourList;
    }
}
