package org.jlab.btm.persistence.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;
import java.util.logging.Logger;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

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
  @Column(name = "END_DAY_MONTH_YEAR", nullable = true)
  @Temporal(TemporalType.TIMESTAMP)
  private Date endDay;

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
    return "[ runId=" + runId + " ], " + "Start Day: " + getStartDay();
  }
}
