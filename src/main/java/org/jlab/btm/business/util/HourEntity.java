package org.jlab.btm.business.util;

import org.jlab.btm.persistence.enumeration.DataSource;

import java.util.Date;

public interface HourEntity {
    Date getDayAndHour();

    void setDayAndHour(Date dayAndHour);

    void setSource(DataSource source);
}
