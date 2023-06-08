package org.jlab.btm.persistence.epics;

import org.jlab.btm.persistence.entity.CcShift;
import org.jlab.btm.persistence.enumeration.DataSource;

/**
 * Represents Accelerator Beam Availability for one week as recorded by an
 * EPICS IOC.
 *
 * @author ryans
 */
public class ShiftInfo {

    private String crewChief;
    private String operators;
    private String program;
    private String programDeputy;
    private String comments;

    public String getCrewChief() {
        return crewChief;
    }

    public void setCrewChief(String crewChief) {
        this.crewChief = crewChief;
    }

    public String getOperators() {
        return operators;
    }

    public void setOperators(String operators) {
        this.operators = operators;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getProgramDeputy() {
        return programDeputy;
    }

    public void setProgramDeputy(String programDeputy) {
        this.programDeputy = programDeputy;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    /**
     * Convert the data into an OpShift
     *
     * @return The shift
     */
    public CcShift getOpShift() {
        CcShift shift = new CcShift();

        shift.setCrewChief(crewChief);
        shift.setOperators(operators);
        shift.setProgram(program);
        shift.setProgramDeputy(programDeputy);
        shift.setRemark(comments);

        shift.setSource(DataSource.EPICS);

        return shift;
    }
}
