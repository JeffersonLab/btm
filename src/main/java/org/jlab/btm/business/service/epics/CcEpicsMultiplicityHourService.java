package org.jlab.btm.business.service.epics;

import gov.aps.jca.dbr.DBR;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.jlab.btm.business.util.CALoadException;
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
      throws CALoadException {
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

  private List<CcMultiplicityHour> loadAccounting() throws CALoadException {

    MultiplicityBeamAvailability accounting;

    accounting = getFromCache();

    return accounting.getOpMultiplicityHours();
  }

  private MultiplicityBeamAvailability getFromCache() {
    MultiplicityBeamAvailability accounting = new MultiplicityBeamAvailability();

    List<DBR> dbrs = new ArrayList<>();

    dbrs.add(cache.get(Constant.TIME_CHANNEL_NAME).getDbr());
    dbrs.add(cache.get(Constant.MULTI_ONE_UP).getDbr());
    dbrs.add(cache.get(Constant.MULTI_TWO_UP).getDbr());
    dbrs.add(cache.get(Constant.MULTI_THREE_UP).getDbr());
    dbrs.add(cache.get(Constant.MULTI_FOUR_UP).getDbr());
    dbrs.add(cache.get(Constant.MULTI_ANY_UP).getDbr());
    dbrs.add(cache.get(Constant.MULTI_ALL_UP).getDbr());
    dbrs.add(cache.get(Constant.MULTI_DOWN).getDbr());

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
