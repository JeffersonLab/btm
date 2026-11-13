package org.jlab.btm.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;
import org.jlab.btm.business.util.RunCalculator;
import org.jlab.smoothness.business.util.TimeUtil;

/**
 * @author ryans
 */
@Entity
@Table(name = "RUN", schema = "BTM_OWNER")
public class Run implements Serializable {

  private static final Logger LOGGER = Logger.getLogger(Run.class.getName());

  private static final long serialVersionUID = 1L;

  @Id
  @SequenceGenerator(name = "RunId", sequenceName = "RUN_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RunId")
  @Basic(optional = false)
  @NotNull
  @Column(name = "RUN_ID", nullable = false, precision = 22, scale = 0)
  private BigInteger runId;

  @Basic(optional = false)
  @NotNull
  @Column(name = "START_DAY_MONTH_YEAR", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date startDay;

  @Basic(optional = false)
  @NotNull
  @Column(name = "END_DAY_MONTH_YEAR", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date endDay;

  /** Whether an admin set the dates; otherwise they come from the published schedules. */
  @Basic(optional = false)
  @NotNull
  @Column(name = "MANUAL", nullable = false)
  private Boolean manual = false;

  /** The run the published schedules have in its place, if any; only for display. */
  @Transient private RunCalculator.Bounds scheduled;

  public BigInteger getRunId() {
    return runId;
  }

  public Date getStartDay() {
    return startDay;
  }

  public void setStartDay(Date startDay) {
    this.startDay = startDay;
  }

  public Date getEndDay() {
    return endDay;
  }

  public void setEndDay(Date endDay) {
    this.endDay = endDay;
  }

  /**
   * @return The last day of the run, the day before its end
   */
  public Date getLastDay() {
    return endDay == null ? null : TimeUtil.addDays(endDay, -1);
  }

  public boolean isManual() {
    return Boolean.TRUE.equals(manual);
  }

  public void setManual(boolean manual) {
    this.manual = manual;
  }

  /**
   * @return The number of days in the run
   */
  public long getDays() {
    return ChronoUnit.DAYS.between(
        RunCalculator.toLocalDate(startDay), RunCalculator.toLocalDate(endDay));
  }

  public RunCalculator.Bounds getScheduled() {
    return scheduled;
  }

  /**
   * @return The first day of the run the published schedules have in its place; null if none
   */
  public Date getScheduledStartDay() {
    return scheduled == null ? null : RunCalculator.toDate(scheduled.start());
  }

  /**
   * @return The last day of the run the published schedules have in its place; null if none
   */
  public Date getScheduledLastDay() {
    return scheduled == null ? null : RunCalculator.toDate(scheduled.lastDay());
  }

  /**
   * @return Whether the published schedules have the same days for the run
   */
  public boolean isAsScheduled() {
    return scheduled != null
        && scheduled.equals(
            new RunCalculator.Bounds(
                RunCalculator.toLocalDate(startDay), RunCalculator.toLocalDate(endDay)));
  }

  public void setScheduled(RunCalculator.Bounds scheduled) {
    this.scheduled = scheduled;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (runId != null ? runId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof Run)) {
      return false;
    }
    Run other = (Run) object;
    return (this.runId != null || other.runId == null)
        && (this.runId == null || this.runId.equals(other.runId));
  }

  @Override
  public String toString() {
    return "[ runId=" + runId + " ], " + "Start Day: " + getStartDay() + ", End Day: " + endDay;
  }
}
