package org.jlab.btm.business.service;

import java.util.Date;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.jlab.btm.business.service.epics.CcEpicsShiftService;
import org.jlab.btm.persistence.entity.CcCrossCheckComment;
import org.jlab.smoothness.business.exception.UserFriendlyException;

/**
 * @author ryans
 */
@Stateless
public class CcCrossCheckCommentService extends AbstractService<CcCrossCheckComment> {

  @EJB CcEpicsShiftService epicsService;

  @PersistenceContext(unitName = "btmPU")
  private EntityManager em;

  public CcCrossCheckCommentService() {
    super(CcCrossCheckComment.class);
  }

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  @PermitAll
  public CcCrossCheckComment findInDatabase(Date startDayAndHour) {
    TypedQuery<CcCrossCheckComment> query =
        em.createQuery(
            "select a from CcCrossCheckComment a where a.startDayAndHour = :startDayAndHour",
            CcCrossCheckComment.class);

    query.setParameter("startDayAndHour", startDayAndHour);

    List<CcCrossCheckComment> commentList = query.getResultList();
    CcCrossCheckComment comment = null;

    if (commentList != null && !commentList.isEmpty()) {
      comment = commentList.get(0);
    }

    return comment;
  }

  @RolesAllowed({"cc", "btm-admin"})
  public void editCrewChiefRemark(Date startDayAndHour, String remark)
      throws UserFriendlyException {
    CcCrossCheckComment comment = findInDatabase(startDayAndHour);

    if (comment == null) {
      comment = new CcCrossCheckComment();
      comment.setStartDayAndHour(startDayAndHour);
    }

    comment.setCrewChiefRemark(remark);

    super.edit(comment);
  }

  @RolesAllowed({"btm-admin"})
  public void editReviewerRemark(Date startDayAndHour, String remark) {
    CcCrossCheckComment comment = findInDatabase(startDayAndHour);

    if (comment == null) {
      comment = new CcCrossCheckComment();
      comment.setStartDayAndHour(startDayAndHour);
    }

    comment.setReviewerRemark(remark);

    super.edit(comment);
  }
}
