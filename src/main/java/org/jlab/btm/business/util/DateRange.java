package org.jlab.btm.business.util;

import java.util.Date;

/**
 * Represents a range of time in millisecond precision.
 *
 * @author ryans
 */
public class DateRange {
  private Date start;
  private Date end;

  /**
   * Constructs a new DateRange with start and end equal to the time in which the object is
   * allocated.
   */
  public DateRange() {
    start = new Date();
    end = start;
  }

  /**
   * Constructs a new DateRange with the specified start and end time.
   *
   * @param start the start time.
   * @param end the end time.
   * @throws IllegalArgumentException if the start time is after end time.
   */
  public DateRange(Date start, Date end) throws IllegalArgumentException {
    if (start.after(end)) {
      throw new IllegalArgumentException("start cannot come after end");
    }

    this.start = start;
    this.end = end;
  }

  /**
   * Get start time.
   *
   * @return start time.
   */
  public Date getStart() {
    return start;
  }

  /**
   * Set start time.
   *
   * @param start start time.
   * @throws IllegalArgumentException if the start time is after end time.
   */
  public void setStart(Date start) throws IllegalArgumentException {
    if (start.after(end)) {
      throw new IllegalArgumentException("start cannot come after end");
    }

    this.start = start;
  }

  /**
   * Get end time.
   *
   * @return end time.
   */
  public Date getEnd() {
    return end;
  }

  /**
   * Set end time.
   *
   * @param end end time.
   * @throws IllegalArgumentException if the start is after end time.
   */
  public void setEnd(Date end) throws IllegalArgumentException {
    if (start.after(end)) {
      throw new IllegalArgumentException("start cannot come after end");
    }

    this.end = end;
  }

  @Override
  public String toString() {
    return "Range: " + start.toString() + " - " + end.toString();
  }
}
