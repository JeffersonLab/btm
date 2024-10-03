package org.jlab.btm.persistence.projection;

import java.util.Date;
import org.jlab.btm.persistence.entity.ScheduleDay;

/**
 * @author ryans
 */
public class ProjectedScheduleDay extends ScheduleDay {

  public ProjectedScheduleDay(
      Date dayMonthYear,
      String accProgram,
      Number kiloVoltsPerPass,
      Number minHallCount,
      String hallANote,
      String hallBNote,
      String hallCNote,
      String hallDNote,
      String note,
      Number hallAProgramId,
      Number hallANanoAmps,
      Number hallAKiloVolts,
      Number hallAPasses,
      Number hallAPriority,
      Character hallAPolarized,
      Number hallBProgramId,
      Number hallBNanoAmps,
      Number hallBKiloVolts,
      Number hallBPasses,
      Number hallBPriority,
      Character hallBPolarized,
      Number hallCProgramId,
      Number hallCNanoAmps,
      Number hallCKiloVolts,
      Number hallCPasses,
      Number hallCPriority,
      Character hallCPolarized,
      Number hallDProgramId,
      Number hallDNanoAmps,
      Number hallDKiloVolts,
      Number hallDPasses,
      Number hallDPriority,
      Character hallDPolarized) {
    this.dayMonthYear = dayMonthYear;
    this.accProgram = accProgram;
    this.kiloVoltsPerPass = kiloVoltsPerPass == null ? null : kiloVoltsPerPass.intValue();
    this.minHallCount = minHallCount == null ? null : minHallCount.intValue();
    this.note = note;

    this.hallANanoAmps = hallANanoAmps == null ? null : hallANanoAmps.intValue();
    this.hallAKiloVolts = hallAKiloVolts == null ? null : hallAKiloVolts.intValue();
    this.hallAProgramId = hallAProgramId == null ? null : hallAProgramId.intValue();
    this.hallAPriority = hallAPriority == null ? null : hallAPriority.intValue();
    this.hallAPasses = hallAPasses == null ? null : hallAPasses.intValue();
    this.hallAPolarized = hallAPolarized == null ? null : hallAPolarized == '1';
    this.hallANote = hallANote;

    this.hallBNanoAmps = hallBNanoAmps == null ? null : hallBNanoAmps.intValue();
    this.hallBKiloVolts = hallBKiloVolts == null ? null : hallBKiloVolts.intValue();
    this.hallBProgramId = hallBProgramId == null ? null : hallBProgramId.intValue();
    this.hallBPriority = hallBPriority == null ? null : hallBPriority.intValue();
    this.hallBPasses = hallBPasses == null ? null : hallBPasses.intValue();
    this.hallBPolarized = hallBPolarized == null ? null : hallBPolarized == '1';
    this.hallBNote = hallBNote;

    this.hallCNanoAmps = hallCNanoAmps == null ? null : hallCNanoAmps.intValue();
    this.hallCKiloVolts = hallCKiloVolts == null ? null : hallCKiloVolts.intValue();
    this.hallCProgramId = hallCProgramId == null ? null : hallCProgramId.intValue();
    this.hallCPriority = hallCPriority == null ? null : hallCPriority.intValue();
    this.hallCPasses = hallCPasses == null ? null : hallCPasses.intValue();
    this.hallCPolarized = hallCPolarized == null ? null : hallCPolarized == '1';
    this.hallCNote = hallCNote;

    this.hallDNanoAmps = hallDNanoAmps == null ? null : hallDNanoAmps.intValue();
    this.hallDKiloVolts = hallDKiloVolts == null ? null : hallDKiloVolts.intValue();
    this.hallDProgramId = hallDProgramId == null ? null : hallDProgramId.intValue();
    this.hallDPriority = hallDPriority == null ? null : hallDPriority.intValue();
    this.hallDPasses = hallDPasses == null ? null : hallDPasses.intValue();
    this.hallDPolarized = hallDPolarized == null ? null : hallDPolarized == '1';
    this.hallDNote = hallDNote;
  }
}
