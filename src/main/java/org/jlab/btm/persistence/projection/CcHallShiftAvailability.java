package org.jlab.btm.persistence.projection;

import java.util.List;
import org.jlab.btm.persistence.entity.CcHallHour;
import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * @author ryans
 */
public class CcHallShiftAvailability {
  private Hall hall;
  private CcHallShiftTotals shiftTotals;
  private CcHallShiftTotals epicsShiftTotals;
  private CcHallShiftTotals pdShiftTotals;
  private List<CcHallHour> hourList;
  private List<CcHallHour> epicsHourList;
  private List<CcHallHour> dbHourList;

  public Hall getHall() {
    return hall;
  }

  public void setHall(Hall hall) {
    this.hall = hall;
  }

  public CcHallShiftTotals getShiftTotals() {
    return shiftTotals;
  }

  public void setShiftTotals(CcHallShiftTotals shiftTotals) {
    this.shiftTotals = shiftTotals;
  }

  public CcHallShiftTotals getEpicsShiftTotals() {
    return epicsShiftTotals;
  }

  public void setEpicsShiftTotals(CcHallShiftTotals epicsShiftTotals) {
    this.epicsShiftTotals = epicsShiftTotals;
  }

  public CcHallShiftTotals getPdShiftTotals() {
    return pdShiftTotals;
  }

  public void setPdShiftTotals(CcHallShiftTotals pdShiftTotals) {
    this.pdShiftTotals = pdShiftTotals;
  }

  public List<CcHallHour> getHourList() {
    return hourList;
  }

  public void setHourList(List<CcHallHour> hourList) {
    this.hourList = hourList;
  }

  public List<CcHallHour> getEpicsHourList() {
    return epicsHourList;
  }

  public void setEpicsHourList(List<CcHallHour> epicsHourList) {
    this.epicsHourList = epicsHourList;
  }

  public List<CcHallHour> getDbHourList() {
    return dbHourList;
  }

  public void setDbHourList(List<CcHallHour> dbHourList) {
    this.dbHourList = dbHourList;
  }
}
