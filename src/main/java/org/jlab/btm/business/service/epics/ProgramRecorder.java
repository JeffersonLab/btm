package org.jlab.btm.business.service.epics;

import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.btm.business.service.CcProgramIntervalService;
import org.jlab.btm.persistence.enumeration.TimelineProgram;
import org.jlab.btm.persistence.epics.Constant;

/**
 * Records the accelerator program that BOOM reports through EPICS in the State Timeline.
 *
 * <p>The program PV's cached value is checked every few seconds; the time recorded for a change is
 * when the value arrived, not when it was checked. Only a change of the PV is recorded, so a
 * correction someone makes to the timeline stands until BOOM reports another program. The first
 * value after startup is compared with the timeline, to catch a change made while this application
 * was down; that change is recorded as of when the value arrived.
 *
 * <p>While the PV is disconnected nothing is recorded, and the current program stays what it was.
 */
@Singleton
public class ProgramRecorder {
  private static final Logger LOGGER = Logger.getLogger(ProgramRecorder.class.getName());

  /** How often the program PV's cached value is checked. */
  static final int CHECK_SECONDS = 5;

  @EJB PVCache cache;
  @EJB CcProgramIntervalService programService;

  /** The PV value that was handled last; null until the first one. */
  private String handled;

  @Schedule(hour = "*", minute = "*", second = "*/" + CHECK_SECONDS, persistent = false)
  void record() {
    String pv = programPv();

    if (pv == null) {
      return;
    }

    PVCacheEntry entry = cache.get(pv);

    if (entry == null || !entry.isConnected()) {
      return;
    }

    String value;

    try {
      value = entry.getString();
    } catch (IllegalStateException e) {
      value = String.valueOf(entry.getValue());
    }

    if (value.equals(handled)) {
      return;
    }

    TimelineProgram program = TimelineProgram.fromPvValue(value);

    if (program == null) {
      LOGGER.log(
          Level.WARNING,
          "Program PV {0} has value \"{1}\", which names no program; the current program stays"
              + " what it was",
          new Object[] {pv, value});
    } else if (programService.recordMeasured(program, entry.getTs().toInstant())) {
      LOGGER.log(Level.INFO, "Program PV {0} changed to {1}", new Object[] {pv, program});
    }

    // After the call: if recording fails, the value is tried again at the next check
    handled = value;
  }

  /** Package-private so a test can name its own PV. */
  String programPv() {
    return Constant.programPv();
  }
}
