package org.jlab.btm.persistence.projection;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.hibernate.envers.RevisionType;
import org.jlab.btm.business.util.BtmTimeUtil;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * @author ryans
 */
public class AuditedEntityChange {
  private final long revision;
  private final RevisionType type;
  private final BigInteger entityId;
  private final Class entityClass;
  private final String url;
  private final String name;

  public AuditedEntityChange(
      long revision,
      RevisionType type,
      BigInteger entityId,
      Class entityClass,
      Date date,
      Hall hall) {
    this.revision = revision;
    this.type = type;
    this.entityId = entityId;
    this.entityClass = entityClass;

    SimpleDateFormat shiftFormat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat hourFormat = new SimpleDateFormat("yyyy-MM-dd HH");

    switch (entityClass.getSimpleName()) {
      case "ExpShift":
        this.url = "/reports/activity-audit/exp-shift?entityId=" + entityId;
        this.name =
            hall.name()
                + " "
                + shiftFormat.format(date)
                + " "
                + BtmTimeUtil.calculateExperimenterShift(date).name();
        break;
      case "CcShift":
        this.url = "/reports/activity-audit/cc-shift?entityId=" + entityId;
        this.name =
            shiftFormat.format(date) + " " + TimeUtil.calculateCrewChiefShiftType(date).name();
        break;
      case "ExpHour":
        this.url = "/reports/activity-audit/exp-hour?entityId=" + entityId;
        this.name = hall.name() + " " + hourFormat.format(date);
        break;
      case "CcAccHour":
        this.url = "/reports/activity-audit/cc-acc-hour?entityId=" + entityId;
        this.name = hourFormat.format(date);
        break;
      default:
        this.url = "Unknown";
        this.name = "Unknown";
        break;
    }
  }

  public long getRevision() {
    return revision;
  }

  public RevisionType getType() {
    return type;
  }

  public BigInteger getEntityId() {
    return entityId;
  }

  public Class getEntityClass() {
    return entityClass;
  }

  public String getUrl() {
    return url;
  }

  public String getName() {
    return name;
  }
}
