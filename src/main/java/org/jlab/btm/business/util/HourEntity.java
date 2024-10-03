package org.jlab.btm.business.util;

import java.util.Date;
import org.jlab.btm.persistence.enumeration.DataSource;

public interface HourEntity {
  Date getDayAndHour();

  void setDayAndHour(Date dayAndHour);

  void setSource(DataSource source);
}
