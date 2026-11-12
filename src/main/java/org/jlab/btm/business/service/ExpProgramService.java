package org.jlab.btm.business.service;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.btm.persistence.entity.ExpProgram;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * Responsible for experimenter program handling.
 *
 * @author ryans
 */
@Stateless
public class ExpProgramService extends AbstractService<ExpProgram> {

  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  public ExpProgramService() {
    super(ExpProgram.class);
  }

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  @PermitAll
  @Override
  public ExpProgram find(Object id) {
    return super.find(id);
  }

  @PermitAll
  public List<ExpProgram> findByHall(Hall hall, Boolean active) {
    String query = "select a from ExpProgram a where hall = :hall ";
    String order = "order by experiment desc, active desc, name asc";

    if (active != null) {
      query = query + "and active = :active ";
    }

    query = query + order;

    TypedQuery<ExpProgram> q = em.createQuery(query, ExpProgram.class);

    q.setParameter("hall", hall);

    if (active != null) {
      q.setParameter("active", active);
    }

    return q.getResultList();
  }

  @PermitAll
  public List<ExpProgram> findActiveExperimentsByHall(Hall hall) {
    String query =
        "select a from ExpProgram a where hall = :hall and active = true and experiment = true ";
    String order = "order by name asc";

    query = query + order;

    TypedQuery<ExpProgram> q = em.createQuery(query, ExpProgram.class);

    q.setParameter("hall", hall);

    return q.getResultList();
  }

  @PermitAll
  public List<ExpProgram> findActiveNonExperimentsByHall(Hall hall) {
    String query =
        "select a from ExpProgram a where hall = :hall and active = true and experiment = false ";
    String order = "order by name asc";

    query = query + order;

    TypedQuery<ExpProgram> q = em.createQuery(query, ExpProgram.class);

    q.setParameter("hall", hall);

    return q.getResultList();
  }

  @PermitAll
  public boolean isDuplicate(Hall hall, String name) {
    TypedQuery<Long> q =
        em.createQuery(
            "select count(a.name) from ExpProgram a where a.hall = :hall and a.name = :name",
            Long.class);

    q.setParameter("hall", hall);
    q.setParameter("name", name);

    Long count = q.getSingleResult();

    return count > 0;
  }

  @RolesAllowed({"cc", "btm-admin", "schcom"})
  public void add(
      Hall hall, String name, String alias, String url, Boolean experiment, Boolean active)
      throws UserFriendlyException {
    ExpProgram purpose = new ExpProgram();

    if (isDuplicate(hall, name)) {
      throw new UserFriendlyException("Program with name " + name + " already exists");
    }

    purpose.setHall(hall);
    purpose.setName(name);
    purpose.setAlias(alias);
    purpose.setUrl(url);
    purpose.setExperiment(experiment);
    purpose.setActive(active);

    create(purpose);
  }

  /**
   * Whether an experimenter shift or a tentative or published monthly schedule refers to the
   * program. Schedule days store each hall's program ID without a foreign key, so removing a
   * scheduled program would leave the schedule with an ID that no longer resolves to a name.
   *
   * @param programId The program ID
   * @return true if the program is in use
   */
  @PermitAll
  public boolean isInUse(BigInteger programId) {
    return isUsedByShift(programId) || isUsedBySchedule(programId);
  }

  private boolean isUsedByShift(BigInteger programId) {
    TypedQuery<Long> q =
        em.createQuery(
            "select count(a.hall) from ExpShift a where a.expProgram.expProgramId = :id",
            Long.class);

    q.setParameter("id", programId);

    Long count = q.getSingleResult();

    return count > 0;
  }

  private boolean isUsedBySchedule(BigInteger programId) {
    Query q =
        em.createNativeQuery(
            "select count(*) from BTM_OWNER.SCHEDULE_DAY where :id in "
                + "(HALL_A_PROGRAM_ID, HALL_B_PROGRAM_ID, HALL_C_PROGRAM_ID, HALL_D_PROGRAM_ID)");

    q.setParameter("id", programId);

    Number count = (Number) q.getSingleResult();

    return count.longValue() > 0;
  }

  @RolesAllowed({"cc", "btm-admin", "schcom"})
  public void remove(BigInteger programId) throws UserFriendlyException {
    ExpProgram purpose = find(programId);

    if (isInUse(programId)) {
      throw new UserFriendlyException(
          "You cannot remove a program which is already in use by an experimenter shift or a schedule.  Update the Active attribute instead.");
    }

    this.remove(purpose);
  }

  @RolesAllowed({"cc", "btm-admin", "schcom"})
  public void edit(
      BigInteger programId,
      String name,
      String alias,
      String url,
      Boolean experiment,
      Boolean active)
      throws UserFriendlyException {
    if (programId == null) {
      throw new UserFriendlyException("Program Id must not be empty");
    }

    ExpProgram purpose = find(programId);

    if (purpose == null) {
      throw new UserFriendlyException("Program with ID: " + programId + " not found");
    }

    purpose.setName(name);
    purpose.setAlias(alias);
    purpose.setUrl(url);
    purpose.setExperiment(experiment);
    purpose.setActive(active);
  }

  @PermitAll
  public Map<Integer, ExpProgram> findProgramByIdMap() {
    Map<Integer, ExpProgram> purposeMap = new HashMap<>();

    List<ExpProgram> purposeList = findAll();

    for (ExpProgram purpose : purposeList) {
      purposeMap.put(purpose.getExpProgramId().intValue(), purpose);
    }

    return purposeMap;
  }

  @PermitAll
  public Map<String, ExpProgram> findProgramByHallNameMap(Hall hall) {
    Map<String, ExpProgram> purposeMap = new HashMap<>();

    List<ExpProgram> purposeList = this.findByHall(hall, null);

    for (ExpProgram purpose : purposeList) {
      purposeMap.put(purpose.getName(), purpose);
    }

    return purposeMap;
  }
}
