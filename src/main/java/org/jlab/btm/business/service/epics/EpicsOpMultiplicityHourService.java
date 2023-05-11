package org.jlab.btm.business.service.epics;

import com.cosylab.epics.caj.CAJContext;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import org.jlab.btm.business.util.HourUtil;
import org.jlab.btm.persistence.entity.OpHallHour;
import org.jlab.btm.persistence.entity.OpMultiplicityHour;
import org.jlab.btm.persistence.epics.MultiplicityBeamAvailability;
import org.jlab.btm.persistence.epics.MultiplicityBeamAvailabilityDao;

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
public class EpicsOpMultiplicityHourService {

    private static final Logger logger = Logger.getLogger(EpicsOpMultiplicityHourService.class.getName());
    @EJB
    ContextFactory factory;

    public List<OpMultiplicityHour> find(Date startDayAndHour,
                                         Date endDayAndHour, List<List<OpHallHour>> hallHoursList) throws TimeoutException,
            InterruptedException, CAException {
        List<OpMultiplicityHour> hours;

        if (HourUtil.isInEpicsWindow(endDayAndHour)) {
            hours = loadAccounting();

            hours = HourUtil.subset(startDayAndHour, endDayAndHour, hours);

            HourRounder rounder = new HourRounder();
            rounder.roundMultiplicityHourList(hours, hallHoursList);
        } else {
            hours = new ArrayList<>();
        }

        return hours;
    }

    private List<OpMultiplicityHour> loadAccounting() throws TimeoutException,
            InterruptedException, CAException {

        MultiplicityBeamAvailability accounting;

        CAJContext context = factory.getContext();

        try {
            MultiplicityBeamAvailabilityDao dao = new MultiplicityBeamAvailabilityDao(context);

            long start = System.currentTimeMillis();
            accounting = dao.loadAccounting();
            long end = System.currentTimeMillis();
            logger.log(Level.FINEST, "EPICS multiplicity hall hours load time (milliseconds): {0}", (end - start));
        } finally {
            factory.returnContext(context);
        }

        return accounting.getOpMultiplicityHours();
    }
}
