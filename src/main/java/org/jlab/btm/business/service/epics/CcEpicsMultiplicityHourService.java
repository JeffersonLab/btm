package org.jlab.btm.business.service.epics;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
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

  private MultiplicityBeamAvailability getFromCache() throws CALoadException {
    MultiplicityBeamAvailability accounting = new MultiplicityBeamAvailability();

    List<PVCacheEntry> entries = new ArrayList<>();

    entries.add(cache.getOrThrow(Constant.TIME_CHANNEL_NAME));
    entries.add(cache.getOrThrow(Constant.MULTI_ONE_UP));
    entries.add(cache.getOrThrow(Constant.MULTI_TWO_UP));
    entries.add(cache.getOrThrow(Constant.MULTI_THREE_UP));
    entries.add(cache.getOrThrow(Constant.MULTI_FOUR_UP));
    entries.add(cache.getOrThrow(Constant.MULTI_ANY_UP));
    entries.add(cache.getOrThrow(Constant.MULTI_ALL_UP));
    entries.add(cache.getOrThrow(Constant.MULTI_DOWN));

    accounting.setTime(entries.remove(0).getDoubleArray());
    accounting.setOneUp(entries.remove(0).getDoubleArray());
    accounting.setTwoUp(entries.remove(0).getDoubleArray());
    accounting.setThreeUp(entries.remove(0).getDoubleArray());
    accounting.setFourUp(entries.remove(0).getDoubleArray());
    accounting.setAnyUp(entries.remove(0).getDoubleArray());
    accounting.setAllUp(entries.remove(0).getDoubleArray());
    accounting.setDownHard(entries.remove(0).getDoubleArray());

    return accounting;
  }
}
