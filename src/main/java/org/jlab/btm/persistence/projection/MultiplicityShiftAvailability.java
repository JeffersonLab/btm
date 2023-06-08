package org.jlab.btm.persistence.projection;

import org.jlab.btm.persistence.entity.CcMultiplicityHour;

import java.util.List;

/**
 * @author ryans
 */
public class MultiplicityShiftAvailability {
    private CcMultiplicityShiftTotals shiftTotals;
    private CcMultiplicityShiftTotals epicsShiftTotals;
    private List<CcMultiplicityHour> hourList;
    private List<CcMultiplicityHour> epicsHourList;
    private List<CcMultiplicityHour> dbHourList;

    public CcMultiplicityShiftTotals getShiftTotals() {
        return shiftTotals;
    }

    public void setShiftTotals(CcMultiplicityShiftTotals shiftTotals) {
        this.shiftTotals = shiftTotals;
    }

    public CcMultiplicityShiftTotals getEpicsShiftTotals() {
        return epicsShiftTotals;
    }

    public void setEpicsShiftTotals(CcMultiplicityShiftTotals epicsShiftTotals) {
        this.epicsShiftTotals = epicsShiftTotals;
    }

    public List<CcMultiplicityHour> getHourList() {
        return hourList;
    }

    public void setHourList(List<CcMultiplicityHour> hourList) {
        this.hourList = hourList;
    }

    public List<CcMultiplicityHour> getEpicsHourList() {
        return epicsHourList;
    }

    public void setEpicsHourList(List<CcMultiplicityHour> epicsHourList) {
        this.epicsHourList = epicsHourList;
    }

    public List<CcMultiplicityHour> getDbHourList() {
        return dbHourList;
    }

    public void setDbHourList(List<CcMultiplicityHour> dbHourList) {
        this.dbHourList = dbHourList;
    }
}
