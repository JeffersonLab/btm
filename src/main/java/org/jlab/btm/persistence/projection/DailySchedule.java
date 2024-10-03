package org.jlab.btm.persistence.projection;

import java.util.Date;

/**
 * @author ryans
 */
public class DailySchedule {

  private Date day;
  private String accProgram;
  private Integer kiloVoltsPerPass;
  private String note;
  private Integer hallANanoAmps;
  private Integer hallAKiloVolts;
  private Integer hallAPurposeId;
  private Integer hallAPriority;
  private Integer hallAPasses;
  private Boolean hallAPolarized;
  private Integer hallBNanoAmps;
  private Integer hallBKiloVolts;
  private Integer hallBPurposeId;
  private Integer hallBPriority;
  private Integer hallBPasses;
  private Boolean hallBPolarized;
  private Integer hallCNanoAmps;
  private Integer hallCKiloVolts;
  private Integer hallCPurposeId;
  private Integer hallCPriority;
  private Integer hallCPasses;
  private Boolean hallCPolarized;
  private Integer hallDNanoAmps;
  private Integer hallDKiloVolts;
  private Integer hallDPurposeId;
  private Integer hallDPriority;
  private Integer hallDPasses;
  private Boolean hallDPolarized;

  public DailySchedule(
      Date day,
      String accProgram,
      Number kiloVoltsPerPass,
      String note,
      Number hallANanoAmps,
      Number hallAKiloVolts,
      Number hallAPurposeId,
      Number hallAPriority,
      Number hallAPasses,
      Character hallAPolarized,
      Number hallBNanoAmps,
      Number hallBKiloVolts,
      Number hallBPurposeId,
      Number hallBPriority,
      Number hallBPasses,
      Character hallBPolarized,
      Number hallCNanoAmps,
      Number hallCKiloVolts,
      Number hallCPurposeId,
      Number hallCPriority,
      Number hallCPasses,
      Character hallCPolarized,
      Number hallDNanoAmps,
      Number hallDKiloVolts,
      Number hallDPurposeId,
      Number hallDPriority,
      Number hallDPasses,
      Character hallDPolarized) {
    this.day = day;
    this.accProgram = accProgram;
    this.kiloVoltsPerPass = kiloVoltsPerPass == null ? null : kiloVoltsPerPass.intValue();
    this.note = note;

    this.hallANanoAmps = hallANanoAmps == null ? null : hallANanoAmps.intValue();
    this.hallAKiloVolts = hallAKiloVolts == null ? null : hallAKiloVolts.intValue();
    this.hallAPurposeId = hallAPurposeId == null ? null : hallAPurposeId.intValue();
    this.hallAPriority = hallAPriority == null ? null : hallAPriority.intValue();
    this.hallAPasses = hallAPasses == null ? null : hallAPasses.intValue();
    this.hallAPolarized = hallAPolarized == null ? null : hallAPolarized == '1';

    this.hallBNanoAmps = hallBNanoAmps == null ? null : hallBNanoAmps.intValue();
    this.hallBKiloVolts = hallBKiloVolts == null ? null : hallBKiloVolts.intValue();
    this.hallBPurposeId = hallBPurposeId == null ? null : hallBPurposeId.intValue();
    this.hallBPriority = hallBPriority == null ? null : hallBPriority.intValue();
    this.hallBPasses = hallBPasses == null ? null : hallBPasses.intValue();
    this.hallBPolarized = hallBPolarized == null ? null : hallBPolarized == '1';

    this.hallCNanoAmps = hallCNanoAmps == null ? null : hallCNanoAmps.intValue();
    this.hallCKiloVolts = hallCKiloVolts == null ? null : hallCKiloVolts.intValue();
    this.hallCPurposeId = hallCPurposeId == null ? null : hallCPurposeId.intValue();
    this.hallCPriority = hallCPriority == null ? null : hallCPriority.intValue();
    this.hallCPasses = hallCPasses == null ? null : hallCPasses.intValue();
    this.hallCPolarized = hallCPolarized == null ? null : hallCPolarized == '1';

    this.hallDNanoAmps = hallDNanoAmps == null ? null : hallDNanoAmps.intValue();
    this.hallDKiloVolts = hallDKiloVolts == null ? null : hallDKiloVolts.intValue();
    this.hallDPurposeId = hallDPurposeId == null ? null : hallDPurposeId.intValue();
    this.hallDPriority = hallDPriority == null ? null : hallDPriority.intValue();
    this.hallDPasses = hallDPasses == null ? null : hallDPasses.intValue();
    this.hallDPolarized = hallDPolarized == null ? null : hallDPolarized == '1';
  }

  public Date getDay() {
    return day;
  }

  public void setDay(Date day) {
    this.day = day;
  }

  public String getAccProgram() {
    return accProgram;
  }

  public void setAccProgram(String accProgram) {
    this.accProgram = accProgram;
  }

  public Integer getKiloVoltsPerPass() {
    return kiloVoltsPerPass;
  }

  public void setKiloVoltsPerPass(Integer kiloVoltsPerPass) {
    this.kiloVoltsPerPass = kiloVoltsPerPass;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public Integer getHallANanoAmps() {
    return hallANanoAmps;
  }

  public void setHallANanoAmps(Integer hallANanoAmps) {
    this.hallANanoAmps = hallANanoAmps;
  }

  public Integer getHallAKiloVolts() {
    return hallAKiloVolts;
  }

  public void setHallAKiloVolts(Integer hallAKiloVolts) {
    this.hallAKiloVolts = hallAKiloVolts;
  }

  public Integer getHallAPurposeId() {
    return hallAPurposeId;
  }

  public void setHallAPurposeId(Integer hallAPurposeId) {
    this.hallAPurposeId = hallAPurposeId;
  }

  public Integer getHallAPriority() {
    return hallAPriority;
  }

  public void setHallAPriority(Integer hallAPriority) {
    this.hallAPriority = hallAPriority;
  }

  public Integer getHallAPasses() {
    return hallAPasses;
  }

  public void setHallAPasses(Integer hallAPasses) {
    this.hallAPasses = hallAPasses;
  }

  public Boolean isHallAPolarized() {
    return hallAPolarized;
  }

  public void setHallAPolarized(Boolean hallAPolarized) {
    this.hallAPolarized = hallAPolarized;
  }

  public Integer getHallBNanoAmps() {
    return hallBNanoAmps;
  }

  public void setHallBNanoAmps(Integer hallBNanoAmps) {
    this.hallBNanoAmps = hallBNanoAmps;
  }

  public Integer getHallBKiloVolts() {
    return hallBKiloVolts;
  }

  public void setHallBKiloVolts(Integer hallBKiloVolts) {
    this.hallBKiloVolts = hallBKiloVolts;
  }

  public Integer getHallBPurposeId() {
    return hallBPurposeId;
  }

  public void setHallBPurposeId(Integer hallBPurposeId) {
    this.hallBPurposeId = hallBPurposeId;
  }

  public Integer getHallBPriority() {
    return hallBPriority;
  }

  public void setHallBPriority(Integer hallBPriority) {
    this.hallBPriority = hallBPriority;
  }

  public Integer getHallBPasses() {
    return hallBPasses;
  }

  public void setHallBPasses(Integer hallBPasses) {
    this.hallBPasses = hallBPasses;
  }

  public Boolean isHallBPolarized() {
    return hallBPolarized;
  }

  public void setHallBPolarized(Boolean hallBPolarized) {
    this.hallBPolarized = hallBPolarized;
  }

  public Integer getHallCNanoAmps() {
    return hallCNanoAmps;
  }

  public void setHallCNanoAmps(Integer hallCNanoAmps) {
    this.hallCNanoAmps = hallCNanoAmps;
  }

  public Integer getHallCKiloVolts() {
    return hallCKiloVolts;
  }

  public void setHallCKiloVolts(Integer hallCKiloVolts) {
    this.hallCKiloVolts = hallCKiloVolts;
  }

  public Integer getHallCPurposeId() {
    return hallCPurposeId;
  }

  public void setHallCPurposeId(Integer hallCPurposeId) {
    this.hallCPurposeId = hallCPurposeId;
  }

  public Integer getHallCPriority() {
    return hallCPriority;
  }

  public void setHallCPriority(Integer hallCPriority) {
    this.hallCPriority = hallCPriority;
  }

  public Integer getHallCPasses() {
    return hallCPasses;
  }

  public void setHallCPasses(Integer hallCPasses) {
    this.hallCPasses = hallCPasses;
  }

  public Boolean isHallCPolarized() {
    return hallCPolarized;
  }

  public void setHallCPolarized(Boolean hallCPolarized) {
    this.hallCPolarized = hallCPolarized;
  }

  public Integer getHallDNanoAmps() {
    return hallDNanoAmps;
  }

  public void setHallDNanoAmps(Integer hallDNanoAmps) {
    this.hallDNanoAmps = hallDNanoAmps;
  }

  public Integer getHallDKiloVolts() {
    return hallDKiloVolts;
  }

  public void setHallDKiloVolts(Integer hallDKiloVolts) {
    this.hallDKiloVolts = hallDKiloVolts;
  }

  public Integer getHallDPurposeId() {
    return hallDPurposeId;
  }

  public void setHallDPurposeId(Integer hallDPurposeId) {
    this.hallDPurposeId = hallDPurposeId;
  }

  public Integer getHallDPriority() {
    return hallDPriority;
  }

  public void setHallDPriority(Integer hallDPriority) {
    this.hallDPriority = hallDPriority;
  }

  public Integer getHallDPasses() {
    return hallDPasses;
  }

  public void setHallDPasses(Integer hallDPasses) {
    this.hallDPasses = hallDPasses;
  }

  public Boolean isHallDPolarized() {
    return hallDPolarized;
  }

  public void setHallDPolarized(Boolean hallDPolarized) {
    this.hallDPolarized = hallDPolarized;
  }
}
