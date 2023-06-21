package org.jlab.btm.persistence.projection;

import org.hibernate.envers.RevisionType;

import java.math.BigInteger;

/**
 *
 * @author ryans
 */
public class AuditedEntityChange {
    private final long revision;
    private final RevisionType type;
    private final BigInteger entityId;
    private final Class entityClass;
    private final String url;

    public AuditedEntityChange(long revision, RevisionType type, BigInteger entityId, Class entityClass) {
        this.revision = revision;
        this.type = type;
        this.entityId = entityId;
        this.entityClass = entityClass;

        switch(entityClass.getSimpleName()) {
            case "ExpShift":
                this.url = "/reports/activity-audit/exp-shift?entityId=" + entityId;
                break;
            case "CcShift":
                this.url = "/reports/activity-audit/cc-shift?entityId=" + entityId;
                break;
            default:
                this.url = "Unknown";
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

    public String getUrl() {return url; }
}
