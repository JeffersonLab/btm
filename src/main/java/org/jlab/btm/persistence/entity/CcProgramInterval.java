package org.jlab.btm.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;
import org.jlab.btm.persistence.enumeration.TimelineProgram;

/**
 * An interval of the State Timeline: the accelerator program from a start time until the next
 * interval starts. The latest interval is the current program and has no end yet.
 *
 * <p>Only the start is stored. The machine always has exactly one program, so an interval ends
 * where the next one starts, and the timeline can't have overlaps or gaps.
 *
 * <p>The start is an Instant, not a Date like the hour entities: Hibernate binds it with its UTC
 * offset, so a start in the repeated hour at the end of daylight saving time is stored as the
 * instant it is.
 */
@Entity
@Table(
    name = "CC_PROGRAM_INTERVAL",
    schema = "BTM_OWNER",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"START_TIME"})})
public class CcProgramInterval implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @SequenceGenerator(
      name = "ProgramIntervalId",
      sequenceName = "CC_PROGRAM_INTERVAL_ID",
      allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ProgramIntervalId")
  @Basic(optional = false)
  @Column(name = "CC_PROGRAM_INTERVAL_ID", nullable = false, precision = 22, scale = 0)
  private BigInteger ccProgramIntervalId;

  @Basic(optional = false)
  @NotNull
  @Column(name = "START_TIME", nullable = false)
  private Instant startTime;

  @Basic(optional = false)
  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "PROGRAM", nullable = false, length = 16)
  private TimelineProgram program;

  /** Who added or last changed the interval; null if it was recorded from EPICS. */
  @Size(max = 64)
  @Column(name = "MODIFIED_BY", length = 64)
  private String modifiedBy;

  @Column(name = "MODIFIED_DATE")
  @Temporal(TemporalType.TIMESTAMP)
  private Date modifiedDate;

  /** When the next interval starts; null for the latest interval. Set by the service. */
  @Transient private Instant endTime;

  public BigInteger getCcProgramIntervalId() {
    return ccProgramIntervalId;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public void setStartTime(Instant startTime) {
    this.startTime = startTime;
  }

  public TimelineProgram getProgram() {
    return program;
  }

  public void setProgram(TimelineProgram program) {
    this.program = program;
  }

  public String getModifiedBy() {
    return modifiedBy;
  }

  public void setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  public Date getModifiedDate() {
    return modifiedDate;
  }

  public void setModifiedDate(Date modifiedDate) {
    this.modifiedDate = modifiedDate;
  }

  public Instant getEndTime() {
    return endTime;
  }

  public void setEndTime(Instant endTime) {
    this.endTime = endTime;
  }

  /**
   * @return The start as a Date, for JSTL's formatDate
   */
  public Date getStartDate() {
    return startTime == null ? null : Date.from(startTime);
  }

  /**
   * @return The end as a Date, for JSTL's formatDate; null for the latest interval
   */
  public Date getEndDate() {
    return endTime == null ? null : Date.from(endTime);
  }

  @Override
  public int hashCode() {
    return ccProgramIntervalId != null ? ccProgramIntervalId.hashCode() : 0;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof CcProgramInterval)) {
      return false;
    }
    CcProgramInterval other = (CcProgramInterval) object;
    return ccProgramIntervalId != null && ccProgramIntervalId.equals(other.ccProgramIntervalId);
  }

  @Override
  public String toString() {
    return "CcProgramInterval{" + "startTime=" + startTime + ", program=" + program + '}';
  }
}
