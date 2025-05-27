package org.jlab.btm.business.service.epics;

import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.jlab.btm.business.util.HourUtil;
import org.jlab.btm.persistence.entity.CcHallHour;
import org.jlab.btm.persistence.entity.CcMultiplicityHour;
import org.jlab.btm.persistence.epics.*;

/**
 * Responsible for querying experimenter hall EPICS time accounting data.
 *
 * @author ryans
 */
@Stateless
public class CcEpicsMultiplicityHourService {

  private static final Logger logger =
      Logger.getLogger(CcEpicsMultiplicityHourService.class.getName());
  @EJB PVCache cache;

  public List<CcMultiplicityHour> find(
      Date startDayAndHour, Date endDayAndHour, List<List<CcHallHour>> hallHoursList)
      throws TimeoutException, InterruptedException, CAException {
    List<CcMultiplicityHour> hours;

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

  private List<CcMultiplicityHour> loadAccounting()
      throws TimeoutException, InterruptedException, CAException {

    MultiplicityBeamAvailability accounting;

    long start = System.currentTimeMillis();
    accounting = getFromCache();
    long end = System.currentTimeMillis();
    logger.log(
        Level.FINEST, "EPICS multiplicity hall hours load time (milliseconds): {0}", (end - start));

    return accounting.getOpMultiplicityHours();
  }

  private MultiplicityBeamAvailability getFromCache() {
    MultiplicityBeamAvailability accounting = new MultiplicityBeamAvailability();

    List<DBR> dbrs = new ArrayList<>();

    dbrs.add(cache.get(Constant.TIME_CHANNEL_NAME));
    dbrs.add(cache.get(Constant.MULTI_ONE_UP));
    dbrs.add(cache.get(Constant.MULTI_TWO_UP));
    dbrs.add(cache.get(Constant.MULTI_THREE_UP));
    dbrs.add(cache.get(Constant.MULTI_FOUR_UP));
    dbrs.add(cache.get(Constant.MULTI_ANY_UP));
    dbrs.add(cache.get(Constant.MULTI_ALL_UP));
    dbrs.add(cache.get(Constant.MULTI_DOWN));

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
