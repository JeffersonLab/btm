package org.jlab.btm.business.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.jlab.btm.persistence.entity.CcAccHour;
import org.jlab.btm.persistence.enumeration.DataSource;
import org.junit.jupiter.api.Test;

class HourUtilTest {
  @Test
  void mapsHoursByTimestampAndKeepsLastDuplicate() {
    CcAccHour first = hour(0);
    CcAccHour replacement = hour(0);
    CcAccHour later = hour(1);

    Map<Date, CcAccHour> result = HourUtil.createHourMap(List.of(first, later, replacement));

    assertEquals(2, result.size());
    assertSame(replacement, result.get(time(0)));
    assertSame(later, result.get(time(1)));
  }

  @Test
  void mapsEmptyInputToEmptyMap() {
    assertTrue(HourUtil.createHourMap(List.of()).isEmpty());
  }

  @Test
  void subsetIncludesBothEndpointsSortsHoursAndSkipsGaps() {
    CcAccHour first = hour(1);
    CcAccHour last = hour(3);
    List<CcAccHour> input = List.of(hour(4), last, hour(0), first);

    List<CcAccHour> result = HourUtil.subset(time(1), time(3), input);

    assertEquals(2, result.size());
    assertSame(first, result.get(0));
    assertSame(last, result.get(1));
    assertEquals(4, input.size());
  }

  @Test
  void subsetCanSelectOneHour() {
    CcAccHour selected = hour(1);
    List<CcAccHour> result = HourUtil.subset(time(1), time(1), List.of(hour(0), selected, hour(2)));
    assertEquals(1, result.size());
    assertSame(selected, result.get(0));
  }

  @Test
  void subsetWithNoMatchingHoursIsEmpty() {
    assertTrue(HourUtil.subset(time(1), time(3), List.of(hour(0), hour(4))).isEmpty());
  }

  @Test
  void fillsGapsWithDatabaseTakingPrecedenceOverEpics() {
    CcAccHour database = hour(0);
    CcAccHour shadowedEpics = hour(0);
    CcAccHour epics = hour(1);
    Map<Date, CcAccHour> db = Map.of(time(0), database);
    Map<Date, CcAccHour> measured = Map.of(time(0), shadowedEpics, time(1), epics);

    List<CcAccHour> result =
        HourUtil.fillMissingHoursAndSetSource(db, measured, time(0), time(2), CcAccHour.class);

    assertEquals(3, result.size());
    assertSame(database, result.get(0));
    assertEquals(DataSource.DATABASE, result.get(0).getSource());
    assertSame(epics, result.get(1));
    assertEquals(DataSource.EPICS, result.get(1).getSource());
    assertEquals(time(2), result.get(2).getDayAndHour());
    assertEquals(DataSource.NONE, result.get(2).getSource());
    assertEquals(0, result.get(2).getUpSeconds());
    assertNull(shadowedEpics.getSource());
    assertEquals(1, db.size());
    assertEquals(2, measured.size());
  }

  @Test
  void createsDistinctPlaceholdersWhenAllHoursAreMissing() {
    List<CcAccHour> result =
        HourUtil.fillMissingHoursAndSetSource(
            Map.of(), Map.of(), time(0), time(1), CcAccHour.class);
    assertEquals(2, result.size());
    assertNotSame(result.get(0), result.get(1));
    for (int i = 0; i < result.size(); i++) {
      assertEquals(time(i), result.get(i).getDayAndHour());
      assertEquals(DataSource.NONE, result.get(i).getSource());
    }
  }

  private static CcAccHour hour(int offset) {
    CcAccHour hour = new CcAccHour();
    hour.setDayAndHour(time(offset));
    return hour;
  }

  private static Date time(int offset) {
    return Date.from(Instant.parse("2026-10-27T04:00:00Z").plusSeconds(offset * 3600L));
  }
}
