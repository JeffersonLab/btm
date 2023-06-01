package org.jlab.btm.business.service;

import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import org.jlab.btm.business.service.epics.EpicsExpHourService;
import org.jlab.btm.business.util.HourUtil;
import org.jlab.btm.persistence.entity.ExpHallHour;
import org.jlab.btm.persistence.entity.ExpHallShiftPurpose;
import org.jlab.btm.persistence.entity.OpAccHour;
import org.jlab.btm.persistence.enumeration.DataSource;
import org.jlab.btm.persistence.projection.ExpHallHourTotals;
import org.jlab.btm.persistence.projection.ExpHallShiftAvailability;
import org.jlab.btm.persistence.projection.ExpHallShiftTotals;
import org.jlab.btm.persistence.projection.PhysicsSummaryTotals;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.DateIterator;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.enumeration.Hall;
import org.jlab.smoothness.persistence.util.JPAUtil;

import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ryans
 */
@Stateless
public class ExpHallHourService extends AbstractService<ExpHallHour> {

    private final static Logger logger = Logger.getLogger(ExpHallHourService.class.getName());

    @PersistenceContext(unitName = "btmPU")
    private EntityManager em;

    @EJB
    EpicsExpHourService epicsHourService;

    @EJB
    ExpSecurityRuleService ruleService;

    public ExpHallHourService() {
        super(ExpHallHour.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /**
     * WARNING WARNING WARING: "end" is INCLUSIVE here; unlike pretty much
     * everywhere else
     *
     * @param start
     * @param end
     * @return
     */
    @PermitAll
    public List<ExpHallShiftTotals> findExpHallShiftTotals(Date start, Date end) {
        String sql
                = "select hall, count(abu_seconds) - 1 as count, sum(abu_seconds) as abu_seconds, sum(banu_seconds) as banu_seconds, sum(bna_seconds) as bna_seconds, sum(acc_seconds) as acc_seconds, sum(off_seconds) as off_seconds "
                + "from (select hall, abu_seconds, banu_seconds, bna_seconds, acc_seconds, off_seconds from exp_hall_hour where day_and_hour between :start and :end "
                + "union all select 'A', 0, 0, 0, 0, 0 from dual "
                + "union all select 'B', 0, 0, 0, 0, 0 from dual "
                + "union all select 'C', 0, 0, 0, 0, 0 from dual "
                + "union all select 'D', 0, 0, 0, 0, 0 from dual) "
                + "group by hall order by hall asc";

        Query q = em.createNativeQuery(sql);

        q.setParameter("start", start);
        q.setParameter("end", end);

        List<ExpHallShiftTotals> totalsList = JPAUtil.getResultList(q, ExpHallShiftTotals.class);

        return totalsList;
    }

    @PermitAll
    public List<ExpHallHourTotals> findExpHallHourTotals(Date start, Date end) {
        String sql
                = "select hall, count(abu_seconds) - 1 as count, sum(abu_seconds) as abu_seconds, sum(banu_seconds) as banu_seconds, sum(bna_seconds) as bna_seconds, sum(acc_seconds) as acc_seconds, sum(off_seconds) as off_seconds, sum(er_seconds) as er_seconds, sum(pcc_seconds) as pcc_seconds, sum(ued_seconds) as ued_seconds "
                + "from (select hall, abu_seconds, banu_seconds, bna_seconds, acc_seconds, off_seconds, er_seconds, pcc_seconds, ued_seconds from exp_hall_hour where day_and_hour >= :start and day_and_hour < :end "
                + "union all select 'A', 0, 0, 0, 0, 0, 0, 0, 0 from dual "
                + "union all select 'B', 0, 0, 0, 0, 0, 0, 0, 0 from dual "
                + "union all select 'C', 0, 0, 0, 0, 0, 0, 0, 0 from dual "
                + "union all select 'D', 0, 0, 0, 0, 0, 0, 0, 0 from dual) "
                + "group by hall order by hall asc";

        Query q = em.createNativeQuery(sql);

        q.setParameter("start", start);
        q.setParameter("end", end);

        List<ExpHallHourTotals> totalsList = JPAUtil.getResultList(q, ExpHallHourTotals.class);

        return totalsList;
    }

    @PermitAll
    public List<PhysicsSummaryTotals> reportTotals(Date start, Date end) {
        Query q = em.createNativeQuery(
                "select a.hall, abu, banu, bna, acc, exp_off, up, tune, bnr, down, op_off from "
                        + "("
                        + "select hall, sum(abu_seconds) as abu, sum(banu_seconds) as banu, sum(bna_seconds) as bna, sum(acc_seconds) as acc, sum(off_seconds) as exp_off from "
                        + "(select hall, abu_seconds, banu_seconds, bna_seconds, acc_seconds, off_seconds "
                        + "from exp_hall_hour " + "where day_and_hour >= :start and day_and_hour < :end "
                        + "union all select 'A', 0, 0, 0, 0, 0 from dual "
                        + "union all select 'B', 0, 0, 0, 0, 0 from dual "
                        + "union all select 'C', 0, 0, 0, 0, 0 from dual "
                        + "union all select 'D', 0, 0, 0, 0, 0 from dual) " + "group by hall " + ") a, "
                        + "("
                        + "select hall, sum(up_seconds) as up, sum(tune_seconds) as tune, sum(bnr_seconds) as bnr, sum(down_seconds) as down, sum(off_seconds) as op_off from "
                        + "(select hall, up_seconds, tune_seconds, bnr_seconds, down_seconds, off_seconds "
                        + "from op_hall_hour " + "where day_and_hour >= :start and day_and_hour < :end "
                        + "union all select 'A', 0, 0, 0, 0, 0 from dual "
                        + "union all select 'B', 0, 0, 0, 0, 0 from dual "
                        + "union all select 'C', 0, 0, 0, 0, 0 from dual "
                        + "union all select 'D', 0, 0, 0, 0, 0 from dual) " + "group by hall " + ") b "
                        + "where a.hall = b.hall order by a.hall asc");

        q.setParameter("start", start);
        q.setParameter("end", end);

        return JPAUtil.getResultList(q, PhysicsSummaryTotals.class);
    }

    @PermitAll
    public ExpHallShiftAvailability getHallAvailability(Hall hall, Date startHour, Date endHour,
                                                         boolean queryEpics) {
        List<ExpHallHour> dbHourList
                = findInDatabase(hall, startHour, endHour);
        List<ExpHallHour> epicsHourList;

        if (queryEpics) {
            try {
                epicsHourList = findInEpics(hall, startHour, endHour, true);
            } catch (UserFriendlyException e) {
                logger.log(Level.FINEST, "Unable to obtain EPICS hall hour data", e);
                epicsHourList = new ArrayList<>();
            }
        } else {
            epicsHourList = new ArrayList<>();
        }

        Map<Date, ExpHallHour> dbHourMap = HourUtil.createHourMap(dbHourList);
        Map<Date, ExpHallHour> epicsHourMap = HourUtil.createHourMap(epicsHourList);
        List<ExpHallHour> hourList = fillMissingHoursAndSetSource(dbHourMap,
                epicsHourMap, hall, startHour, endHour);

        ExpHallHourTotals totals = calculateTotals(hall, hourList);
        ExpHallHourTotals epicsTotals = calculateTotals(hall, epicsHourList);

        ExpHallShiftAvailability availability = new ExpHallShiftAvailability();
        availability.setHall(hall);
        availability.setHourList(hourList);
        availability.setEpicsHourList(epicsHourList);
        availability.setShiftTotals(totals);
        availability.setEpicsShiftTotals(epicsTotals);
        availability.setDbHourList(dbHourList);

        return availability;
    }

    @PermitAll
    public List<ExpHallShiftAvailability> findAvailability(Date startHour, Date endHour) {
        List<ExpHallShiftAvailability> availList = new ArrayList<>();

        ExpHallShiftAvailability hallAAvail = getHallAvailability(Hall.A, startHour, endHour, false);
        ExpHallShiftAvailability hallBAvail = getHallAvailability(Hall.B, startHour, endHour, false);
        ExpHallShiftAvailability hallCAvail = getHallAvailability(Hall.C, startHour, endHour, false);
        ExpHallShiftAvailability hallDAvail = getHallAvailability(Hall.D, startHour, endHour, false);

        availList.add(hallAAvail);
        availList.add(hallBAvail);
        availList.add(hallCAvail);
        availList.add(hallDAvail);

        return availList;
    }

    @PermitAll
    public List<ExpHallHour> fillMissingHoursAndSetSource(Map<Date, ExpHallHour> dbHourMap,
                                                          Map<Date, ExpHallHour> epicsHourMap, Hall hall, Date start, Date end) {
        List<ExpHallHour> filledList = new ArrayList<>();

        DateIterator iterator = new DateIterator(start, end,
                Calendar.HOUR_OF_DAY);

        ExpHallHour hallHour;

        for (Date hour : iterator) {
            if (dbHourMap.containsKey(hour)) {
                hallHour = dbHourMap.get(hour);
                hallHour.setSource(DataSource.DATABASE);
            } else if (epicsHourMap.containsKey(hour)) {
                hallHour = epicsHourMap.get(hour);
                hallHour.setSource(DataSource.EPICS);
            } else {
                hallHour = new ExpHallHour();
                hallHour.setDayAndHour(hour);
                hallHour.setHall(hall);
                hallHour.setSource(DataSource.NONE);
            }

            filledList.add(hallHour);
        }

        return filledList;
    }

    public ExpHallHourTotals calculateTotals(Hall hall, List<ExpHallHour> hourList) {
        Integer abuSeconds = 0;
        Integer banuSeconds = 0;
        Integer bnaSeconds = 0;
        Integer accSeconds = 0;
        Integer offSeconds = 0;
        Integer erSeconds = 0;
        Integer pccSeconds = 0;
        Integer uedSeconds = 0;

        if (hourList != null) {
            for (ExpHallHour hour : hourList) {
                abuSeconds = abuSeconds + hour.getAbuSeconds();
                banuSeconds = banuSeconds + hour.getBanuSeconds();
                bnaSeconds = bnaSeconds + hour.getBnaSeconds();
                accSeconds = accSeconds + hour.getAccSeconds();
                offSeconds = offSeconds + hour.getOffSeconds();
                erSeconds = erSeconds + hour.getErSeconds();
                pccSeconds = pccSeconds + hour.getPccSeconds();
                uedSeconds = uedSeconds + hour.getUedSeconds();
            }
        }

        return new ExpHallHourTotals(hall.getLetter(), hourList.size(), abuSeconds, banuSeconds, bnaSeconds, accSeconds, offSeconds, erSeconds, pccSeconds, uedSeconds);
    }

    /**
     * Fetch the experimenter hall hours for the specified hall, start day and
     * hour, and end dayAndHour from the database.
     *
     * @param hall            the hall.
     * @param startDayAndHour the start day and hour.
     * @param endDayAndHour   the end day and hour.
     * @return the list of experimenter hall hours.
     */
    @PermitAll
    public List<ExpHallHour> findInDatabase(Hall hall, Date startDayAndHour,
                                            Date endDayAndHour) {
        TypedQuery<ExpHallHour> q = em.createNamedQuery("ExpHallHour.findByHallAndHourRange", ExpHallHour.class);

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        start.setTime(startDayAndHour);
        end.setTime(endDayAndHour);

        q.setParameter("hall", hall);
        q.setParameter("startDayAndHourCal", start);
        q.setParameter("endDayAndHourCal", end);

        logger.log(Level.FINEST, "ExpHallHourFacade.findInDatabase: {} - {}", new Object[]{startDayAndHour, endDayAndHour});
        List<ExpHallHour> hours = q.getResultList();
        logger.log(Level.FINEST, "ExpHallHourFacade.findInDatabase: Found: {}", hours.size());

        /*for(ExpHallHour hour: hours) {
            System.out.println(TimeHelper.formatDetailHour(hour.getDayAndHour()));
        }*/
        return hours;
    }

    /**
     * Fetch the experimenter hall hours for the specified hall, start day and
     * hour, and end day and hour from EPICS, optionally rounded.
     *
     * @param hall the hall.
     * @param startDayAndHour the start day and hour.
     * @param endDayAndHour the end day and hour.
     * @param round whether or not to round data.
     * @return the list of experimenter hall hours.
     * @throws UserFriendlyException if unable to fetch the hours.
     */
    @PermitAll
    public List<ExpHallHour> findInEpics(Hall hall, Date startDayAndHour, 
    Date endDayAndHour, boolean round) throws UserFriendlyException {
        
        List<ExpHallHour> hours = null;
        
        try {
            hours = epicsHourService.loadAccounting(hall, startDayAndHour,
                    endDayAndHour, round);
        } catch (CAException | TimeoutException | InterruptedException e) {
            throw new UserFriendlyException("Unable to query EPICS", e);
        }
        
        return hours;
    }

    @PermitAll
    public void editExpHours(Hall hall, Date[] hourArray, Short[] abuArray, Short[] banuArray, Short[] bnaArray, Short[] accArray, Short[] offArray, Short[] erArray, Short[] pccArray, Short[] uedArray, String[] commentsArray) throws UserFriendlyException {
        if (hourArray == null || abuArray == null || banuArray == null || bnaArray == null
                    || accArray == null || offArray == null || erArray == null || pccArray == null || uedArray == null || commentsArray == null) {
                throw new UserFriendlyException("Some columns of data are missing");
            }

            if (hourArray.length == 0) {
                throw new UserFriendlyException("No data");
            }

            if (hourArray.length > 9) {
                throw new UserFriendlyException("Only a single shift of data can be edited at a time");
            }

            if (hourArray.length != abuArray.length ||
                hourArray.length != banuArray.length ||
                hourArray.length != bnaArray.length ||
                hourArray.length != accArray.length ||
                hourArray.length != offArray.length ||
                hourArray.length != erArray.length ||
                hourArray.length != pccArray.length ||
                hourArray.length != uedArray.length ||
                hourArray.length != commentsArray.length) {
                throw new UserFriendlyException("Column data does not line up in equal number of rows");
            }

            Date previous = null;
            for (Date hour : hourArray) {
                if (previous == null) {
                    previous = hour;
                } else {
                    if (hour == null) {
                        throw new UserFriendlyException("Day and hour cannot be empty");
                    }
                    if (!TimeUtil.addHours(hour, -1).equals(previous)) {
                        throw new UserFriendlyException("Hours must be contiguous");
                    }
                    previous = hour;
                }
            }

            List<ExpHallHour> hourList = findInDatabase(hall, hourArray[0], hourArray[hourArray.length - 1]);
            Map<Date, ExpHallHour> hourMap = HourUtil.createHourMap(hourList);


            ruleService.editCheck(hall, hourArray[0]);

            // We probably could consolidate with previous loop... (performance vs maintainability)
            for (int i = 0; i < hourArray.length; i++) {
                Date hour = hourArray[i];

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy HH:mm z");
                logger.log(Level.FINEST, "Editing hour: {0}", dateFormat.format(hour));
                ExpHallHour expHour = hourMap.get(hour);

                int total = abuArray[i] + banuArray[i] + bnaArray[i] + accArray[i] + offArray[i];

                if (total != 3600) {
                    SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
                    throw new UserFriendlyException("Hour " + hourFormat.format(hourArray[i])
                            + " accelerator beam time must total 1 hour");
                }

                total = erArray[i] + pccArray[i] + uedArray[i] + offArray[i];

                if (total != 3600) {
                    SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
                    throw new UserFriendlyException("Hour " + hourFormat.format(hourArray[i])
                            + " experiment beam time must total 1 hour");
                }

                if (expHour == null) {
                    expHour = new ExpHallHour();
                    expHour.setDayAndHour(hour);
                    expHour.setHall(hall);
                }

                expHour.setAbuSeconds(abuArray[i]);
                expHour.setBanuSeconds(banuArray[i]);
                expHour.setBnaSeconds(bnaArray[i]);
                expHour.setAccSeconds(accArray[i]);
                expHour.setOffSeconds(offArray[i]);
                expHour.setErSeconds(erArray[i]);
                expHour.setPccSeconds(pccArray[i]);
                expHour.setUedSeconds(uedArray[i]);
                expHour.setRemark(commentsArray[i]);

                edit(expHour);
            }
    }

    @Override
    protected void edit(ExpHallHour hour) {
        if (hour.getExpHallHourId() == null) {
            this.manualInsert(hour);
        } else {
            super.edit(hour);
        }
    }

    private ExpHallHour manualInsert(ExpHallHour hour) {

        String dayAndHourStr = TimeUtil.formatDatabaseDateTimeTZ(hour.getDayAndHour());

        Query idq = em.createNativeQuery("select exp_hall_hour_id.nextval from dual");

        BigDecimal idDec = (BigDecimal) idq.getSingleResult();

        BigInteger id = idDec.toBigInteger();

        Query q = em.createNamedQuery("ExpHallHour.insertNATIVE");

        q.setParameter("id", id);
        q.setParameter("hall", hour.getHall().getLetter());
        q.setParameter("dayAndHour", dayAndHourStr);
        q.setParameter("abu", hour.getAbuSeconds());
        q.setParameter("banu", hour.getBanuSeconds());
        q.setParameter("bna", hour.getBnaSeconds());
        q.setParameter("acc", hour.getAccSeconds());
        q.setParameter("off", hour.getOffSeconds());
        q.setParameter("er", hour.getErSeconds());
        q.setParameter("pcc", hour.getPccSeconds());
        q.setParameter("ued", hour.getUedSeconds());
        q.setParameter("remark", hour.getRemark());

        int count = q.executeUpdate();

        if (count == 0) {
            logger.log(Level.WARNING, "Insert count is zero");
        }

        hour = this.find(id);

        if (hour == null) {
            logger.log(Level.WARNING, "Unable to find hour after insert");
        }

        return hour;
    }
}
