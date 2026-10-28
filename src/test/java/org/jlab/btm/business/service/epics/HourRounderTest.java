package org.jlab.btm.business.service.epics;

import static org.junit.jupiter.api.Assertions.*;

import org.jlab.btm.persistence.entity.CcAccHour;
import org.jlab.btm.persistence.entity.CcHallHour;
import org.jlab.btm.persistence.entity.ExpHour;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HourRounderTest {
  private final HourRounder rounder = new HourRounder();

  @ParameterizedTest
  @CsvSource({"1770, 1770", "1830, 1830", "1800, 1800"})
  void reconcilesDifferencesUpToAndIncludingOneMinute(short first, short second) {
    short[] statuses = {first, second, 0};
    rounder.roundMutuallyExclusiveSet(statuses);
    assertArrayEquals(new short[] {1800, 1800, 0}, statuses);
  }

  @ParameterizedTest
  @CsvSource({"1769, 1770", "1831, 1830"})
  void leavesDiscrepanciesBeyondOneMinuteForReview(short first, short second) {
    short[] statuses = {first, second, 0};
    rounder.roundMutuallyExclusiveSet(statuses);
    assertArrayEquals(new short[] {first, second, 0}, statuses);
  }

  @Test
  void clampsMeasurementsOutsideTheHour() {
    short[] statuses = {-1, 3601, 0};
    rounder.roundMutuallyExclusiveSet(statuses);
    assertArrayEquals(new short[] {0, 3600, 0}, statuses);
  }

  @Test
  void doesNotInventActivityForEmptyStatuses() {
    short[] statuses = {0, 0, 0};
    rounder.roundMutuallyExclusiveSet(statuses);
    assertArrayEquals(new short[] {0, 0, 0}, statuses);
  }

  @Test
  void roundsHallMeasurementsToHundredthsWhilePreservingTotal() {
    CcHallHour hour = new CcHallHour();
    hour.setUpSeconds((short) 1789);
    hour.setTuneSeconds((short) 1811);

    rounder.roundHallHour(hour);

    assertEquals(1800, hour.getUpSeconds());
    assertEquals(1800, hour.getTuneSeconds());
    assertEquals(0, hour.getBnrSeconds());
    assertEquals(0, hour.getDownSeconds());
    assertEquals(0, hour.getOffSeconds());
  }

  @Test
  void addsUnaccountedAcceleratorTimeToExistingPhysics() {
    CcAccHour hour = new CcAccHour();
    hour.setUpSeconds((short) 1000);
    hour.setDownSeconds((short) 1800);

    rounder.roundAcceleratorHour(hour);

    assertEquals(1800, hour.getUpSeconds());
    assertEquals(1800, hour.getDownSeconds());
    assertEquals(0, hour.getStudiesSeconds());
    assertEquals(0, hour.getRestoreSeconds());
    assertEquals(0, hour.getAccSeconds());
    assertEquals(0, hour.getSadSeconds());
  }

  @Test
  void doesNotInventPhysicsWhenNoPhysicsWasMeasured() {
    CcAccHour hour = new CcAccHour();
    hour.setDownSeconds((short) 1800);
    rounder.roundAcceleratorHour(hour);
    assertEquals(0, hour.getUpSeconds());
    assertEquals(1800, hour.getDownSeconds());
  }

  @Test
  void reconcilesBothExperimenterSetsWithoutChangingSharedOffTime() {
    ExpHour hour = new ExpHour();
    hour.setOffSeconds((short) 600);
    hour.setAbuSeconds((short) 1470);
    hour.setBanuSeconds((short) 1470);
    hour.setErSeconds((short) 1530);
    hour.setPccSeconds((short) 1530);

    rounder.roundExpHour(hour);

    assertEquals(600, hour.getOffSeconds());
    assertEquals(1500, hour.getAbuSeconds());
    assertEquals(1500, hour.getBanuSeconds());
    assertEquals(0, hour.getBnaSeconds());
    assertEquals(1500, hour.getErSeconds());
    assertEquals(1500, hour.getPccSeconds());
    assertEquals(0, hour.getUedSeconds());
  }

  @Test
  void replacesMatchingExperimenterMetricsWithOffTime() {
    ExpHour hour = new ExpHour();
    hour.setOffSeconds((short) 3550);
    hour.setUedSeconds((short) 3600);
    rounder.roundExpHour(hour);
    assertEquals(3600, hour.getOffSeconds());
    assertEquals(0, hour.getUedSeconds());
    assertEquals(0, hour.getErSeconds());
    assertEquals(0, hour.getPccSeconds());
  }
}
