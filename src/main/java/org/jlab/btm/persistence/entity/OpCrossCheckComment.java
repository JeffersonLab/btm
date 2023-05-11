package org.jlab.btm.persistence.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author ryans
 */
@Entity
@Table(name = "OP_CROSS_CHECK_COMMENT", schema = "BTM_OWNER", uniqueConstraints
        = {
        @UniqueConstraint(columnNames = {"START_DAY_AND_HOUR"})})
public class OpCrossCheckComment implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name = "OpCrossCheckCommentId", sequenceName = "OP_CROSS_CHECK_COMMENT_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "OpCrossCheckCommentId")
    @Basic(optional = false)
    @NotNull
    @Column(name = "OP_CHECK_COMMENT_ID", nullable = false, precision = 38, scale = 0)
    private BigDecimal opCheckCommentId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "START_DAY_AND_HOUR", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDayAndHour;
    @Size(max = 2048)
    @Column(length = 2048)
    private String remark;
    @Size(max = 2048)
    @Column(name = "REVIEWER_REMARK", length = 2048)
    private String reviewerRemark;

    public OpCrossCheckComment() {
    }

    public OpCrossCheckComment(BigDecimal opCheckCommentId) {
        this.opCheckCommentId = opCheckCommentId;
    }

    public OpCrossCheckComment(BigDecimal opCheckCommentId, Date startDayAndHour) {
        this.opCheckCommentId = opCheckCommentId;
        this.startDayAndHour = startDayAndHour;
    }

    public BigDecimal getOpShiftId() {
        return opCheckCommentId;
    }

    public void setOpShiftId(BigDecimal opCheckCommentId) {
        this.opCheckCommentId = opCheckCommentId;
    }

    public Date getStartDayAndHour() {
        return startDayAndHour;
    }

    public void setStartDayAndHour(Date startDayAndHour) {
        this.startDayAndHour = startDayAndHour;
    }

    public String getCrewChiefRemark() {
        return remark;
    }

    public void setCrewChiefRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (opCheckCommentId != null ? opCheckCommentId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof OpCrossCheckComment)) {
            return false;
        }
        OpCrossCheckComment other = (OpCrossCheckComment) object;
        return (this.opCheckCommentId != null || other.opCheckCommentId == null) && (this.opCheckCommentId == null || this.opCheckCommentId.equals(other.opCheckCommentId));
    }

    @Override
    public String toString() {
        return "org.jlab.webapp.persistence.entity.OpCrossCheckComment[ opCheckCommentId=" + opCheckCommentId + " ]";
    }

    public String getReviewerRemark() {
        return reviewerRemark;
    }

    public void setReviewerRemark(String reviewerRemark) {
        this.reviewerRemark = reviewerRemark;
    }
}
