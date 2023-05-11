package org.jlab.btm.persistence.enumeration;

/**
 * Represents the beam time accounting data source.
 *
 * @author ryans
 */
public enum DataSource {
    DATABASE("Database"),
    EPICS("Epics"),
    NONE("None");

    private final String label;

    DataSource(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
