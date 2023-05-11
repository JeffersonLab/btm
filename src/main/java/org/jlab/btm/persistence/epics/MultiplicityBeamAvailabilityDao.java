package org.jlab.btm.persistence.epics;

import gov.aps.jca.CAException;
import gov.aps.jca.Context;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) responsible for reading Accelerator Beam Availability from an
 * EPICS IOC.
 *
 * @author ryans
 */
public class MultiplicityBeamAvailabilityDao {

    private Context context = null;

    /**
     * Constructs an Accelerator Beam Availability data access object.
     *
     * @param context the EPICS context.
     */
    public MultiplicityBeamAvailabilityDao(Context context) {
        this.context = context;
    }

    /**
     * Load Accelerator Beam Availability data from EPICS.
     *
     * @return the accounting data.
     * @throws InterruptedException if the waiting thread is interrupted before
     *                              it is notified.
     * @throws TimeoutException     if the waiting has exceeded the timeout period.
     * @throws CAException          if a problem occurs transferring the EPICS data.
     */
    public MultiplicityBeamAvailability loadAccounting() throws InterruptedException,
            TimeoutException, CAException {
        List<String> channelNames = new ArrayList<>();

        channelNames.add(Constant.TIME_CHANNEL_NAME);
        channelNames.add(Constant.MULTI_ONE_UP);
        channelNames.add(Constant.MULTI_TWO_UP);
        channelNames.add(Constant.MULTI_THREE_UP);
        channelNames.add(Constant.MULTI_FOUR_UP);
        channelNames.add(Constant.MULTI_ANY_UP);
        channelNames.add(Constant.MULTI_ALL_UP);
        channelNames.add(Constant.MULTI_DOWN);

        List<DBR> dbrs = SimpleGet.doAsyncGet(context, channelNames);

        MultiplicityBeamAvailability accounting = new MultiplicityBeamAvailability();

        accounting.setTime(SimpleGet.getDoubleValue(dbrs.remove(0)));
        accounting.setOneUp(SimpleGet.getDoubleValue(dbrs.remove(0)));
        accounting.setTwoUp(SimpleGet.getDoubleValue(dbrs.remove(0)));
        accounting.setThreeUp(SimpleGet.getDoubleValue(dbrs.remove(0)));
        accounting.setFourUp(SimpleGet.getDoubleValue(dbrs.remove(0)));
        accounting.setAnyUp(SimpleGet.getDoubleValue(dbrs.remove(0)));
        accounting.setAllUp(SimpleGet.getDoubleValue(dbrs.remove(0)));
        accounting.setDownHard(SimpleGet.getDoubleValue(dbrs.remove(0)));

        return accounting;
    }
}
