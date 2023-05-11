package org.jlab.btm.business.service;

import org.jlab.btm.business.util.HourUtil;
import org.jlab.btm.persistence.entity.ExpHallHour;
import org.jlab.btm.persistence.enumeration.DataSource;
import org.jlab.btm.persistence.projection.ExpHallHourTotals;
import org.jlab.btm.persistence.projection.ExpHallShiftAvailability;
import org.jlab.btm.persistence.projection.ExpHallShiftTotals;
import org.jlab.btm.persistence.projection.PhysicsSummaryTotals;
import org.jlab.smoothness.business.util.DateIterator;
import org.jlab.smoothness.persistence.enumeration.Hall;
import org.jlab.smoothness.persistence.util.JPAUtil;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ryans
 */
@Stateless
public class ExpHallHourService {

    private final static Logger logger = Logger.getLogger(ExpHallHourService.class.getName());

    @PersistenceContext(unitName = "jbtaPU")
    private EntityManager em;

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

    private ExpHallShiftAvailability getHallAvailability(Hall hall, Date startHour, Date endHour,
                                                         boolean queryEpics) {
        List<ExpHallHour> dbHourList
                = findInDatabase(hall, startHour, endHour);
        List<ExpHallHour> epicsHourList;

        /*if (queryEpics) {
            try {
                epicsHourList = findInEpics(hall, startHour, endHour);
            } catch (UserFriendlyException e) {
                logger.log(Level.FINEST, "Unable to obtain EPICS hall hour data", e);
                epicsHourList = new ArrayList<>();
            }
        } else {*/
        epicsHourList = new ArrayList<>();
        //}

        Map<Date, ExpHallHour> dbHourMap = HourUtil.createHourMap(dbHourList);
        Map<Date, ExpHallHour> epicsHourMap = HourUtil.createHourMap(epicsHourList);
        List<ExpHallHour> hourList = fillMissingHoursAndSetSource(dbHourMap,
                epicsHourMap, hall, startHour, endHour);

        //ExpHallShiftTotals totals = calculateTotals(hourList);
        //ExpHallShiftTotals epicsTotals = calculateTotals(epicsHourList);

        ExpHallShiftAvailability availability = new ExpHallShiftAvailability();
        availability.setHall(hall);
        availability.setHourList(hourList);
        availability.setEpicsHourList(epicsHourList);
        //availability.setShiftTotals(totals);
        //availability.setEpicsShiftTotals(epicsTotals);
        //availability.setPdShiftTotals(pdShiftTotals);
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

    /*public ExpHallShiftTotals calculateTotals(List<ExpHallHour> hourList) {
        ExpHallShiftTotals totals = new ExpHallShiftTotals();

        if (hourList != null) {
            for (ExpHallHour hour : hourList) {
                totals.setUpSeconds(
                        totals.getUpSeconds() == null ? hour.getUpSeconds() : totals.getUpSeconds()
                        + hour.getUpSeconds());
                totals.setTuneSeconds(
                        totals.getTuneSeconds() == null ? hour.getTuneSeconds() : totals.getTuneSeconds()
                        + hour.getTuneSeconds());
                totals.setBnrSeconds(
                        totals.getBnrSeconds() == null ? hour.getBnrSeconds() : totals.getBnrSeconds()
                        + hour.getBnrSeconds());
                totals.setDownSeconds(
                        totals.getDownSeconds() == null ? hour.getDownSeconds() : totals.getDownSeconds()
                        + hour.getDownSeconds());
                totals.setOffSeconds(
                        totals.getOffSeconds() == null ? hour.getOffSeconds() : totals.getOffSeconds()
                        + hour.getOffSeconds());
            }
        }

        return totals;
    }    */

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
     * @throws FindInEPICSException if unable to fetch the hours.
     */
    /*@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List<ExpHallHour> findInEpics(Hall hall, Date startDayAndHour, 
    Date endDayAndHour, boolean round) throws FindInEPICSException {
        
        List<ExpHallHour> hours = null;
        
        String errorMsg = "Unable to find hours in Epics for hall " + hall;
        
        try {
            hours = epicsManager.loadAccounting(hall, startDayAndHour,
                    endDayAndHour, round);
        } catch (TimeoutException ex) {
            throw new FindInEPICSException(errorMsg, ex);
        } catch (InterruptedException ex) {
            throw new FindInEPICSException(errorMsg, ex);
        } catch (CAException ex) {
            throw new FindInEPICSException(errorMsg, ex);
        }
        
        return hours;
    }    */
}
