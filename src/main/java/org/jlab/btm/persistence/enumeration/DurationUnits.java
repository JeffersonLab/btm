package org.jlab.btm.persistence.enumeration;

/**
 * @author ryans
 */
public enum DurationUnits {
  HOURS("Hours"),
  MINUTES("Minutes"),
  SECONDS("Seconds");

  private final String label;

  DurationUnits(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
