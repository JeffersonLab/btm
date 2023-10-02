package org.jlab.btm.business.params;

import org.jlab.btm.persistence.enumeration.TimesheetType;
import org.jlab.smoothness.persistence.enumeration.Shift;

import java.util.Date;

public class ActivityAuditParams {
    private Date modifiedStart;
    private Date modifiedEnd;
    private TimesheetType type;
    private Shift shift;
    private Date timesheetDate;
    private int offset;
    private int maxPerPage;

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getMaxPerPage() {
        return maxPerPage;
    }

    public void setMaxPerPage(int maxPerPage) {
        this.maxPerPage = maxPerPage;
    }

    public Date getModifiedStart() {
        return modifiedStart;
    }

    public void setModifiedStart(Date modifiedStart) {
        this.modifiedStart = modifiedStart;
    }

    public Date getModifiedEnd() {
        return modifiedEnd;
    }

    public void setModifiedEnd(Date modifiedEnd) {
        this.modifiedEnd = modifiedEnd;
    }

    public TimesheetType getType() {
        return type;
    }

    public void setType(TimesheetType type) {
        this.type = type;
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public Date getTimesheetDate() {
        return timesheetDate;
    }

    public void setTimesheetDate(Date timesheetDate) {
        this.timesheetDate = timesheetDate;
    }
}
