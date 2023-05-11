package org.jlab.btm.persistence.enumeration;

public enum Role {
    OPERABILITY_MANAGER("Operability Manager"),
    CREW_CHIEF("Crew Chief");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
