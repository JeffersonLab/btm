package org.jlab.btm.persistence.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.Size;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;
import org.jlab.btm.persistence.projection.AuditedEntityChange;
import org.jlab.btm.presentation.util.RevisionInfoListener;

/**
 * An Envers entity auditing revision information record.
 *
 * @author ryans
 */
@Entity
@RevisionEntity(RevisionInfoListener.class)
@Table(name = "REVISION_INFO", schema = "BTM_OWNER")
public class RevisionInfo implements Serializable {
    @Id
    @GeneratedValue
    @RevisionNumber
    @Column(name = "REV", nullable = false)
    private int id;
    @RevisionTimestamp
    @Column(name = "REVTSTMP")
    private long ts;
    @Basic(optional = false)
    @Column(name = "USERNAME", length = 64)
    @Size(max=64)
    private String username;
    @Basic(optional = false)
    @Column(name = "ADDRESS", length = 64)
    @Size(max=64)
    private String address;
    @Transient
    List<AuditedEntityChange> changeList;

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + this.id;
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof RevisionInfo)) {
            return false;
        }

        return ((RevisionInfo)o).getId() == this.getId();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTimestamp() {
        return ts;
    }

    public void setTimestamp(long ts) {
        this.ts = ts;
    }

    public Date getRevisionDate() {
        return new Date(ts);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<AuditedEntityChange> getChangeList() {
        return changeList;
    }

    public void setChangeList(List<AuditedEntityChange> changeList) {
        this.changeList = changeList;
    }
}
