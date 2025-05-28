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
import org.jlab.btm.persistence.entity.CcShift;
import org.jlab.btm.persistence.enumeration.DataSource;
import org.jlab.smoothness.business.exception.UserFriendlyException;

/**
 * @author ryans
 */
@Stateless
public class CcShiftService extends AbstractService<CcShift> {

  @EJB CcEpicsShiftService epicsService;

  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  public CcShiftService() {
    super(CcShift.class);
  }

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  @PermitAll
  public CcShift findInDatabase(Date startDayAndHour) {
    TypedQuery<CcShift> query =
        em.createQuery(
            "select a from CcShift a where a.startDayAndHour = :startDayAndHour", CcShift.class);

    query.setParameter("startDayAndHour", startDayAndHour);

    List<CcShift> shiftList = query.getResultList();
    CcShift shift = null;

    if (shiftList != null && !shiftList.isEmpty()) {
      shift = shiftList.get(0);
      if (shift != null) {
        shift.setSource(DataSource.DATABASE);
      }
    }

    return shift;
  }

  @PermitAll
  public CcShift findInEpics(Date startHour) {
    return epicsService.find(startHour);
  }

  @RolesAllowed({"cc", "btm-admin"})
  public void editShift(
      Date startDayAndHour,
      String crewChief,
      String operators,
      String program,
      String programDeputy,
      String comments)
      throws UserFriendlyException {
    CcShift shift = findInDatabase(startDayAndHour);

    if (shift == null) {
      shift = new CcShift();
      shift.setStartDayAndHour(startDayAndHour);
    }

    shift.setCrewChief(crewChief);
    shift.setOperators(operators);
    shift.setProgram(program);
    shift.setProgramDeputy(programDeputy);
    shift.setRemark(comments);

    super.edit(shift);
  }
}
