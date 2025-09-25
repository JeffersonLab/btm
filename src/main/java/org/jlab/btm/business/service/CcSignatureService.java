package org.jlab.btm.business.service;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.btm.business.util.CALoadException;
import org.jlab.btm.persistence.entity.*;
import org.jlab.btm.persistence.enumeration.Role;
import org.jlab.btm.persistence.projection.*;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * @author ryans
 */
@Stateless
public class CcSignatureService extends AbstractService<CcSignature> {

  private static final Logger logger = Logger.getLogger(CcSignatureService.class.getName());

  @EJB CcAccHourService accHourService;
  @EJB CcHallHourService hallHourService;
  @EJB CcMultiplicityHourService multiplicityHourService;
  @EJB ExpHourService expHourService;
  @EJB CcShiftService ccShiftService;
  @EJB CcCrossCheckCommentService crossCheckCommentService;
  @EJB CcMultiplicityHourService multiHourService;
  @EJB CcShiftService shiftService;
  @EJB PdShiftPlanService planService;
  @EJB DowntimeService downService;

  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  public CcSignatureService() {
    super(CcSignature.class);
  }

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  @PermitAll
  public List<CcSignature> find(Date startDayAndHour) {
    TypedQuery<CcSignature> query =
        em.createQuery(
            "select a from CcSignature a where a.startDayAndHour = :startDayAndHour order by a.signedDate asc",
            CcSignature.class);

    query.setParameter("startDayAndHour", startDayAndHour);

    return query.getResultList();
  }

  @PermitAll
  public CcTimesheetStatus calculateStatus(
      Date startDayAndHour,
      Date endDayAndHour,
      List<CcAccHour> accHourList,
      List<CcHallHour> hallAHourList,
      List<CcHallHour> hallBHourList,
      List<CcHallHour> hallCHourList,
      List<CcHallHour> hallDHourList,
      List<CcMultiplicityHour> multiHourList,
      CcShift shift,
      List<CcSignature> signatureList) {
    CcTimesheetStatus status = new CcTimesheetStatus();

    long hoursInShift = TimeUtil.differenceInHours(startDayAndHour, endDayAndHour) + 1;

    if (accHourList != null && accHourList.size() == hoursInShift) {
      status.setAcceleratorComplete(true);
    }

    if (hallAHourList != null && hallAHourList.size() == hoursInShift) {
      status.setHallAComplete(true);
    }

    if (hallBHourList != null && hallBHourList.size() == hoursInShift) {
      status.setHallBComplete(true);
    }

    if (hallCHourList != null && hallCHourList.size() == hoursInShift) {
      status.setHallCComplete(true);
    }

    if (hallDHourList != null && hallDHourList.size() == hoursInShift) {
      status.setHallDComplete(true);
    }

    if (multiHourList != null && multiHourList.size() == hoursInShift) {
      status.setMultiplicityComplete(true);
    }

    if (shift != null) {
      status.setShiftInfoComplete(true);
    }

    if (signatureList != null && !signatureList.isEmpty()) {
      status.setSignatureComplete(true);
    }

    return status;
  }

  @PermitAll
  public CcTimesheetStatus calculateStatus(Date startDayAndHour) {

    Date endDayAndHour = TimeUtil.calculateCrewChiefShiftEndDayAndHour(startDayAndHour);

    List<CcAccHour> accHourList = accHourService.findInDatabase(startDayAndHour, endDayAndHour);

    List<CcHallHour> hallAHourList =
        hallHourService.findInDatabase(Hall.A, startDayAndHour, endDayAndHour);
    List<CcHallHour> hallBHourList =
        hallHourService.findInDatabase(Hall.B, startDayAndHour, endDayAndHour);
    List<CcHallHour> hallCHourList =
        hallHourService.findInDatabase(Hall.C, startDayAndHour, endDayAndHour);
    List<CcHallHour> hallDHourList =
        hallHourService.findInDatabase(Hall.D, startDayAndHour, endDayAndHour);

    List<CcMultiplicityHour> multiHourList =
        multiHourService.findInDatabase(startDayAndHour, endDayAndHour);

    CcShift shift = shiftService.findInDatabase(startDayAndHour);

    List<CcSignature> signatureList = find(startDayAndHour);

    return this.calculateStatus(
        startDayAndHour,
        endDayAndHour,
        accHourList,
        hallAHourList,
        hallBHourList,
        hallCHourList,
        hallDHourList,
        multiHourList,
        shift,
        signatureList);
  }

  @RolesAllowed({"cc", "btm-admin"})
  public void signTimesheet(Date startDayAndHour) throws UserFriendlyException {
    Role role = Role.CREW_CHIEF;

    if (context.isCallerInRole("btm-admin")) {
      role = Role.OPERABILITY_MANAGER;
    }

    String username = context.getCallerPrincipal().getName();

    List<CcSignature> signatureList = find(startDayAndHour);

    for (CcSignature sig : signatureList) {
      if (sig.getStartDayAndHour().getTime() == startDayAndHour.getTime()
          && sig.getSignedBy().equals(username)
          && sig.getSignedRole() == role) {
        throw new UserFriendlyException("User has already signed the timesheet");
      }
    }

    CcTimesheetStatus status = this.calculateStatus(startDayAndHour);

    if (!status.isAcceleratorComplete()) {
      throw new UserFriendlyException("You must save all accelerator availability hours");
    }

    if (!status.isHallAComplete()) {
      throw new UserFriendlyException("You must save all Hall A availability hours");
    }

    if (!status.isHallBComplete()) {
      throw new UserFriendlyException("You must save all Hall B availability hours");
    }

    if (!status.isHallCComplete()) {
      throw new UserFriendlyException("You must save all Hall C availability hours");
    }

    if (!status.isHallDComplete()) {
      throw new UserFriendlyException("You must save all Hall D availability hours");
    }

    if (!status.isMultiplicityComplete()) {
      throw new UserFriendlyException("You must save all multiplicity hours");
    }

    if (!status.isShiftInfoComplete()) {
      throw new UserFriendlyException("You must save shift information");
    }

    CcSignature signature = new CcSignature();
    signature.setStartDayAndHour(startDayAndHour);
    signature.setSignedDate(new Date());
    signature.setSignedRole(role);
    signature.setSignedBy(username);

    create(signature);
  }

  @PermitAll
  public void populateRequestAttributes(
      HttpServletRequest request, Date startHour, Date endHour, Date startOfNextShift) {
    PdShiftPlan plan = planService.findInDatabase(startHour);

    /*ACCELERATOR AVAILABILITY*/
    AcceleratorShiftAvailability accAvailability =
        accHourService.getAcceleratorAvailability(startHour, endHour, true, plan);

    /*HALL AVAILABILITY*/
    List<CcHallShiftAvailability> hallAvailabilityList =
        hallHourService.getHallAvailablilityList(startHour, endHour, true, plan);

    List<List<CcHallHour>> hallHoursList = new ArrayList<>();
    hallHoursList.add(hallAvailabilityList.get(0).getEpicsHourList());
    hallHoursList.add(hallAvailabilityList.get(1).getEpicsHourList());
    hallHoursList.add(hallAvailabilityList.get(2).getEpicsHourList());
    hallHoursList.add(hallAvailabilityList.get(3).getEpicsHourList());

    /*MULTIPLICITY AVAILABILITY*/
    MultiplicityShiftAvailability multiplicityAvailability =
        multiplicityHourService.getMultiShiftAvailability(startHour, endHour, true, hallHoursList);

    /*EXPERIMENTAL HALL PERSPECTIVE*/
    List<ExpShiftTotals> expHallHourTotalsList =
        expHourService.findExpHallShiftTotals(startHour, endHour);

    List<ExpShiftAvailability> expHallAvailabilityList =
        expHourService.findAvailability(startHour, endHour);

    /*SHIFT INFORMATION*/
    CcShift dbShiftInfo = ccShiftService.findInDatabase(startHour);
    CcShift epicsShiftInfo = null;

    try {
      epicsShiftInfo = ccShiftService.findInEpics(startHour);
    } catch (CALoadException e) {
      logger.log(Level.INFO, "CALoadException: " + e.getMessage());
      logger.log(Level.FINEST, "Unable to obtain EPICS cc shift info", e);
      epicsShiftInfo = new CcShift();
    }

    CcShift shiftInfo = dbShiftInfo;

    if (dbShiftInfo == null) {
      shiftInfo = epicsShiftInfo;
    }

    /*CROSS CHECK COMMENT*/
    CcCrossCheckComment crossCheckComment = crossCheckCommentService.findInDatabase(startHour);

    /*SIGNATURES*/
    List<CcSignature> signatureList = this.find(startHour);
    CcTimesheetStatus status =
        this.calculateStatus(
            startHour,
            endHour,
            accAvailability.getDbHourList(),
            hallAvailabilityList.get(0).getDbHourList(),
            hallAvailabilityList.get(1).getDbHourList(),
            hallAvailabilityList.get(2).getDbHourList(),
            hallAvailabilityList.get(3).getDbHourList(),
            multiplicityAvailability.getDbHourList(),
            dbShiftInfo,
            signatureList);

    boolean editable = request.isUserInRole("btm-admin") || request.isUserInRole("cc");

    /*Cross Check*/
    HourlyCrossCheckService crossCheckService = new HourlyCrossCheckService();
    List<HallHourCrossCheck> hallAHourCrossCheckList =
        crossCheckService.getHourList(
            Hall.A,
            accAvailability,
            multiplicityAvailability,
            hallAvailabilityList.get(0),
            expHallAvailabilityList.get(0));
    List<HallHourCrossCheck> hallBHourCrossCheckList =
        crossCheckService.getHourList(
            Hall.B,
            accAvailability,
            multiplicityAvailability,
            hallAvailabilityList.get(1),
            expHallAvailabilityList.get(1));
    List<HallHourCrossCheck> hallCHourCrossCheckList =
        crossCheckService.getHourList(
            Hall.C,
            accAvailability,
            multiplicityAvailability,
            hallAvailabilityList.get(2),
            expHallAvailabilityList.get(2));
    List<HallHourCrossCheck> hallDHourCrossCheckList =
        crossCheckService.getHourList(
            Hall.D,
            accAvailability,
            multiplicityAvailability,
            hallAvailabilityList.get(3),
            expHallAvailabilityList.get(3));

    CcBeamModeCrossCheck modeCrossCheck =
        new CcBeamModeCrossCheck(
            accAvailability.getShiftTotals(),
            hallAvailabilityList.get(0).getShiftTotals(),
            hallAvailabilityList.get(1).getShiftTotals(),
            hallAvailabilityList.get(2).getShiftTotals(),
            hallAvailabilityList.get(3).getShiftTotals());

    CcAcceleratorCrossCheck accCrossCheck =
        new CcAcceleratorCrossCheck(
            accAvailability.getShiftTotals(),
            expHallHourTotalsList.get(0),
            expHallHourTotalsList.get(1),
            expHallHourTotalsList.get(2),
            expHallHourTotalsList.get(3));

    CcHallCrossCheck hallCrossCheck =
        new CcHallCrossCheck(
            hallAvailabilityList.get(0).getShiftTotals(),
            hallAvailabilityList.get(1).getShiftTotals(),
            hallAvailabilityList.get(2).getShiftTotals(),
            hallAvailabilityList.get(3).getShiftTotals(),
            expHallHourTotalsList.get(0),
            expHallHourTotalsList.get(1),
            expHallHourTotalsList.get(2),
            expHallHourTotalsList.get(3));

    CcMultiplicityCrossCheck multiCrossCheck =
        new CcMultiplicityCrossCheck(
            hallAvailabilityList.get(0).getShiftTotals(),
            hallAvailabilityList.get(1).getShiftTotals(),
            hallAvailabilityList.get(2).getShiftTotals(),
            hallAvailabilityList.get(3).getShiftTotals(),
            multiplicityAvailability.getShiftTotals());

    // Downtime check
    DowntimeSummaryTotals dtmTotals = downService.reportTotals(startHour, startOfNextShift);

    CcDowntimeCrossCheck downCrossCheck =
        new CcDowntimeCrossCheck(accAvailability.getShiftTotals(), dtmTotals.getEventSeconds());

    request.setAttribute("plan", plan);
    request.setAttribute("accAvailability", accAvailability);
    request.setAttribute("hallAvailabilityList", hallAvailabilityList);
    request.setAttribute("multiplicityAvailability", multiplicityAvailability);
    request.setAttribute("expHallHourTotalsList", expHallHourTotalsList);
    request.setAttribute("expHallAvailabilityList", expHallAvailabilityList);
    request.setAttribute("crossCheckComment", crossCheckComment);
    request.setAttribute("shiftInfo", shiftInfo);
    request.setAttribute("epicsShiftInfo", epicsShiftInfo);
    request.setAttribute("signatureList", signatureList);
    request.setAttribute("status", status);
    request.setAttribute("editable", editable);
    request.setAttribute("modeCrossCheck", modeCrossCheck);
    request.setAttribute("accCrossCheck", accCrossCheck);
    request.setAttribute("hallCrossCheck", hallCrossCheck);
    request.setAttribute("multiCrossCheck", multiCrossCheck);
    request.setAttribute("downCrossCheck", downCrossCheck);
    request.setAttribute("dtmTotals", dtmTotals);
    request.setAttribute("hallAHourCrossCheckList", hallAHourCrossCheckList);
    request.setAttribute("hallBHourCrossCheckList", hallBHourCrossCheckList);
    request.setAttribute("hallCHourCrossCheckList", hallCHourCrossCheckList);
    request.setAttribute("hallDHourCrossCheckList", hallDHourCrossCheckList);

    // DEBUG
    /*for(int i = 0; i < hallAvailabilityList.size(); i++) {
        CcHallShiftAvailability ha = hallAvailabilityList.get(i);

        boolean mode = modeCrossCheck.getHallPassed()[i];
        boolean acc = accCrossCheck.getHallPassed()[i];
        boolean hall = hallCrossCheck.getHallPassed()[i];
        boolean multi = multiCrossCheck.getHallPassed()[i];

        System.err.println("mode: " + mode + ", acc: " + acc + ", hall: " + hall + ", multi: " + multi);
    }*/
  }
}
