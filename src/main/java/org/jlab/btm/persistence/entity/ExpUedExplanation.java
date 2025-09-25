package org.jlab.btm.persistence.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigInteger;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * An experimenter UED explanation.
 *
 * @author ryans
 */
@Entity
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@Table(
    name = "EXP_UED_EXPLANATION",
    schema = "BTM_OWNER",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"HALL", "EXP_HOUR_ID", "EXP_REASON_ID"})})
@NamedQueries({
  @NamedQuery(
      name = "ExpHourReasonTime.findByExpUedExplanationId",
      query =
          "SELECT e FROM ExpUedExplanation e WHERE e.expUedExplanationId = :expUedExplanationId"),
  @NamedQuery(
      name = "ExpHourReasonTime.findByHallAndHourRange",
      query =
          "SELECT e FROM ExpUedExplanation e WHERE e.hall = :hall AND e.expHour.dayAndHour BETWEEN :startDayAndHour AND :endDayAndHour ORDER BY e.expHour.dayAndHour ASC"),
})
public class ExpUedExplanation implements Comparable<ExpUedExplanation>, Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @SequenceGenerator(
      name = "ExpUedExplanationId",
      sequenceName = "EXP_UED_EXPLANATION_ID",
      allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ExpUedExplanationId")
  @Basic(optional = false)
  @Column(name = "EXP_UED_EXPLANATION_ID", nullable = false, precision = 38, scale = 0)
  private BigInteger expUedExplanationId;

  @Basic(optional = false)
  @Column(name = "HALL", nullable = false, length = 1, columnDefinition = "char(1 char)")
  @NotNull
  @Enumerated(EnumType.STRING)
  private Hall hall;

  @Basic(optional = false)
  @Column(name = "SECONDS", nullable = false)
  @NotNull
  @Min(value = 1, message = "Minimum value is 1 second")
  @Max(value = 3600, message = "Maximum value is 3600 seconds")
  private short seconds;

  @NotNull
  @JoinColumn(name = "EXP_HOUR_ID", referencedColumnName = "EXP_HOUR_ID", nullable = false)
  @ManyToOne(optional = false)
  private ExpHour expHour;

  @NotNull
  @JoinColumn(name = "EXP_REASON_ID", referencedColumnName = "EXP_REASON_ID", nullable = false)
  @ManyToOne(optional = false)
  private ExpReason expReason;

  public ExpUedExplanation() {}

  public ExpUedExplanation(BigInteger expUedExplanationId) {
    this.expUedExplanationId = expUedExplanationId;
  }

  public ExpUedExplanation(BigInteger expUedExplanationId, Hall hall, short seconds) {
    this.expUedExplanationId = expUedExplanationId;
    this.hall = hall;
    this.seconds = seconds;
  }

  public BigInteger getExpUedExplanationId() {
    return expUedExplanationId;
  }

  public void setExpUedExplanationId(BigInteger expUedExplanationId) {
    this.expUedExplanationId = expUedExplanationId;
  }

  public Hall getHall() {
    return hall;
  }

  public void setHall(Hall hall) {
    this.hall = hall;
  }

  public short getSeconds() {
    return seconds;
  }

  public void setSeconds(short seconds) {
    this.seconds = seconds;
  }

  public ExpHour getExpHour() {
    return expHour;
  }

  public void setExpHour(ExpHour expHour) {
    this.expHour = expHour;
  }

  public ExpReason getExpReason() {
    return expReason;
  }

  public void setExpReason(ExpReason expReason) {
    this.expReason = expReason;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (expUedExplanationId != null ? expUedExplanationId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof ExpUedExplanation)) {
      return false;
    }
    ExpUedExplanation other = (ExpUedExplanation) object;
    if ((this.expUedExplanationId == null && other.expUedExplanationId != null)
        || (this.expUedExplanationId != null
            && !this.expUedExplanationId.equals(other.expUedExplanationId))) {
      return false;
    }
    return true;
  }

  @Override
  public int compareTo(ExpUedExplanation o) {
    int result = this.getHall().compareTo(o.getHall());

    if (result == 0) {
      result = this.getExpHour().getDayAndHour().compareTo(o.getExpHour().getDayAndHour());
    }

    return result;
  }

  @Override
  public String toString() {
    return "org.jlab.btm.entity.ExpUedExplanation[expUedExplanationId=" + expUedExplanationId + "]";
  }
}
