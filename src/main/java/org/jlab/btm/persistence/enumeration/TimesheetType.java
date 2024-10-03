package org.jlab.btm.persistence.enumeration;

/**
 * @author ryans
 */
public enum TimesheetType {
  CC("Crew Chief"),
  EA("Experimenter A"),
  EB("Experimenter B"),
  EC("Experimenter C"),
  ED("Experimenter D");

  private final String label;

  TimesheetType(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
