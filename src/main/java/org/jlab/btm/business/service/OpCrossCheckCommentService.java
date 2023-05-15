package org.jlab.btm.business.service;

import org.jlab.btm.business.service.epics.EpicsShiftService;
import org.jlab.btm.persistence.entity.OpCrossCheckComment;
import org.jlab.smoothness.business.exception.UserFriendlyException;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

/**
 * @author ryans
 */
@Stateless
public class OpCrossCheckCommentService extends AbstractService<OpCrossCheckComment> {

    @EJB
    EpicsShiftService epicsService;
    @PersistenceContext(unitName = "btmPU")
    private EntityManager em;

    public OpCrossCheckCommentService() {
        super(OpCrossCheckComment.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @PermitAll
    public OpCrossCheckComment findInDatabase(Date startDayAndHour) {
        TypedQuery<OpCrossCheckComment> query = em.createQuery(
                "select a from OpCrossCheckComment a where a.startDayAndHour = :startDayAndHour", OpCrossCheckComment.class);

        query.setParameter("startDayAndHour", startDayAndHour);

        List<OpCrossCheckComment> commentList = query.getResultList();
        OpCrossCheckComment comment = null;

        if (commentList != null && !commentList.isEmpty()) {
            comment = commentList.get(0);
        }

        return comment;
    }

    @RolesAllowed({"cc", "btm-admin"})
    public void editCrewChiefRemark(Date startDayAndHour, String remark) throws UserFriendlyException {
        OpCrossCheckComment comment = findInDatabase(startDayAndHour);

        if (comment == null) {
            comment = new OpCrossCheckComment();
            comment.setStartDayAndHour(startDayAndHour);
        }

        comment.setCrewChiefRemark(remark);

        super.edit(comment);
    }

    @RolesAllowed({"btm-admin"})
    public void editReviewerRemark(Date startDayAndHour, String remark) {
        OpCrossCheckComment comment = findInDatabase(startDayAndHour);

        if (comment == null) {
            comment = new OpCrossCheckComment();
            comment.setStartDayAndHour(startDayAndHour);
        }

        comment.setReviewerRemark(remark);

        super.edit(comment);
    }

}
