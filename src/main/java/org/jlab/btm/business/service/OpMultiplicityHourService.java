package org.jlab.btm.business.service;

import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import org.jlab.btm.business.service.epics.EpicsOpMultiplicityHourService;
import org.jlab.btm.business.util.HourUtil;
import org.jlab.btm.persistence.entity.OpHallHour;
import org.jlab.btm.persistence.entity.OpMultiplicityHour;
import org.jlab.btm.persistence.projection.MultiplicityShiftAvailability;
import org.jlab.btm.persistence.projection.MultiplicitySummaryTotals;
import org.jlab.btm.persistence.projection.OpMultiplicityShiftTotals;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.util.JPAUtil;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ryans
 */
@Stateless
public class OpMultiplicityHourService extends AbstractService<OpMultiplicityHour> {

    private final static Logger logger = Logger.getLogger(OpMultiplicityHourService.class.getName());
    @EJB
    EpicsOpMultiplicityHourService epicsService;
    @PersistenceContext(unitName = "btmPU")
    private EntityManager em;

    public OpMultiplicityHourService() {
        super(OpMultiplicityHour.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @SuppressWarnings("unchecked")
    @PermitAll
    public List<OpMultiplicityHour> findInDatabase(Date start, Date end) {
        // The following don't work with daylight savings:
        // Use Date
        // Use Calendar
        // Instead, use string and specify date format.
        // Example test case: if start and end are both daylight savings date such as 1 AM Nov. 4 2018 EST
        
        /*TypedQuery<OpMultiplicityHour> query = em.createQuery(
                "select a from OpMultiplicityHour a where a.dayAndHour between :start and :end",
                OpMultiplicityHour.class);

        query.setParameter("start", start);
        query.setParameter("end", end);*/

        Query query = em.createNativeQuery("select * from OP_MULTIPLICITY_HOUR a where a.day_and_hour between to_timestamp_tz(:start, 'YYYY-MM-DD HH24 TZD') and to_timestamp_tz(:end, 'YYYY-MM-DD HH24 TZD')", OpMultiplicityHour.class);

        String startStr = TimeUtil.formatDatabaseDateTimeTZ(start);
        String endStr = TimeUtil.formatDatabaseDateTimeTZ(end);

        query.setParameter("start", startStr);
        query.setParameter("end", endStr);

        return query.getResultList();
    }

    @PermitAll
    public List<OpMultiplicityHour> findInEpics(Date start, Date end, List<List<OpHallHour>> hallHoursList) throws UserFriendlyException {
        try {
            return epicsService.find(start, end, hallHoursList);
        } catch (TimeoutException | InterruptedException | CAException e) {
            throw new UserFriendlyException("Unable to query EPICS", e);
        }
    }

    @PermitAll
    public OpMultiplicityShiftTotals calculateTotals(List<OpMultiplicityHour> hourList) {
        OpMultiplicityShiftTotals totals = new OpMultiplicityShiftTotals();

        if (hourList != null) {
            for (OpMultiplicityHour hour : hourList) {
                totals.setOneHallUpSeconds(
                        totals.getOneHallUpSeconds() == null ? hour.getOneHallUpSeconds() : totals.getOneHallUpSeconds()
                                + hour.getOneHallUpSeconds());
                totals.setTwoHallUpSeconds(
                        totals.getTwoHallUpSeconds() == null ? hour.getTwoHallUpSeconds() : totals.getTwoHallUpSeconds()
                                + hour.getTwoHallUpSeconds());
                totals.setThreeHallUpSeconds(
                        totals.getThreeHallUpSeconds() == null ? hour.getThreeHallUpSeconds() : totals.getThreeHallUpSeconds()
                                + hour.getThreeHallUpSeconds());
                totals.setFourHallUpSeconds(
                        totals.getFourHallUpSeconds() == null ? hour.getFourHallUpSeconds() : totals.getFourHallUpSeconds()
                                + hour.getFourHallUpSeconds());
                totals.setAnyHallUpSeconds(
                        totals.getAnyHallUpSeconds() == null ? hour.getAnyHallUpSeconds() : totals.getAnyHallUpSeconds()
                                + hour.getAnyHallUpSeconds());
                totals.setAllHallUpSeconds(
                        totals.getAllHallUpSeconds() == null ? hour.getAllHallUpSeconds() : totals.getAllHallUpSeconds()
                                + hour.getAllHallUpSeconds());
                totals.setDownHardSeconds(
                        totals.getDownHardSeconds() == null ? hour.getDownHardSeconds() : totals.getDownHardSeconds()
                                + hour.getDownHardSeconds());
            }
        }

        return totals;
    }

    @RolesAllowed({"cc", "btm-admin"})
    public void editMultiHours(Date[] hourArray, Short[] fourUpArray, Short[] threeUpArray, Short[] twoUpArray,
                               Short[] oneUpArray, Short[] anyUpArray, Short[] allUpArray, Short[] downHardArray) throws
            UserFriendlyException {
        if (hourArray == null || fourUpArray == null || threeUpArray == null || twoUpArray == null || oneUpArray == null
                || anyUpArray == null || allUpArray == null || downHardArray == null) {
            throw new UserFriendlyException("Some columns of data are missing");
        }

        if (hourArray.length == 0) {
            throw new UserFriendlyException("No data");
        }

        if (hourArray.length > 9) {
            throw new UserFriendlyException("Only a single shift of data can be edited at a time");
        }

        if (hourArray.length != fourUpArray.length || hourArray.length != threeUpArray.length || hourArray.length != twoUpArray.length
                || hourArray.length != oneUpArray.length || hourArray.length != anyUpArray.length
                || hourArray.length != allUpArray.length || hourArray.length
                != downHardArray.length) {
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

        List<OpMultiplicityHour> hourList = findInDatabase(hourArray[0], hourArray[hourArray.length
                - 1]);
        Map<Date, OpMultiplicityHour> hourMap = HourUtil.createHourMap(hourList);

        // We probably could consolidate with previous loop... (performance vs maintainability)
        for (int i = 0; i < hourArray.length; i++) {
            Date hour = hourArray[i];
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy HH:mm z");
            logger.log(Level.FINEST, "Editing hour: {0}", dateFormat.format(hour));
            OpMultiplicityHour hourEntity = hourMap.get(hour);

            if (hourEntity == null) {
                hourEntity = new OpMultiplicityHour();
                hourEntity.setDayAndHour(hour);
            }

            hourEntity.setFourHallUpSeconds(fourUpArray[i]);
            hourEntity.setThreeHallUpSeconds(threeUpArray[i]);
            hourEntity.setTwoHallUpSeconds(twoUpArray[i]);
            hourEntity.setOneHallUpSeconds(oneUpArray[i]);
            hourEntity.setAnyHallUpSeconds(anyUpArray[i]);
            hourEntity.setAllHallUpSeconds(allUpArray[i]);
            hourEntity.setDownHardSeconds(downHardArray[i]);

            edit(hourEntity);
        }
    }

    @Override
    protected void edit(OpMultiplicityHour hour) {
        if (hour.getOpMultiplicityHourId() == null) {
            this.manualInsert(hour);
        } else {
            super.edit(hour);
        }
    }

    private OpMultiplicityHour manualInsert(OpMultiplicityHour hour) {

        String dayAndHourStr = TimeUtil.formatDatabaseDateTimeTZ(hour.getDayAndHour());

        Query idq = em.createNativeQuery("select op_multiplicity_hour_id.nextval from dual");

        BigDecimal idDec = (BigDecimal) idq.getSingleResult();

        BigInteger id = idDec.toBigInteger();

        Query q = em.createNamedQuery("OpMultiplicityHour.insertNATIVE");

        q.setParameter("id", id);
        q.setParameter("dayAndHour", dayAndHourStr);
        q.setParameter("fourUp", hour.getFourHallUpSeconds());
        q.setParameter("threeUp", hour.getThreeHallUpSeconds());
        q.setParameter("twoUp", hour.getTwoHallUpSeconds());
        q.setParameter("oneUp", hour.getOneHallUpSeconds());
        q.setParameter("anyUp", hour.getAnyHallUpSeconds());
        q.setParameter("allUp", hour.getAllHallUpSeconds());
        q.setParameter("downHard", hour.getDownHardSeconds());

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
    public MultiplicityShiftAvailability getMultiShiftAvailability(Date startHour, Date endHour,
                                                                   boolean queryEpics, List<List<OpHallHour>> hallHoursList) {
        List<OpMultiplicityHour> dbHourList = findInDatabase(startHour,
                endHour);
        List<OpMultiplicityHour> epicsHourList;

        if (queryEpics) {
            try {
                epicsHourList = findInEpics(startHour, endHour, hallHoursList);
            } catch (UserFriendlyException e) {
                logger.log(Level.FINEST, "Unable to obtain EPICS agg hour data", e);
                epicsHourList = new ArrayList<>();
            }
        } else {
            epicsHourList = new ArrayList<>();
        }

        Map<Date, OpMultiplicityHour> dbMultiHourMap = HourUtil.createHourMap(dbHourList);
        Map<Date, OpMultiplicityHour> epicsMultiHourMap = HourUtil.createHourMap(epicsHourList);
        List<OpMultiplicityHour> multiHourList = HourUtil.fillMissingHoursAndSetSource(
                dbMultiHourMap,
                epicsMultiHourMap, startHour, endHour, OpMultiplicityHour.class);

        OpMultiplicityShiftTotals shiftTotals = calculateTotals(
                multiHourList);
        OpMultiplicityShiftTotals epicsShiftTotals = calculateTotals(
                epicsHourList);

        MultiplicityShiftAvailability availability = new MultiplicityShiftAvailability();

        availability.setHourList(multiHourList);
        availability.setEpicsHourList(epicsHourList);
        availability.setShiftTotals(shiftTotals);
        availability.setEpicsShiftTotals(epicsShiftTotals);
        availability.setDbHourList(dbHourList);

        return availability;
    }

    @PermitAll
    public MultiplicitySummaryTotals reportTotals(Date start, Date end) {
        Query q = em.createNativeQuery(
                "select sum(four_hall_up_seconds), sum(three_hall_up_seconds), sum(two_hall_up_seconds), sum(one_hall_up_seconds), sum(any_hall_up_seconds), sum(all_hall_up_seconds), sum(down_hard_seconds) "
                        + "from ("
                        + "select four_hall_up_seconds, three_hall_up_seconds, two_hall_up_seconds, one_hall_up_seconds, any_hall_up_seconds, all_hall_up_seconds, down_hard_seconds from op_multiplicity_hour "
                        + "where day_and_hour >= :start and day_and_hour < :end "
                        + "union all select 0, 0, 0, 0, 0, 0, 0 from dual)");

        q.setParameter("start", start);
        q.setParameter("end", end);

        List<MultiplicitySummaryTotals> totalsList = JPAUtil.getResultList(q,
                MultiplicitySummaryTotals.class);

        MultiplicitySummaryTotals totals = null;

        if (totalsList != null && !totalsList.isEmpty()) {
            totals = totalsList.get(0);
        }

        return totals;
    }
}
