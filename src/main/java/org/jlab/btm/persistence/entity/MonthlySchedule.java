package org.jlab.btm.persistence.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;
import java.util.logging.Logger;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import org.jlab.smoothness.business.util.DateIterator;
import org.jlab.smoothness.business.util.TimeUtil;

/**
 * @author ryans
 */
@Entity
@Table(
    name = "MONTHLY_SCHEDULE",
    schema = "BTM_OWNER",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"START_DAY", "VERSION"})})
public class MonthlySchedule implements Serializable {

  private static final Logger LOGGER = Logger.getLogger(MonthlySchedule.class.getName());

  private static final long serialVersionUID = 1L;

  @Id
  @SequenceGenerator(name = "ScheduleId", sequenceName = "SCHEDULE_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ScheduleId")
  @Basic(optional = false)
  @NotNull
  @Column(name = "MONTHLY_SCHEDULE_ID", nullable = false, precision = 22, scale = 0)
  private BigInteger monthlyScheduleId;

  @Basic(optional = false)
  @NotNull
  @Column(name = "VERSION", nullable = false)
  private Integer version;

  @Basic(optional = false)
  @NotNull
  @Column(name = "START_DAY", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date startDay;

  @Basic(optional = true)
  @Column(name = "PUBLISHED_DATE", nullable = true)
  @Temporal(TemporalType.TIMESTAMP)
  private Date publishedDate;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "monthlySchedule", fetch = FetchType.LAZY)
  private List<ScheduleDay> scheduleDayList;

  public BigInteger getMonthlyScheduleId() {
    return monthlyScheduleId;
  }

  public void setScheduleMonthlyId(BigInteger monthlyScheduleId) {
    this.monthlyScheduleId = monthlyScheduleId;
  }

  public Date getStartDay() {
    return startDay;
  }

  public void setStartDay(Date startDay) {
    this.startDay = startDay;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public Date getPublishedDate() {
    return publishedDate;
  }

  public void setPublishedDate(Date publishedDate) {
    this.publishedDate = publishedDate;
  }

  public List<ScheduleDay> getScheduleDayList() {
    return scheduleDayList;
  }

  public void setScheduleDayList(List<ScheduleDay> scheduleDayList) {
    this.scheduleDayList = scheduleDayList;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (monthlyScheduleId != null ? monthlyScheduleId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof MonthlySchedule)) {
      return false;
    }
    MonthlySchedule other = (MonthlySchedule) object;
    return (this.monthlyScheduleId != null || other.monthlyScheduleId == null)
        && (this.monthlyScheduleId == null
            || this.monthlyScheduleId.equals(other.monthlyScheduleId));
  }

  @Override
  public String toString() {
    return "[ scheduleId=" + monthlyScheduleId + " ], " + "Start Day: " + getStartDay();
  }

  public ScheduleDay getScheduleDay(Date day) {
    ScheduleDay sd = null;

    Date dayMonthYear = TimeUtil.startOfDay(day, Calendar.getInstance());

    if (scheduleDayList != null) {
      for (ScheduleDay s : scheduleDayList) {
        // Don't use equals() method since java.util.Date and java.sql.Date and java.sql.Timestamp
        // break this
        if (s.dayMonthYear.getTime() == dayMonthYear.getTime()) {
          sd = s;
          break;
        }
      }
    }

    return sd;
  }

  /**
   * Returns days before specified day in reverse order (descending date).
   *
   * @param day The day
   * @return Set of days that come before sorted in reverse
   */
  public SortedSet<ScheduleDay> getDaysBefore(Date day) {
    SortedSet<ScheduleDay> daysBefore = new TreeSet<>(Collections.reverseOrder());

    Date dayMonthYear = TimeUtil.startOfDay(day, Calendar.getInstance());

    if (scheduleDayList != null) {
      for (ScheduleDay s : scheduleDayList) {
        if (s.dayMonthYear.before(dayMonthYear)) {
          daysBefore.add(s);
        }
      }
    }

    return daysBefore;
  }

  /**
   * Returns days after specified day in natural order (ascending date).
   *
   * @param searchStartInclusive The day in the month to start searching, inclusive
   * @return Set of days that come after sorted naturally
   */
  public SortedSet<ScheduleDay> getDaysAfter(Date searchStartInclusive) {
    SortedSet<ScheduleDay> daysAfter = new TreeSet<>();

    Date start = TimeUtil.startOfDay(searchStartInclusive, Calendar.getInstance());
    Date end = TimeUtil.endOfMonth(searchStartInclusive, Calendar.getInstance());

    Map<Date, ScheduleDay> dayMap = new HashMap<>();

    if (scheduleDayList != null) {
      for (ScheduleDay s : scheduleDayList) {
        dayMap.put(s.dayMonthYear, s);
      }
    }

    DateIterator iterator = new DateIterator(start, end);

    while (iterator.hasNext()) {
      Date next = iterator.next();
      ScheduleDay sd = dayMap.get(next);
      if (sd == null) {
        sd = new ScheduleDay();
        sd.setDayMonthYear(next);
        sd.setAccProgram("UNKNOWN");
      }
      daysAfter.add(sd);
    }

    return daysAfter;
  }
}
