package org.jlab.btm.business.service;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.btm.persistence.entity.ExpProgram;
import org.jlab.btm.persistence.entity.ExpShift;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * Responsible for experimenter hall shift business operations.
 *
 * @author ryans
 */
@Stateless
public class ExpShiftService extends AbstractService<ExpShift> {

  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  public ExpShiftService() {
    super(ExpShift.class);
  }

  @EJB ExpSecurityRuleService ruleService;

  @EJB ExpProgramService programService;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  @PermitAll
  public List<ExpShift> findByShiftStartAndLoadPurpose(Date startDayAndHour) {
    TypedQuery<ExpShift> q =
        em.createQuery(
            "select a from ExpShift a where a.startDayAndHour = :startDayAndHour order by hall desc",
            ExpShift.class);

    q.setParameter("startDayAndHour", startDayAndHour);

    List<ExpShift> shiftList = q.getResultList();

    if (shiftList != null) {
      for (ExpShift shift : shiftList) {
        shift.getExpProgram().getName();
      }
    }

    return shiftList;
  }

  @PermitAll
  public ExpShift find(Hall hall, Date startDayAndHour) {
    TypedQuery<ExpShift> q =
        em.createNamedQuery("ExpShift.findByHallAndStartDayAndHour", ExpShift.class);

    q.setParameter("hall", hall);
    q.setParameter("startDayAndHour", startDayAndHour);

    // We don't use q.getSingleResult() because it throws NoResultException,
    // and we simply want to return null if no result.
    List<ExpShift> results = q.getResultList();
    ExpShift result = null;

    if (results.size() > 1) {
      throw new NonUniqueResultException(
          "There should only be one shift for a particular hall and start day and hour");
    } else if (results.size() == 1) {
      result = results.get(0);
    }

    return result;
  }

  @PermitAll
  public Map<Hall, ExpShift> getMap(List<ExpShift> shiftList) {
    Map<Hall, ExpShift> shiftMap = new HashMap<>();

    if (shiftList != null) {
      for (ExpShift shift : shiftList) {
        shiftMap.put(shift.getHall(), shift);
      }
    }

    return shiftMap;
  }

  @PermitAll
  public void editShift(
      Hall hall,
      Date startDayAndHour,
      String leader,
      String workers,
      BigInteger purposeId,
      String comments)
      throws UserFriendlyException {
    ruleService.editCheck(hall, startDayAndHour);

    ExpShift shift = find(hall, startDayAndHour);

    if (shift == null) {
      shift = new ExpShift();
      shift.setStartDayAndHour(startDayAndHour);
      shift.setHall(hall);
    }

    if (purposeId == null) {
      throw new UserFriendlyException("Program is required");
    }

    ExpProgram program = programService.find(purposeId);

    if (program == null) {
      throw new UserFriendlyException("Program not found");
    }

    shift.setLeader(leader);
    shift.setWorkers(workers);
    shift.setExpProgram(program);
    shift.setRemark(comments);

    super.edit(shift);
  }
}
