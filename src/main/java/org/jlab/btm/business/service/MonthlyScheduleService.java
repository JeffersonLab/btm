package org.jlab.btm.business.service;

import org.jlab.btm.business.util.BtmTimeUtil;
import org.jlab.btm.business.util.DateRange;
import org.jlab.btm.persistence.entity.MonthlySchedule;
import org.jlab.btm.persistence.entity.ScheduleDay;
import org.jlab.btm.persistence.projection.PacAccSum;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.DateIterator;
import org.jlab.smoothness.business.util.TimeUtil;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.math.BigInteger;
import java.time.Duration;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author ryans
 */
@Stateless
public class MonthlyScheduleService extends AbstractService<MonthlySchedule> {

    private final static Logger LOGGER
            = Logger.getLogger(MonthlyScheduleService.class.getName());

    @EJB
    ScheduleDayService scheduleDayService;
    @PersistenceContext(unitName = "btmPU")
    private EntityManager em;

    public MonthlyScheduleService() {
        super(MonthlySchedule.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public MonthlySchedule buildTenative(Date firstDayOfMonth) {
        return null;
    }

    @PermitAll
    public List<MonthlySchedule> findAll(Date firstDayOfMonth) {
        TypedQuery<MonthlySchedule> q = em.createQuery(
                "select m from MonthlySchedule m where m.startDay = :start order by m.version desc",
                MonthlySchedule.class);

        q.setParameter("start", firstDayOfMonth);

        return q.getResultList();
    }

    /**
     * Find most recent published schedule for the month given the first day of the month.  If none published,
     * then return most recent tentative schedule for the month.
     *
     * @param firstDayOfMonth The first day of the month
     * @return The monthly schedule or null if none
     */
    @PermitAll
    public MonthlySchedule findMostRecentPublished(Date firstDayOfMonth) {
        List<MonthlySchedule> scheduleList = findAll(firstDayOfMonth);

        return findMostRecentPublished(scheduleList);
    }

    /**
     * Find most recent published schedule for the month given the first day of the month.  If none published,
     * then return most recent tentative schedule for the month.
     *
     * @param scheduleList The list of monthly schedules
     * @return The monthly schedule or null if none
     */
    @PermitAll
    public MonthlySchedule findMostRecentPublished(List<MonthlySchedule> scheduleList) {
        MonthlySchedule schedule = null;

        // Grab latest published schedule
        if (scheduleList != null && !scheduleList.isEmpty()) {
            for (MonthlySchedule s : scheduleList) {
                if (s.getPublishedDate() != null && (schedule == null
                        || schedule.getPublishedDate() == null
                        || schedule.getPublishedDate().before(s.getPublishedDate()))) {
                    schedule = s;
                }
            }

            // If none published, try to grab latest tentative by version #
            if (schedule == null) {
                for (MonthlySchedule s : scheduleList) {
                    if (schedule == null
                            || schedule.getVersion() < s.getVersion()) {
                        schedule = s;
                    }
                }
            }
        }

        return schedule;
    }

    /**
     * Find the most recently published set of monthly schedules that are included with the specified date range.  For
     * any given month if no schedules are published then them most recent tentative schedule is used, or if none at
     * all then no record is included for that month.
     *
     * @param start The start date
     * @param end The end date
     * @return A list of published/recent monthly schedules that span the date range
     */
    @PermitAll
    public List<MonthlySchedule> findMostRecentPublishedInDateRange(Date start, Date end) {
        List<MonthlySchedule> monthlySchedules = new ArrayList<>();

        Date firstMonthStart = TimeUtil.startOfMonth(start, Calendar.getInstance());
        Date lastMonthStart = TimeUtil.startOfMonth(end, Calendar.getInstance());
        DateIterator iterator = new DateIterator(firstMonthStart, lastMonthStart, Calendar.MONTH);

        while(iterator.hasNext()) {
            Date next = iterator.next();
            //LOGGER.log(Level.WARNING, "Date: " + next);
            MonthlySchedule schedule = this.findMostRecentPublished(next);
            if(schedule != null) {
                monthlySchedules.add(schedule);
            }
        }

        return monthlySchedules;
    }

    @RolesAllowed({"schcom"})
    public void editRow(Date date, BigInteger scheduleId, String accProgram,
                        Integer kiloVoltsPerPass, Integer minHallCount, int hallAProgramId,
                        int hallBProgramId, int hallCProgramId, int hallDProgramId, Integer hallAKiloVolts,
                        Integer hallBKiloVolts, Integer hallCKiloVolts, Integer hallDKiloVolts,
                        Integer hallANanoAmps,
                        Integer hallBNanoAmps, Integer hallCNanoAmps, Integer hallDNanoAmps,
                        boolean hallAPolarized,
                        boolean hallBPolarized, boolean hallCPolarized, boolean hallDPolarized,
                        Integer hallAPasses,
                        Integer hallBPasses, Integer hallCPasses, Integer hallDPasses, Integer hallAPriority,
                        Integer hallBPriority,
                        Integer hallCPriority, Integer hallDPriority, String hallANotes, String hallBNotes,
                        String hallCNotes, String hallDNotes, String notes, int count) throws UserFriendlyException {

        if (count < 1 || count > 31) {
            throw new UserFriendlyException("Paste count must be between 1 and 31");
        }

        MonthlySchedule schedule = find(scheduleId);

        if (schedule == null) {
            throw new UserFriendlyException("Unable to find monthly schedule for supplied ID: "
                    + scheduleId);
        }

        if (schedule.getPublishedDate() != null) {
            throw new UserFriendlyException(
                    "Unable to modify schedule because it has already been published");
        }

        Date endDate = TimeUtil.addDays(date, count - 1);

        if (!TimeUtil.isSameMonth(date, endDate)) {
            throw new UserFriendlyException("Cannot paste onto days that span into the next month");
        }

        for (int i = 0; i < count; i++) {
            editRow(date, schedule, accProgram, kiloVoltsPerPass, minHallCount, hallAProgramId,
                    hallBProgramId,
                    hallCProgramId, hallDProgramId, hallAKiloVolts, hallBKiloVolts, hallCKiloVolts,
                    hallDKiloVolts, hallANanoAmps, hallBNanoAmps, hallCNanoAmps, hallDNanoAmps,
                    hallAPolarized, hallBPolarized, hallCPolarized, hallDPolarized, hallAPasses,
                    hallBPasses, hallCPasses, hallDPasses, hallAPriority, hallBPriority,
                    hallCPriority, hallDPriority, hallANotes, hallBNotes, hallCNotes, hallDNotes,
                    notes);
            date = TimeUtil.addDays(date, 1);
        }
    }

    private void editRow(Date date, MonthlySchedule schedule, String accProgram,
                         Integer kiloVoltsPerPass, Integer minHallCount, int hallAProgramId,
                         int hallBProgramId, int hallCProgramId, int hallDProgramId, Integer hallAKiloVolts,
                         Integer hallBKiloVolts, Integer hallCKiloVolts, Integer hallDKiloVolts,
                         Integer hallANanoAmps,
                         Integer hallBNanoAmps, Integer hallCNanoAmps, Integer hallDNanoAmps,
                         boolean hallAPolarized,
                         boolean hallBPolarized, boolean hallCPolarized, boolean hallDPolarized,
                         Integer hallAPasses,
                         Integer hallBPasses, Integer hallCPasses, Integer hallDPasses, Integer hallAPriority,
                         Integer hallBPriority,
                         Integer hallCPriority, Integer hallDPriority, String hallANotes, String hallBNotes,
                         String hallCNotes, String hallDNotes, String notes) {

        ScheduleDay scheduleDay = scheduleDayService.findByDateAndSchedule(date,
                schedule.getMonthlyScheduleId());

        if (scheduleDay == null) {
            scheduleDay = new ScheduleDay();
            scheduleDay.setDayMonthYear(date);
            scheduleDay.setMonthlySchedule(schedule);
        }

        scheduleDay.setAccProgram(accProgram);
        scheduleDay.setKiloVoltsPerPass(kiloVoltsPerPass);
        scheduleDay.setMinHallCount(minHallCount);
        scheduleDay.setNote(notes);

        /* Hall A */
        scheduleDay.setHallAProgramId(hallAProgramId);
        scheduleDay.setHallAKiloVolts(hallAKiloVolts);
        scheduleDay.setHallANanoAmps(hallANanoAmps);
        scheduleDay.setHallAPolarized(hallAPolarized);
        scheduleDay.setHallAPasses(hallAPasses);
        scheduleDay.setHallAPriority(hallAPriority);
        scheduleDay.setHallANote(hallANotes);

        /* Hall B */
        scheduleDay.setHallBProgramId(hallBProgramId);
        scheduleDay.setHallBKiloVolts(hallBKiloVolts);
        scheduleDay.setHallBNanoAmps(hallBNanoAmps);
        scheduleDay.setHallBPolarized(hallBPolarized);
        scheduleDay.setHallBPasses(hallBPasses);
        scheduleDay.setHallBPriority(hallBPriority);
        scheduleDay.setHallBNote(hallBNotes);

        /* Hall C */
        scheduleDay.setHallCProgramId(hallCProgramId);
        scheduleDay.setHallCKiloVolts(hallCKiloVolts);
        scheduleDay.setHallCNanoAmps(hallCNanoAmps);
        scheduleDay.setHallCPolarized(hallCPolarized);
        scheduleDay.setHallCPasses(hallCPasses);
        scheduleDay.setHallCPriority(hallCPriority);
        scheduleDay.setHallCNote(hallCNotes);

        /* Hall D */
        scheduleDay.setHallDProgramId(hallDProgramId);
        scheduleDay.setHallDKiloVolts(hallDKiloVolts);
        scheduleDay.setHallDNanoAmps(hallDNanoAmps);
        scheduleDay.setHallDPolarized(hallDPolarized);
        scheduleDay.setHallDPasses(hallDPasses);
        scheduleDay.setHallDPriority(hallDPriority);
        scheduleDay.setHallDNote(hallDNotes);

        em.merge(scheduleDay);
    }

    @RolesAllowed({"schcom"})
    public ScheduleDay setDay(MonthlySchedule schedule, ScheduleDay day) {
        day.setMonthlySchedule(schedule);

        if (schedule.getMonthlyScheduleId() == null) {
            return null;
        } else {
            return em.merge(day);
        }
    }

    @RolesAllowed({"schcom"})
    public MonthlySchedule publish(BigInteger scheduleId) throws UserFriendlyException {
        if (scheduleId == null) {
            throw new UserFriendlyException("Schedule ID must not be empty");
        }

        MonthlySchedule schedule = find(scheduleId);

        if (schedule == null) {
            throw new UserFriendlyException("Could not find a schedule with provided ID: " + scheduleId);
        }

        if (schedule.getPublishedDate() != null) {
            throw new UserFriendlyException("Cannot publish a schedule that is already published");
        }

        schedule.setPublishedDate(new Date());

        return schedule;
    }

    @PermitAll
    public MonthlySchedule find(BigInteger scheduleId) {
        return super.find(scheduleId);
    }

    @RolesAllowed({"schcom"})
    public MonthlySchedule create(Date start) throws UserFriendlyException {
        Date firstOfMonth = TimeUtil.startOfMonth(start, Calendar.getInstance());

        if (!firstOfMonth.equals(start)) {
            throw new UserFriendlyException("Date is not the first of the month");
        }

        List<MonthlySchedule> scheduleList = findAll(start);

        int latestVersion = 0;

        if (scheduleList != null && !scheduleList.isEmpty()) {
            latestVersion = scheduleList.get(0).getVersion();
        }

        MonthlySchedule schedule = new MonthlySchedule();

        schedule.setStartDay(start);
        schedule.setVersion(latestVersion + 1);

        create(schedule);

        return schedule;
    }

    @RolesAllowed({"schcom"})
    public MonthlySchedule newVersion(BigInteger scheduleId) throws UserFriendlyException {
        if (scheduleId == null) {
            throw new UserFriendlyException("Schedule ID must not be emtpy");
        }

        MonthlySchedule templateSchedule = this.find(scheduleId);

        if (templateSchedule == null) {
            throw new UserFriendlyException("Schedule with supplied ID not found: " + scheduleId);
        }

        List<MonthlySchedule> scheduleList = findAll(templateSchedule.getStartDay());

        int latestVersion = 0;

        if (scheduleList != null && !scheduleList.isEmpty()) {
            latestVersion = scheduleList.get(0).getVersion();
        }

        MonthlySchedule schedule = new MonthlySchedule();

        schedule.setStartDay(templateSchedule.getStartDay());
        schedule.setVersion(latestVersion + 1);

        create(schedule);

        cloneScheduleDays(templateSchedule, schedule);

        return schedule;
    }

    private void cloneScheduleDays(MonthlySchedule templateSchedule, MonthlySchedule schedule) {
        List<ScheduleDay> dayList = templateSchedule.getScheduleDayList();

        List<ScheduleDay> cloneList = new ArrayList<>();

        for (ScheduleDay day : dayList) {
            ScheduleDay clone = day.scheduleCopy();
            clone.setMonthlySchedule(schedule);
            cloneList.add(clone);
        }

        schedule.setScheduleDayList(cloneList);
    }

    @PermitAll
    public List<ScheduleDay> filterScheduleDaysFromRange(List<MonthlySchedule> monthlySchedules, Date start, Date end) {
        List<ScheduleDay> days = new ArrayList<>();

        // Schedule resolution is only per day so hours, minutes, seconds need to be trimmed off.
        start =  TimeUtil.startOfDay(start, Calendar.getInstance());
        end = TimeUtil.startOfDay(end, Calendar.getInstance());

        //LOGGER.log(Level.WARNING, "Start: " + start);
        for(MonthlySchedule schedule: monthlySchedules) {
            schedule = em.merge(schedule);
            List<ScheduleDay> dayList = schedule.getScheduleDayList();

            Collections.sort(dayList);

            for(ScheduleDay day: dayList) {
                if(!day.getDayMonthYear().before(start) && !day.getDayMonthYear().after(end)) {
                    days.add(day);
                } else {
                    //LOGGER.log(Level.WARNING, "Not Included: " + day.getDayMonthYear());
                }
            }
        }

        return days;
    }

    private Date findPreviousOff(Date today, MonthlySchedule currentMonth, boolean inverse) {
        Date previousOff = null;

        SortedSet<ScheduleDay> daysBefore = currentMonth.getDaysBefore(today);

        //LOGGER.log(Level.WARNING, "Looking at days before in month: " + currentMonth.getStartDay());

        for(ScheduleDay sd: daysBefore) {
            //LOGGER.log(Level.WARNING, "Day: " + sd);
            if(inverse != "OFF".equals(sd.getAccProgram())) {
                //LOGGER.log(Level.WARNING, "Found boundary! " + sd);
                previousOff = sd.getDayMonthYear();
                break;
            }
        }

        // Run didn't start this month so look at previous month
        if(previousOff == null) {
            Date startOfMonth = currentMonth.getStartDay();
            Date startOfPreviousMonth = TimeUtil.addMonths(startOfMonth, -1);

            // Once we've recursively searched for a year's worth of days time to call it quits
            if(Duration.between(today.toInstant(), startOfPreviousMonth.toInstant()).compareTo(Duration.ofDays(365)) < 0) {
                MonthlySchedule schedule = findMostRecentPublished(startOfPreviousMonth);
                if (schedule != null) { // If previous month has no schedule at all then just return null, let's bail as months with no schedule at all are simply not supported!
                    previousOff = findPreviousOff(today, schedule, inverse);
                }
            }
        }

        return previousOff;
    }

    private Date findNextOff(Date searchStartInclusive, MonthlySchedule currentMonth, int overflow) {
        Date nextOff = null;

        SortedSet<ScheduleDay> daysAfter = currentMonth.getDaysAfter(searchStartInclusive);

        for(ScheduleDay sd: daysAfter) {
            if("OFF".equals(sd.getAccProgram()) || "UNKNOWN".equals(sd.getAccProgram())) {
                nextOff = sd.getDayMonthYear();
                break;
            }
        }

        // Run didn't end this month so look at next month
        if(nextOff == null) {
            Date startOfMonth = currentMonth.getStartDay();
            Date startOfNextMonth = TimeUtil.addMonths(startOfMonth, 1);

            // Once we've recursively searched for a year's worth of days time to call it quits
            if(overflow++ < 12) {
                MonthlySchedule schedule = findMostRecentPublished(startOfNextMonth);

                if(schedule == null) { // No data for next month
                    if(daysAfter.size() > 0) { // Use last non-off day + 1
                        nextOff = TimeUtil.addDays(daysAfter.last().getDayMonthYear(), 1);
                    } else { // First of current month is start of OFF then
                        nextOff = startOfMonth;
                    }
                } else {
                    nextOff = findNextOff(startOfNextMonth, schedule, overflow);
                }
            }
        }

        return nextOff;
    }

    @PermitAll
    public DateRange getCurrentRunBounds() {
        DateRange bounds = null;
        Date today = new Date();
        MonthlySchedule schedule = findMostRecentPublished(TimeUtil.startOfMonth(today, Calendar.getInstance()));
        if(schedule != null) {
            ScheduleDay sd = schedule.getScheduleDay(today);

            // If most recent schedule says today we're not OFF (SAD) then a current run exists!
            if(sd != null && !"OFF".equals(sd.getAccProgram())) {
                // Great, current run exists; now we have to find bounds by checking one month at a time in both directions
                // starting with current month
                Date start = findPreviousOff(today, schedule, false);
                Date end = findNextOff(today, schedule, 0);
                if(start != null && end != null) {
                    bounds = new DateRange(start, end);
                }
            }
        }
        return bounds;
    }

    @PermitAll
    public DateRange getPreviousRunBounds(Date previousToDate) {
        DateRange bounds = null;
        MonthlySchedule schedule = findMostRecentPublished(TimeUtil.startOfMonth(previousToDate, Calendar.getInstance()));

        if(schedule != null) {
            ScheduleDay sd = schedule.getScheduleDay(previousToDate);

            //LOGGER.log(Level.WARNING, "There is a schedule this month!");

            //LOGGER.log(Level.WARNING, "Scheduled Day: " + sd);

            // If most recent schedule says previousToDate we're not OFF (SAD) then a current run exists so we can't continue
            if(sd != null && "OFF".equals(sd.getAccProgram())) {
                //LOGGER.log(Level.WARNING, "We are in SAD!");
                // Great, we are in SAD; now we have to find bounds of previous run
                Date end = findPreviousOff(previousToDate, schedule, true);
                if(end != null) { // Only continue if not null, else return null
                    schedule = findMostRecentPublished(TimeUtil.startOfMonth(end, Calendar.getInstance()));
                    Date start = findPreviousOff(end, schedule, false);

                    //LOGGER.log(Level.WARNING, "Start: " + start);
                    //LOGGER.log(Level.WARNING, "End: " + end);

                    if (start != null && end != null) {
                        // Because of inverse lookup we need to add 1 to end date to keep boundary days always SAD days
                        // But we need to do this here instead of above so we don't sabotage second findPreviousOff call
                        bounds = new DateRange(start, TimeUtil.addDays(end, 1));
                    }
                }
            }
        }
        return bounds;
    }

    @PermitAll
    public PacAccSum findSummary(Date start, Date end) {

        // PAC schedule lines up with whole days
        start = TimeUtil.startOfDay(start, Calendar.getInstance());
        end = BtmTimeUtil.isStartOfDay(end) ? end : TimeUtil.startOfNextDay(end, Calendar.getInstance());

        PacAccSum record = new PacAccSum();

        record.setRange(start, end);

        List<MonthlySchedule> monthlySchedules = this.findMostRecentPublishedInDateRange(start, end);
        List<ScheduleDay> scheduleDays = this.filterScheduleDaysFromRange(monthlySchedules, start, end);


        for (ScheduleDay day : scheduleDays) {
            if (!"OFF".equals(day.getAccProgram())) {
                record.programDays++;

                switch(day.getAccProgram()) {
                    case "PHYSICS":
                        record.physicsDays++;
                        break;
                    case "RESTORE":
                        record.restoreDays++;
                        break;
                    case "STUDIES":
                        record.studiesDays++;
                        break;
                    case "ACC":
                        record.accDays++;
                        break;
                }

            } else {
                record.offDays++;  // May be implied off gaps that are missed so this number is explicit off only...
            }
        }

        return record;
    }
}
