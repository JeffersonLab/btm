package org.jlab.btm.business.service.epics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.epics.vtype.Alarm;
import org.epics.vtype.EnumDisplay;
import org.epics.vtype.Time;
import org.epics.vtype.VDouble;
import org.epics.vtype.VEnum;
import org.epics.vtype.VString;
import org.epics.vtype.VType;
import org.jlab.btm.business.service.CcProgramIntervalService;
import org.jlab.btm.persistence.enumeration.TimelineProgram;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Checks what the recorder passes on from the PV cache; the timeline itself is stood in for. */
class ProgramRecorderTest {
  private static final String PV = "TEST:PROGRAM";

  private final PVCache cache = new PVCache();
  private final FakeTimeline timeline = new FakeTimeline();
  private ProgramRecorder recorder;

  @BeforeEach
  void setUp() {
    recorder =
        new ProgramRecorder() {
          @Override
          String programPv() {
            return PV;
          }
        };
    recorder.cache = cache;
    recorder.programService = timeline;
  }

  @Test
  void recordsNothingWithoutAValue() {
    recorder.record();

    assertEquals(List.of(), timeline.recorded);
  }

  @Test
  void recordsNothingWithoutAProgramPv() {
    recorder =
        new ProgramRecorder() {
          @Override
          String programPv() {
            return null;
          }
        };
    recorder.cache = cache;
    recorder.programService = timeline;
    put(string("PHYSICS"), true, "2026-01-01T05:00:00Z");

    recorder.record();

    assertEquals(List.of(), timeline.recorded);
  }

  @Test
  void recordsEachChangeOnceAsOfWhenTheValueArrived() {
    put(string("PHYSICS"), true, "2026-01-01T05:00:00Z");
    recorder.record();
    recorder.record();

    put(enumeration(1, "PHYSICS", "STUDIES"), true, "2026-01-01T06:30:10Z");
    recorder.record();
    recorder.record();

    assertEquals(
        List.of("PHYSICS at 2026-01-01T05:00:00Z", "STUDIES at 2026-01-01T06:30:10Z"),
        timeline.recorded);
  }

  /** A correction made by hand must stand until BOOM reports another program. */
  @Test
  void doesNotRecordAnUnchangedValueAgainWhenItsTimeChanges() {
    put(string("PHYSICS"), true, "2026-01-01T05:00:00Z");
    recorder.record();

    put(string("PHYSICS"), true, "2026-01-01T09:00:00Z");
    recorder.record();

    assertEquals(List.of("PHYSICS at 2026-01-01T05:00:00Z"), timeline.recorded);
  }

  @Test
  void ignoresValuesThatNameNoProgram() {
    put(string("STUDIES"), true, "2026-01-01T05:00:00Z");
    recorder.record();

    put(string("DOWN"), true, "2026-01-01T06:00:00Z");
    recorder.record();

    put(
        VDouble.of(3.0, Alarm.none(), Time.now(), org.epics.vtype.Display.none()),
        true,
        "2026-01-01T06:30:00Z");
    recorder.record();

    put(string("STUDIES"), true, "2026-01-01T07:00:00Z");
    recorder.record();

    // The timeline is told of Studies again; it adds nothing, since that is still its program
    assertEquals(
        List.of("STUDIES at 2026-01-01T05:00:00Z", "STUDIES at 2026-01-01T07:00:00Z"),
        timeline.recorded);
  }

  @Test
  void waitsForADisconnectedPvToReconnect() {
    put(string("ACC"), false, "2026-01-01T05:00:00Z");
    recorder.record();

    assertEquals(List.of(), timeline.recorded);

    put(string("ACC"), true, "2026-01-01T05:00:00Z");
    recorder.record();

    assertEquals(List.of("ACC at 2026-01-01T05:00:00Z"), timeline.recorded);
  }

  @Test
  void triesAgainAfterRecordingFails() {
    put(string("OFF"), true, "2026-01-01T05:00:00Z");
    timeline.failing = true;

    try {
      recorder.record();
    } catch (IllegalStateException e) {
      // As when the database is unavailable; the timer service logs it
    }

    timeline.failing = false;
    recorder.record();

    assertEquals(List.of("OFF at 2026-01-01T05:00:00Z"), timeline.recorded);
  }

  private void put(VType value, boolean connected, String arrived) {
    cache
        .writer()
        .accept(PV, new PVCacheEntry(value, connected, Date.from(Instant.parse(arrived))));
  }

  private static VType string(String value) {
    return VString.of(value, Alarm.none(), Time.now());
  }

  private static VType enumeration(int index, String... labels) {
    return VEnum.of(index, EnumDisplay.of(labels), Alarm.none(), Time.now());
  }

  private static final class FakeTimeline extends CcProgramIntervalService {
    final List<String> recorded = new ArrayList<>();
    boolean failing = false;

    @Override
    public boolean recordMeasured(TimelineProgram program, Instant at) {
      if (failing) {
        throw new IllegalStateException("Synthetic failure");
      }

      recorded.add(program + " at " + at);

      return true;
    }
  }
}
