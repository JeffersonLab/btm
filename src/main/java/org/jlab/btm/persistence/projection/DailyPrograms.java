package org.jlab.btm.persistence.projection;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

/** The program each hall is scheduled to run on one day, as the Charge report shows on hover. */
public class DailyPrograms {
  private final Date day;
  private final String hallA;
  private final String hallB;
  private final String hallC;
  private final String hallD;

  public DailyPrograms(Date day, String hallA, String hallB, String hallC, String hallD) {
    this.day = day;
    this.hallA = hallA;
    this.hallB = hallB;
    this.hallC = hallC;
    this.hallD = hallD;
  }

  public Date getDay() {
    return day;
  }

  /**
   * Returns the day's start as milliseconds since the Epoch in local time, not UTC: the server's
   * wall-clock time read as if it were UTC. The Charge report's charts plot the archiver's data
   * this way, so the browser need not know the server's time zone or daylight saving dates.
   *
   * @return the day's start in local milliseconds
   */
  public long getTimestamp() {
    return day.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .toInstant(ZoneOffset.UTC)
        .toEpochMilli();
  }

  public String getHallA() {
    return hallA;
  }

  public String getHallB() {
    return hallB;
  }

  public String getHallC() {
    return hallC;
  }

  public String getHallD() {
    return hallD;
  }
}
