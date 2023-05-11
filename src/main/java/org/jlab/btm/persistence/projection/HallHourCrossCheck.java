package org.jlab.btm.persistence.projection;

import org.jlab.btm.persistence.entity.ExpHallHour;
import org.jlab.btm.persistence.entity.OpAccHour;
import org.jlab.btm.persistence.entity.OpHallHour;
import org.jlab.btm.persistence.entity.OpMultiplicityHour;
import org.jlab.btm.persistence.enumeration.DurationUnits;
import org.jlab.btm.presentation.util.BtmFunctions;
import org.jlab.smoothness.persistence.enumeration.Hall;

import java.util.Date;

/**
 * @author ryans
 */
public class HallHourCrossCheck {

    private static final int HALF_HOUR_OF_SECONDS = 1800;
    private static final int TEN_MINUTES_OF_SECONDS = 600;

    private final Hall hall;
    private final Date dayAndHour;
    /*private final OpAccHour opAccHour;
    private final OpMultiplicityHour opMultiHour;
    private final OpHallHour opHallHour;
    private final ExpHallHour expHallHour;*/

    // Check results
    private final boolean highHallPhysics;
    private final boolean highUp;

    private final boolean highAbuAccCheck;
    private final boolean highBanuAccCheck;
    private final boolean highBnaAccCheck;
    private final boolean highAccAccCheck;

    private final boolean highAbu;
    private final boolean lowAbu;
    private final boolean lowBanu;
    private final boolean highBna;
    private final boolean highOff;
    private final boolean highAcc;

    // Check messages
    private final String highHallPhysicsMessage;
    private final String highUpMessage;

    private final String highAbuAccMessage;
    private final String highBanuAccMessage;
    private final String highBnaAccMessage;
    private final String highAccAccMessage;

    private final String highAbuMessage;
    private final String lowAbuMessage;
    private final String lowBanuMessage;
    private final String highBnaMessage;
    private final String highOffMessage;
    private final String highAccMessage;

    public HallHourCrossCheck(Hall hall, Date dayAndHour, OpAccHour opAccHour, OpMultiplicityHour opMultiHour, OpHallHour opHallHour, ExpHallHour expHallHour) {
        this.hall = hall;
        this.dayAndHour = dayAndHour;
        /*this.opAccHour = opAccHour;
        this.opMultiHour = opMultiHour;
        this.opHallHour = opHallHour;
        this.expHallHour = expHallHour;*/

        // Beam mode and multiplicity
        highHallPhysicsMessage = "Operations Hall " + hall
                + " reports significantly more UP + TUNE + BNR + PHYSICS DOWN ("
                + BtmFunctions.formatDuration(opHallHour.getUpSeconds()
                + opHallHour.getTuneSeconds() + opHallHour.getBnrSeconds()
                + opHallHour.getDownSeconds(), DurationUnits.MINUTES)
                + " minutes) than the Operations Accelerator reported PHYSICS ("
                + BtmFunctions.formatDuration(opAccHour.getUpSeconds(), DurationUnits.MINUTES) + " minutes)";

        highUpMessage = "Operations reports significantly more UP ("
                + BtmFunctions.formatDuration(opHallHour.getUpSeconds(), DurationUnits.MINUTES)
                + " minutes) for Hall " + hall
                + " than ANY UP ("
                + BtmFunctions.formatDuration(opMultiHour.getAnyHallUpSeconds(), DurationUnits.MINUTES)
                + " minutes)";

        // Accelerator Experimenter vs Operations
        highAbuAccMessage = "Experimenter Hall " + hall
                + " reports significantly more ABU ("
                + BtmFunctions.formatDuration(expHallHour.getAbuSeconds(), DurationUnits.MINUTES)
                + " minutes) than the Operations reported PHYSICS + STUDIES + RESTORE ("
                + BtmFunctions.formatDuration(opAccHour.getUpSeconds() + opAccHour.getRestoreSeconds()
                + opAccHour.getStudiesSeconds(), DurationUnits.MINUTES) + " minutes)";
        highBanuAccMessage = "Experimenter Hall " + hall
                + " reports significantly more BANU ("
                + BtmFunctions.formatDuration(expHallHour.getBanuSeconds(), DurationUnits.MINUTES)
                + " minutes) than the Operations reported PHYSICS ("
                + BtmFunctions.formatDuration(opAccHour.getUpSeconds(), DurationUnits.MINUTES) + " minutes)";
        highBnaAccMessage = "Experimenter Hall " + hall
                + " reports significantly more BNA ("
                + BtmFunctions.formatDuration(expHallHour.getBnaSeconds(), DurationUnits.MINUTES)
                + " minutes) than the Operations reported PHYSICS ("
                + BtmFunctions.formatDuration(opAccHour.getUpSeconds(), DurationUnits.MINUTES) + " minutes)";
        highAccAccMessage = "Experimenter Hall " + hall
                + " reports significantly more ACC ("
                + BtmFunctions.formatDuration(expHallHour.getAccSeconds(), DurationUnits.MINUTES)
                + " minutes) than the Operations reported ACC ("
                + BtmFunctions.formatDuration(opAccHour.getAccSeconds(), DurationUnits.MINUTES) + " minutes)";

        // Hall Experimeneter vs Operations
        highAbuMessage = "Experimenter Hall " + hall
                + " reports significantly more ABU + BANU ("
                + BtmFunctions.formatDuration(expHallHour.getAbuSeconds() + expHallHour.getBanuSeconds(),
                DurationUnits.MINUTES)
                + " minutes) than the Operations reported UP + TUNE + BNR ("
                + BtmFunctions.formatDuration(opHallHour.getUpSeconds()
                        + opHallHour.getTuneSeconds() + opHallHour.getBnrSeconds(),
                DurationUnits.MINUTES)
                + " minutes)";
        lowAbuMessage = "Experimenter Hall " + hall
                + " reports significantly less ABU + BANU ("
                + BtmFunctions.formatDuration(expHallHour.getAbuSeconds() + expHallHour.getBanuSeconds(),
                DurationUnits.MINUTES)
                + " minutes) than the Operations reported UP + TUNE + BNR ("
                + BtmFunctions.formatDuration(opHallHour.getUpSeconds()
                        + opHallHour.getTuneSeconds() + opHallHour.getBnrSeconds(),
                DurationUnits.MINUTES)
                + " minutes)";
        lowBanuMessage = "Experimenter Hall " + hall + " reports significantly less BANU ("
                + BtmFunctions.formatDuration(expHallHour.getBanuSeconds(), DurationUnits.MINUTES)
                + " minutes) vs the Operations reported BNR (" + BtmFunctions.formatDuration(
                opHallHour.getBnrSeconds(),
                DurationUnits.MINUTES) + " minutes)";
        highBnaMessage = "Experimenter Hall " + hall + " reports significantly more BNA ("
                + BtmFunctions.formatDuration(expHallHour.getBnaSeconds(), DurationUnits.MINUTES)
                + " minutes) than the Operations reported UP + TUNE + DOWN ("
                + BtmFunctions.formatDuration(opHallHour.getUpSeconds()
                        + opHallHour.getTuneSeconds() + opHallHour.getDownSeconds(),
                DurationUnits.MINUTES)
                + " minutes)";
        highOffMessage = "Experimenter Hall " + hall + " reports significantly more OFF ("
                + BtmFunctions.formatDuration(expHallHour.getOffSeconds(), DurationUnits.MINUTES)
                + " minutes) than the Operations reported OFF (" + BtmFunctions.formatDuration(
                opHallHour.getOffSeconds(),
                DurationUnits.MINUTES) + " minutes)";
        highAccMessage = "Experimenter Hall " + hall + " reports significantly more ACC ("
                + BtmFunctions.formatDuration(expHallHour.getAccSeconds(), DurationUnits.MINUTES)
                + " minutes) than the Operations reported OFF (" + BtmFunctions.formatDuration(
                opHallHour.getOffSeconds(),
                DurationUnits.MINUTES) + " minutes)";

        // Beam mode and multiplicity
        highHallPhysics = opHallHour.getUpSeconds()
                + opHallHour.getTuneSeconds()
                + opHallHour.getBnrSeconds()
                + opHallHour.getDownSeconds() > opAccHour.getUpSeconds() + TEN_MINUTES_OF_SECONDS;
        highUp = opHallHour.getUpSeconds() > opMultiHour.getAnyHallUpSeconds() + TEN_MINUTES_OF_SECONDS;

        // Accelerator Experimenter vs Operations
        highAbuAccCheck = expHallHour.getAbuSeconds() > opAccHour.getUpSeconds()
                + opAccHour.getRestoreSeconds()
                + opAccHour.getStudiesSeconds() + TEN_MINUTES_OF_SECONDS;
        highBanuAccCheck = expHallHour.getBanuSeconds() > opAccHour.getUpSeconds() + TEN_MINUTES_OF_SECONDS;
        highBnaAccCheck = expHallHour.getBnaSeconds() > opAccHour.getUpSeconds() + TEN_MINUTES_OF_SECONDS;
        highAccAccCheck = expHallHour.getAccSeconds() > opAccHour.getAccSeconds() + TEN_MINUTES_OF_SECONDS;

        // Hall Experimenter vs Operations
        highAbu = ((expHallHour.getAbuSeconds() + expHallHour.getBanuSeconds()) / 2 > opHallHour.getUpSeconds()
                + opHallHour.getTuneSeconds() + opHallHour.getBnrSeconds()) && (opHallHour.getUpSeconds()
                > HALF_HOUR_OF_SECONDS);
        lowAbu = (expHallHour.getAbuSeconds() + expHallHour.getBanuSeconds() < (opHallHour.getUpSeconds()
                + opHallHour.getTuneSeconds() + opHallHour.getBnrSeconds()) / 2) && (opHallHour.getUpSeconds()
                > HALF_HOUR_OF_SECONDS);
        lowBanu = expHallHour.getBanuSeconds() + TEN_MINUTES_OF_SECONDS < opHallHour.getBnrSeconds();
        highBna = expHallHour.getBnaSeconds() > opHallHour.getDownSeconds()
                + opHallHour.getUpSeconds() + opHallHour.getTuneSeconds() + TEN_MINUTES_OF_SECONDS;
        highOff = expHallHour.getOffSeconds() > opHallHour.getOffSeconds() + TEN_MINUTES_OF_SECONDS;
        highAcc = expHallHour.getAccSeconds() > opHallHour.getOffSeconds() + TEN_MINUTES_OF_SECONDS;
    }

    public Hall getHall() {
        return hall;
    }

    public Date getDayAndHour() {
        return dayAndHour;
    }

    public boolean isHighHallPhysics() {
        return highHallPhysics;
    }

    public boolean isHighUp() {
        return highUp;
    }

    public String getHighHallPhysicsMessage() {
        return highHallPhysicsMessage;
    }

    public String getHighUpMessage() {
        return highUpMessage;
    }

    public boolean isHighAbu() {
        return highAbu;
    }

    public boolean isLowAbu() {
        return lowAbu;
    }

    public boolean isLowBanu() {
        return lowBanu;
    }

    public boolean isHighBna() {
        return highBna;
    }

    public boolean isHighOff() {
        return highOff;
    }

    public boolean isHighAcc() {
        return highAcc;
    }

    public String getHighAbuMessage() {
        return highAbuMessage;
    }

    public String getLowAbuMessage() {
        return lowAbuMessage;
    }

    public String getLowBanuMessage() {
        return lowBanuMessage;
    }

    public String getHighBnaMessage() {
        return highBnaMessage;
    }

    public String getHighOffMessage() {
        return highOffMessage;
    }

    public String getHighAccMessage() {
        return highAccMessage;
    }

    public boolean isHighAbuAccCheck() {
        return highAbuAccCheck;
    }

    public boolean isHighBanuAccCheck() {
        return highBanuAccCheck;
    }

    public boolean isHighBnaAccCheck() {
        return highBnaAccCheck;
    }

    public boolean isHighAccAccCheck() {
        return highAccAccCheck;
    }

    public String getHighAbuAccMessage() {
        return highAbuAccMessage;
    }

    public String getHighBanuAccMessage() {
        return highBanuAccMessage;
    }

    public String getHighBnaAccMessage() {
        return highBnaAccMessage;
    }

    public String getHighAccAccMessage() {
        return highAccAccMessage;
    }
}
