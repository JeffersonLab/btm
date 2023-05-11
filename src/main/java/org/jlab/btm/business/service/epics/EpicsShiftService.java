package org.jlab.btm.business.service.epics;

import com.cosylab.epics.caj.CAJContext;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import org.jlab.btm.business.service.OpAccHourService;
import org.jlab.btm.persistence.entity.OpShift;
import org.jlab.btm.persistence.epics.ShiftInfo;
import org.jlab.btm.persistence.epics.ShiftInfoDao;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.enumeration.Shift;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Responsible for querying experimenter hall EPICS time accounting data.
 *
 * @author ryans
 */
@Stateless
public class EpicsShiftService {

    private static final Logger logger = Logger.getLogger(EpicsShiftService.class.getName());
    @EJB
    ContextFactory factory;
    @EJB
    OpAccHourService accHourService;

    private boolean isCurrentLastOrNextShiftStart(Date startDayAndHour) {
        Date now = new Date();
        Date day = TimeUtil.getCurrentCrewChiefShiftDay(now); // If hour is 23 then actually return tomorrow...
        Shift currentShift = TimeUtil.calculateCrewChiefShift(now);
        Date currentStartDayAndHour = TimeUtil.getCrewChiefStartDayAndHour(day, currentShift);
        Date lastStartDayAndHour = TimeUtil.previousCrewChiefShiftStart(currentStartDayAndHour);
        Date nextStartDayAndHour = TimeUtil.nextCrewChiefShiftStart(currentStartDayAndHour);
        boolean current = currentStartDayAndHour.equals(startDayAndHour);
        boolean last = lastStartDayAndHour.equals(startDayAndHour);
        boolean next = nextStartDayAndHour.equals(startDayAndHour);
        
        /*SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyy HH:mm");
        System.out.println("startDayAndHour: " + formatter.format(startDayAndHour));
        System.out.println("computed current: " + formatter.format(currentStartDayAndHour));
        System.out.println("computed last: " + formatter.format(lastStartDayAndHour));
        System.out.println("computed next: " + formatter.format(nextStartDayAndHour));
        
        Date testDate = TimeUtil.getCrewChiefStartDayAndHour(day, Shift.OWL);
        System.out.println("Previous from " + formatter.format(testDate) + ": " + TimeUtil.previousCrewChiefShiftStart(testDate));
        System.out.println("Next from " + formatter.format(testDate) + ": " + TimeUtil.nextCrewChiefShiftStart(testDate));
        
        testDate = TimeUtil.getCrewChiefStartDayAndHour(day, Shift.DAY);
        System.out.println("Previous from " + formatter.format(testDate) + ": " + TimeUtil.previousCrewChiefShiftStart(testDate));
        System.out.println("Next from " + formatter.format(testDate) + ": " + TimeUtil.nextCrewChiefShiftStart(testDate));   
        
        testDate = TimeUtil.getCrewChiefStartDayAndHour(day, Shift.SWING);
        System.out.println("Previous from " + formatter.format(testDate) + ": " + TimeUtil.previousCrewChiefShiftStart(testDate));
        System.out.println("Next from " + formatter.format(testDate) + ": " + TimeUtil.nextCrewChiefShiftStart(testDate)); */

        return current || last || next;
    }

    /**
     * Fetches EPICS accounting information for a particular experimenter hall, optionally rounded,
     * and restricted to only a subset of the value return from an IOC query.
     * <p>
     * The accounting information returned from an EPICS IOC query is generally the past week of
     * data, up to, and including the current hour.
     * <p>
     * An empty list is returned if no data falls within the range. If EPICS does not contain data
     * for the entire range only the available data in the requested range is returned.</p>
     *
     * @param startDayAndHour the start day and hour.
     * @return a list of experimenter hall hours.
     * @throws TimeoutException     if a network request takes too long.
     * @throws InterruptedException if a thread gets unexpectedly interrupted.
     * @throws CAException          if a channel access problem occurs.
     */
    public OpShift find(Date startDayAndHour) throws TimeoutException,
            InterruptedException, CAException {
        OpShift shift = null;

        if (isCurrentLastOrNextShiftStart(startDayAndHour)) {
            shift = loadAccounting();
        }

        return shift;
    }

    /**
     * Fetches EPICS accounting information.
     * <p>
     * The accounting information returned from an EPICS IOC query is generally the past week of
     * data, up to, and including the current hour.
     *
     * @return the accounting information as a list of experimenter hall hours.
     * @throws TimeoutException     if a network request takes too long.
     * @throws InterruptedException if a thread gets unexpectedly interrupted.
     * @throws CAException          if a channel access problem occurs.
     */
    private OpShift loadAccounting() throws TimeoutException,
            InterruptedException, CAException {

        ShiftInfo accounting;

        CAJContext context = factory.getContext();

        try {
            ShiftInfoDao dao = new ShiftInfoDao(context);

            long start = System.currentTimeMillis();
            accounting = dao.loadAccounting();
            long end = System.currentTimeMillis();
            logger.log(Level.FINEST, "EPICS shift load time (milliseconds): {0}", (end - start));
        } finally {
            factory.returnContext(context);
        }

        return accounting.getOpShift();
    }
}
