package org.jlab.btm.persistence.enumeration;

import org.jlab.smoothness.persistence.enumeration.Hall;

public enum Role {

    USER("**", "User"),
    OPERABILITY_MANAGER("btm-admin", "Operability Manager"),
    CREW_CHIEF("cc", "Crew Chief"),
    HALL_A_MANAGER("halead", "Hall A Manager"),
    HALL_B_MANAGER("hblead", "Hall B Manager"),
    HALL_C_MANAGER("hclead", "Hall C Manager"),
    HALL_D_MANAGER("hdlead", "Hall D Manager");

    private final String name;
    private final String label;

    Role(String name, String label) {
        this.name = name;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String getName() {
        return name;
    }

    public static Role getHallManagerRole(Hall hall) {
        Role role = null;

        switch(hall) {
            case A:
                role = Role.HALL_A_MANAGER;
                break;
            case B:
                role = Role.HALL_B_MANAGER;
                break;
            case C:
                role = Role.HALL_C_MANAGER;
                break;
            case D:
                role = Role.HALL_D_MANAGER;
                break;
        }

        return role;
    }
}
