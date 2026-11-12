package org.jlab.btm.persistence.enumeration;

import java.util.Locale;

/**
 * The accelerator programs of the State Timeline: what the machine is scheduled to be doing.
 *
 * <p>There is no "down" program: downtime is recorded in DTM only, and is correlated with these
 * programs when it needs to be attributed to one.
 */
public enum TimelineProgram {
  PHYSICS("PHYSICS", false),
  STUDIES("STUDIES", true),
  RESTORE("NPES RESTORE", true),
  ACC("ACC", true),
  OFF("OFF (SAM)", false);

  private final String label;
  private final boolean internal;

  TimelineProgram(String label, boolean internal) {
    this.label = label;
    this.internal = internal;
  }

  public String getLabel() {
    return label;
  }

  /**
   * @return Whether downtime during this program counts as Internal Down
   */
  public boolean isInternal() {
    return internal;
  }

  /**
   * Maps a value of the program PV to a program. Accepts the names of this enumeration and the
   * names BOOM uses in its hourly PVs (BOOMCEBANULL_h for physics, BOOMCEBAMD_h for studies),
   * ignoring case and surrounding whitespace.
   *
   * @param value The PV's value: a string, or an enumeration's label
   * @return The program, or null if the value names none, such as BOOM's DOWN
   */
  public static TimelineProgram fromPvValue(String value) {
    if (value == null) {
      return null;
    }

    switch (value.trim().toUpperCase(Locale.ROOT)) {
      case "PHYSICS":
      case "NULL":
        return PHYSICS;
      case "STUDIES":
      case "MD":
        return STUDIES;
      case "RESTORE":
        return RESTORE;
      case "ACC":
        return ACC;
      case "OFF":
        return OFF;
      default:
        return null;
    }
  }
}
