package org.jlab.btm.persistence.projection;

import java.util.Date;
import org.jlab.btm.persistence.entity.CcAccHour;

/**
 * @author ryans
 */
public class DowntimeHourCrossCheck {

  private static final int HALF_HOUR_OF_SECONDS = 1800;
  private static final int TEN_MINUTES_OF_SECONDS = 600;

  private final Date dayAndHour;

  public DowntimeHourCrossCheck(Date dayAndHour, CcAccHour ccAccHour, DtmHour dtmHour) {
    this.dayAndHour = dayAndHour;
  }

  public Date getDayAndHour() {
    return dayAndHour;
  }
}
