package org.jlab.btm.persistence.epics;

import gov.aps.jca.CAException;
import gov.aps.jca.Context;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import java.util.ArrayList;
import java.util.List;
import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * Data Access Object (DAO) responsible for reading Experimenter BTA from an EPICS IOC.
 *
 * @author ryans
 */
public class ExperimenterAccountingDao {

  private Hall hall = null;
  private Context context = null;

  /**
   * Constructs an experimenter accounting data access object.
   *
   * @param hall the hall.
   * @param context the EPICS context.
   */
  public ExperimenterAccountingDao(Hall hall, Context context) {
    this.hall = hall;
    this.context = context;
  }

  /**
   * Load experimenter Beam Time Accounting (BTA) data from EPICS.
   *
   * @return the accounting data.
   * @throws InterruptedException if the waiting thread is interrupted before it is notified.
   * @throws TimeoutException if the waiting has exceeded the timeout period.
   * @throws CAException if a problem occurs transferring the EPICS data.
   */
  public ExperimenterAccounting loadAccounting()
      throws InterruptedException, TimeoutException, CAException {
    List<String> channelNames = new ArrayList<String>();

    channelNames.add(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_TIME_SUFFIX);
    channelNames.add(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_ABU_SUFFIX);
    channelNames.add(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_BANU_SUFFIX);
    channelNames.add(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_BNA_SUFFIX);
    channelNames.add(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_ACC_SUFFIX);
    channelNames.add(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_ER_SUFFIX);
    channelNames.add(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_PCC_SUFFIX);
    channelNames.add(Constant.EXP_HALL_PREFIX + hall + Constant.EXP_UED_SUFFIX);

    channelNames.add(Constant.HALL_PREFIX + hall + Constant.HALL_OFF_SUFFIX);

    List<DBR> dbrs = SimpleGet.doAsyncGet(context, channelNames);

    ExperimenterAccounting accounting = new ExperimenterAccounting();
    accounting.setHall(hall);

    accounting.setTime(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setABU(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setBANU(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setBNA(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setACC(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setER(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setPCC(SimpleGet.getDoubleValue(dbrs.remove(0)));
    accounting.setUED(SimpleGet.getDoubleValue(dbrs.remove(0)));

    accounting.setOFF(SimpleGet.getDoubleValue(dbrs.remove(0)));

    return accounting;
  }
}
