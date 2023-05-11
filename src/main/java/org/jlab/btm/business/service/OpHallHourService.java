package org.jlab.btm.business.service;

import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import org.jlab.btm.business.service.epics.EpicsOpHallHourService;
import org.jlab.btm.business.util.HourUtil;
import org.jlab.btm.persistence.entity.OpHallHour;
import org.jlab.btm.persistence.entity.PdShiftPlan;
import org.jlab.btm.persistence.enumeration.DataSource;
import org.jlab.btm.persistence.projection.OpHallShiftAvailability;
import org.jlab.btm.persistence.projection.OpHallShiftTotals;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.DateIterator;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.enumeration.Hall;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
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
public class OpHallHourService extends AbstractService<OpHallHour> {

    private final static Logger logger = Logger.getLogger(OpHallHourService.class.getName());
    @EJB
    EpicsOpHallHourService epicsService;
    @PersistenceContext(unitName = "jbtaPU")
    private EntityManager em;

    public OpHallHourService() {
        super(OpHallHour.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @SuppressWarnings("unchecked")
    @PermitAll
    public List<OpHallHour> findInDatabase(Hall hall, Date start, Date end) {
        // The following don't work with daylight savings:
        // Use Date
        // Use Calendar
        // Instead, use string and specify date format.
        // Example test case: if start and end are both daylight savings date such as 1 AM Nov. 4 2018 EST

        /*TypedQuery<OpHallHour> query = em.createQuery(
                "select a from OpHallHour a where a.hall = :hall and a.dayAndHour between :start and :end",
                OpHallHour.class);

        query.setParameter("hall", hall);
        query.setParameter("start", start);
        query.setParameter("end", end);*/
        Query query = em.createNativeQuery("select * from OP_HALL_HOUR a where a.hall = :hall and a.day_and_hour between to_timestamp_tz(:start, 'YYYY-MM-DD HH24 TZD') and to_timestamp_tz(:end, 'YYYY-MM-DD HH24 TZD')", OpHallHour.class);

        String startStr = TimeUtil.formatDatabaseDateTimeTZ(start);
        String endStr = TimeUtil.formatDatabaseDateTimeTZ(end);

        query.setParameter("hall", hall.getLetter());
        query.setParameter("start", startStr);
        query.setParameter("end", endStr);

        return query.getResultList();
    }

    @PermitAll
    public List<OpHallHour> findInEpics(Hall hall, Date start, Date end) throws UserFriendlyException {
        try {
            return epicsService.find(hall, start, end);
        } catch (TimeoutException | InterruptedException | CAException e) {
            throw new UserFriendlyException("Unable to query EPICS", e);
        }
    }

    @PermitAll
    public List<OpHallHour> fillMissingHoursAndSetSource(Map<Date, OpHallHour> dbHourMap,
                                                         Map<Date, OpHallHour> epicsHourMap, Hall hall, Date start, Date end) {
        List<OpHallHour> filledList = new ArrayList<>();

        DateIterator iterator = new DateIterator(start, end,
                Calendar.HOUR_OF_DAY);

        OpHallHour hallHour;

        for (Date hour : iterator) {
            if (dbHourMap.containsKey(hour)) {
                hallHour = dbHourMap.get(hour);
                hallHour.setSource(DataSource.DATABASE);
            } else if (epicsHourMap.containsKey(hour)) {
                hallHour = epicsHourMap.get(hour);
                hallHour.setSource(DataSource.EPICS);
            } else {
                hallHour = new OpHallHour();
                hallHour.setDayAndHour(hour);
                hallHour.setHall(hall);
                hallHour.setSource(DataSource.NONE);
            }

            filledList.add(hallHour);
        }

        return filledList;
    }

    @PermitAll
    public OpHallShiftTotals calculateTotals(List<OpHallHour> hourList) {
        OpHallShiftTotals totals = new OpHallShiftTotals();

        if (hourList != null) {
            for (OpHallHour hour : hourList) {
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
    }

    @RolesAllowed({"cc", "oability"})
    public void editHallHours(Hall hall, Date[] hourArray, Short[] upArray, Short[] tuneArray,
                              Short[] bnrArray, Short[] downArray, Short[] offArray) throws UserFriendlyException {
        if (hourArray == null || upArray == null || tuneArray == null || bnrArray == null
                || downArray == null || offArray == null) {
            throw new UserFriendlyException("Some columns of data are missing");
        }

        if (hourArray.length == 0) {
            throw new UserFriendlyException("No data");
        }

        if (hourArray.length > 9) {
            throw new UserFriendlyException("Only a single shift of data can be edited at a time");
        }

        if (hourArray.length != upArray.length || hourArray.length != upArray.length
                || hourArray.length != tuneArray.length || hourArray.length != bnrArray.length
                || hourArray.length != downArray.length || hourArray.length
                != offArray.length) {
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

        List<OpHallHour> hourList = findInDatabase(hall, hourArray[0], hourArray[hourArray.length
                - 1]);
        Map<Date, OpHallHour> hourMap = HourUtil.createHourMap(hourList);

        // We probably could consolidate with previous loop... (performance vs maintainability)
        for (int i = 0; i < hourArray.length; i++) {
            Date hour = hourArray[i];
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy HH:mm z");
            logger.log(Level.FINEST, "Editing hall hour: {0}", dateFormat.format(hour));
            OpHallHour hallHour = hourMap.get(hour);

            int total = upArray[i] + tuneArray[i] + bnrArray[i] + downArray[i] + offArray[i];

            if (total != 3600) {
                SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
                throw new UserFriendlyException("Hour " + hourFormat.format(hourArray[i])
                        + " availability must total 1 hour");
            }

            if (hallHour == null) {
                hallHour = new OpHallHour();
                hallHour.setDayAndHour(hour);
                hallHour.setHall(hall);
            }

            hallHour.setUpSeconds(upArray[i]);
            hallHour.setTuneSeconds(tuneArray[i]);
            hallHour.setBnrSeconds(bnrArray[i]);
            hallHour.setDownSeconds(downArray[i]);
            hallHour.setOffSeconds(offArray[i]);

            edit(hallHour);
        }
    }

    @Override
    protected void edit(OpHallHour hour) {
        if (hour.getOpHallHourId() == null) {
            this.manualInsert(hour);
        } else {
            super.edit(hour);
        }
    }

    private OpHallHour manualInsert(OpHallHour hour) {

        String dayAndHourStr = TimeUtil.formatDatabaseDateTimeTZ(hour.getDayAndHour());

        Query idq = em.createNativeQuery("select op_hall_hour_id.nextval from dual");

        BigDecimal idDec = (BigDecimal) idq.getSingleResult();

        BigInteger id = idDec.toBigInteger();

        Query q = em.createNamedQuery("OpHallHour.insertNATIVE");

        q.setParameter("id", id);
        q.setParameter("hall", hour.getHall().getLetter());
        q.setParameter("dayAndHour", dayAndHourStr);
        q.setParameter("up", hour.getUpSeconds());
        q.setParameter("tune", hour.getTuneSeconds());
        q.setParameter("bnr", hour.getBnrSeconds());
        q.setParameter("down", hour.getDownSeconds());
        q.setParameter("off", hour.getOffSeconds());

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

    @PermitAll
    public List<OpHallShiftAvailability> getHallAvailablilityList(Date startHour, Date endHour,
                                                                  boolean queryEpics, PdShiftPlan plan) {
        OpHallShiftTotals hallAPdTotals = new OpHallShiftTotals();
        OpHallShiftTotals hallBPdTotals = new OpHallShiftTotals();
        OpHallShiftTotals hallCPdTotals = new OpHallShiftTotals();
        OpHallShiftTotals hallDPdTotals = new OpHallShiftTotals();

        if (plan != null) {
            hallAPdTotals.setUpSeconds(plan.getHallAUpSeconds());
            hallAPdTotals.setTuneSeconds(plan.getHallATuneSeconds());
            hallAPdTotals.setBnrSeconds(plan.getHallABnrSeconds());
            hallAPdTotals.setDownSeconds(plan.getHallADownSeconds());
            hallAPdTotals.setOffSeconds(plan.getHallAOffSeconds());

            hallBPdTotals.setUpSeconds(plan.getHallBUpSeconds());
            hallBPdTotals.setTuneSeconds(plan.getHallBTuneSeconds());
            hallBPdTotals.setBnrSeconds(plan.getHallBBnrSeconds());
            hallBPdTotals.setDownSeconds(plan.getHallBDownSeconds());
            hallBPdTotals.setOffSeconds(plan.getHallBOffSeconds());

            hallCPdTotals.setUpSeconds(plan.getHallCUpSeconds());
            hallCPdTotals.setTuneSeconds(plan.getHallCTuneSeconds());
            hallCPdTotals.setBnrSeconds(plan.getHallCBnrSeconds());
            hallCPdTotals.setDownSeconds(plan.getHallCDownSeconds());
            hallCPdTotals.setOffSeconds(plan.getHallCOffSeconds());

            hallDPdTotals.setUpSeconds(plan.getHallDUpSeconds());
            hallDPdTotals.setTuneSeconds(plan.getHallDTuneSeconds());
            hallDPdTotals.setBnrSeconds(plan.getHallDBnrSeconds());
            hallDPdTotals.setDownSeconds(plan.getHallDDownSeconds());
            hallDPdTotals.setOffSeconds(plan.getHallDOffSeconds());
        }

        List<OpHallShiftAvailability> hallAvailabilityList = new ArrayList<>();
        hallAvailabilityList.add(getHallAvailability(Hall.A, startHour, endHour, queryEpics, hallAPdTotals));
        hallAvailabilityList.add(getHallAvailability(Hall.B, startHour, endHour, queryEpics, hallBPdTotals));
        hallAvailabilityList.add(getHallAvailability(Hall.C, startHour, endHour, queryEpics, hallCPdTotals));
        hallAvailabilityList.add(getHallAvailability(Hall.D, startHour, endHour, queryEpics, hallDPdTotals));

        return hallAvailabilityList;
    }

    private OpHallShiftAvailability getHallAvailability(Hall hall, Date startHour, Date endHour,
                                                        boolean queryEpics, OpHallShiftTotals pdShiftTotals) {
        List<OpHallHour> dbHourList
                = findInDatabase(hall, startHour, endHour);
        List<OpHallHour> epicsHourList;

        if (queryEpics) {
            try {
                epicsHourList = findInEpics(hall, startHour, endHour);
            } catch (UserFriendlyException e) {
                logger.log(Level.FINEST, "Unable to obtain EPICS hall hour data", e);
                epicsHourList = new ArrayList<>();
            }
        } else {
            epicsHourList = new ArrayList<>();
        }

        Map<Date, OpHallHour> dbHourMap = HourUtil.createHourMap(dbHourList);
        Map<Date, OpHallHour> epicsHourMap = HourUtil.createHourMap(epicsHourList);
        List<OpHallHour> hourList = fillMissingHoursAndSetSource(dbHourMap,
                epicsHourMap, hall, startHour, endHour);

        OpHallShiftTotals totals = calculateTotals(hourList);
        OpHallShiftTotals epicsTotals = calculateTotals(epicsHourList);

        OpHallShiftAvailability availability = new OpHallShiftAvailability();
        availability.setHall(hall);
        availability.setHourList(hourList);
        availability.setEpicsHourList(epicsHourList);
        availability.setShiftTotals(totals);
        availability.setEpicsShiftTotals(epicsTotals);
        availability.setPdShiftTotals(pdShiftTotals);
        availability.setDbHourList(dbHourList);

        return availability;
    }
}
