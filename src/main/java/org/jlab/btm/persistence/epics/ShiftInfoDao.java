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
public class ShiftInfoDao {

  private Context context = null;

  /**
   * Constructs an Accelerator Beam Availability data access object.
   *
   * @param context the EPICS context.
   */
  public ShiftInfoDao(Context context) {
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
  public ShiftInfo loadAccounting() throws InterruptedException, TimeoutException, CAException {
    List<String> channelNames = new ArrayList<>();

    channelNames.add(Constant.CREW_CHIEF_CHANNEL_NAME);
    channelNames.add(Constant.OPERATORS_CHANNEL_NAME);
    channelNames.add(Constant.PROGRAM_CHANNEL_NAME);
    channelNames.add(Constant.PROGRAM_DEPUTY_CHANNEL_NAME);
    channelNames.add(Constant.COMMENTS_CHANNEL_NAME);

    List<DBR> dbrs = SimpleGet.doAsyncGet(context, channelNames);

    ShiftInfo accounting = new ShiftInfo();

    accounting.setCrewChief(SimpleGet.getStringValue(dbrs.remove(0))[0]);
    accounting.setOperators(SimpleGet.getStringValue(dbrs.remove(0))[0]);
    accounting.setProgram(SimpleGet.getStringValue(dbrs.remove(0))[0]);
    accounting.setProgramDeputy(SimpleGet.getStringValue(dbrs.remove(0))[0]);
    accounting.setComments(SimpleGet.getStringValue(dbrs.remove(0))[0]);

    return accounting;
  }
}
