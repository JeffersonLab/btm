package org.jlab.btm.persistence.enumeration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class TimelineProgramTest {
  @ParameterizedTest
  @CsvSource({
    "PHYSICS,PHYSICS",
    "physics,PHYSICS",
    "NULL,PHYSICS",
    "STUDIES,STUDIES",
    "MD,STUDIES",
    "' Restore ',RESTORE",
    "ACC,ACC",
    "OFF,OFF"
  })
  void mapsPvValuesToPrograms(String value, TimelineProgram program) {
    assertEquals(program, TimelineProgram.fromPvValue(value));
  }

  /** BOOM's DOWN in particular: downtime comes from DTM, so it must not become a program. */
  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "DOWN", "FSD", "3", "PHYSICS DOWN"})
  void mapsOtherPvValuesToNoProgram(String value) {
    assertNull(TimelineProgram.fromPvValue(value));
  }

  @Test
  void onlyStudiesRestoreAndAccAreInternal() {
    assertFalse(TimelineProgram.PHYSICS.isInternal());
    assertTrue(TimelineProgram.STUDIES.isInternal());
    assertTrue(TimelineProgram.RESTORE.isInternal());
    assertTrue(TimelineProgram.ACC.isInternal());
    assertFalse(TimelineProgram.OFF.isInternal());
  }
}
