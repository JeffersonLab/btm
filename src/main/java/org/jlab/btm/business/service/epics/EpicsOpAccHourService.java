package org.jlab.btm.business.service.epics;

import com.cosylab.epics.caj.CAJContext;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import org.jlab.btm.business.util.HourUtil;
import org.jlab.btm.persistence.entity.OpAccHour;
import org.jlab.btm.persistence.epics.AcceleratorBeamAvailability;
import org.jlab.btm.persistence.epics.AcceleratorBeamAvailabilityDao;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Responsible for querying experimenter hall EPICS time accounting data.
 *
 * @author ryans
 */
@Stateless
public class EpicsOpAccHourService {

    private static final Logger logger = Logger.getLogger(EpicsOpAccHourService.class.getName());
    @EJB
    ContextFactory factory;

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
     * @param endDayAndHour   the end day and hour.
     * @return a list of experimenter hall hours.
     * @throws TimeoutException     if a network request takes too long.
     * @throws InterruptedException if a thread gets unexpectedly interrupted.
     * @throws CAException          if a channel access problem occurs.
     */
    public List<OpAccHour> find(Date startDayAndHour,
                                Date endDayAndHour) throws TimeoutException,
            InterruptedException, CAException {
        List<OpAccHour> hours;

        if (HourUtil.isInEpicsWindow(endDayAndHour)) {
            hours = loadAccounting();

            hours = HourUtil.subset(startDayAndHour, endDayAndHour, hours);

            HourRounder rounder = new HourRounder();
            rounder.roundAcceleratorHourList(hours);
        } else {
            hours = new ArrayList<>();
        }

        return hours;
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
    private List<OpAccHour> loadAccounting() throws TimeoutException,
            InterruptedException, CAException {

        AcceleratorBeamAvailability accounting;

        CAJContext context = factory.getContext();

        try {
            AcceleratorBeamAvailabilityDao dao = new AcceleratorBeamAvailabilityDao(context);

            long start = System.currentTimeMillis();
            accounting = dao.loadAccounting();
            long end = System.currentTimeMillis();
            logger.log(Level.FINEST, "EPICS acc hours load time (milliseconds): {0}", (end - start));
        } finally {
            factory.returnContext(context);
        }

        return accounting.getOpAccHours();
    }
}
