package org.jlab.btm.persistence.epics;

import gov.aps.jca.CAException;
import gov.aps.jca.Context;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import org.jlab.smoothness.persistence.enumeration.Hall;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) responsible for reading Accelerator Beam Availability from an
 * EPICS IOC.
 *
 * @author ryans
 */
public class HallBeamAvailabilityDao {

    private Context context = null;

    /**
     * Constructs an Accelerator Beam Availability data access object.
     *
     * @param context the EPICS context.
     */
    public HallBeamAvailabilityDao(Context context) {
        this.context = context;
    }

    /**
     * Load Accelerator Beam Availability data from EPICS.
     *
     * @param hall
     * @return the accounting data.
     * @throws InterruptedException if the waiting thread is interrupted before
     *                              it is notified.
     * @throws TimeoutException     if the waiting has exceeded the timeout period.
     * @throws CAException          if a problem occurs transferring the EPICS data.
     */
    public HallBeamAvailability loadAccounting(Hall hall) throws InterruptedException,
            TimeoutException, CAException {
        List<String> channelNames = new ArrayList<>();

        channelNames.add(Constant.TIME_CHANNEL_NAME);
        channelNames.add(Constant.HALL_PREFIX + hall + Constant.HALL_UP_SUFFIX);
        channelNames.add(Constant.HALL_PREFIX + hall + Constant.HALL_TUNE_SUFFIX);
        channelNames.add(Constant.HALL_PREFIX + hall + Constant.HALL_BNR_SUFFIX);
        channelNames.add(Constant.HALL_PREFIX + hall + Constant.HALL_DOWN_SUFFIX);
        channelNames.add(Constant.HALL_PREFIX + hall + Constant.HALL_OFF_SUFFIX);

        List<DBR> dbrs = SimpleGet.doAsyncGet(context, channelNames);

        HallBeamAvailability accounting = new HallBeamAvailability();

        accounting.setTime(SimpleGet.getDoubleValue(dbrs.remove(0)));
        accounting.setUp(SimpleGet.getDoubleValue(dbrs.remove(0)));
        accounting.setTune(SimpleGet.getDoubleValue(dbrs.remove(0)));
        accounting.setBnr(SimpleGet.getDoubleValue(dbrs.remove(0)));
        accounting.setDown(SimpleGet.getDoubleValue(dbrs.remove(0)));
        accounting.setOff(SimpleGet.getDoubleValue(dbrs.remove(0)));

        return accounting;
    }
}
