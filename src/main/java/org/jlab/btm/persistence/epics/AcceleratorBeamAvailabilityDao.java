package org.jlab.btm.persistence.epics;

import gov.aps.jca.CAException;
import gov.aps.jca.Context;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) responsible for reading Accelerator Beam Availability from an EPICS IOC.
 *
 * @author ryans
 */
public class AcceleratorBeamAvailabilityDao {

  private Context context = null;

  /**
   * Constructs an Accelerator Beam Availability data access object.
   *
   * @param context the EPICS context.
   */
  public AcceleratorBeamAvailabilityDao(Context context) {
    this.context = context;
  }

  /**
   * Load Accelerator Beam Availability data from EPICS.
   *
   * @return the accounting data.
   * @throws InterruptedException if the waiting thread is interrupted before it is notified.
   * @throws TimeoutException if the waiting has exceeded the timeout period.
   * @throws CAException if a problem occurs transferring the EPICS data.
   */
  public AcceleratorBeamAvailability loadAccounting()
      throws InterruptedException, TimeoutException, CAException {
    List<String> channelNames = new ArrayList<>();

    channelNames.add(Constant.TIME_CHANNEL_NAME);
    channelNames.add(Constant.ACC_UP_CHANNEL_NAME);
    channelNames.add(Constant.ACC_SAD_CHANNEL_NAME);
    channelNames.add(Constant.ACC_DOWN_CHANNEL_NAME);
    channelNames.add(Constant.ACC_STUDIES_CHANNEL_NAME);
    channelNames.add(Constant.ACC_RESTORE_CHANNEL_NAME);
    channelNames.add(Constant.ACC_ACC_CHANNEL_NAME);

    List<DBR> dbrs = SimpleGet.doAsyncGet(context, channelNames);

    AcceleratorBeamAvailability accounting = new AcceleratorBeamAvailability();

    accounting.setTime(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setUp(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setSad(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setDown(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setStudies(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setRestore(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setAcc(SimpleGet.getDoubleValue(dbrs.remove(0)));

    return accounting;
  }
}
