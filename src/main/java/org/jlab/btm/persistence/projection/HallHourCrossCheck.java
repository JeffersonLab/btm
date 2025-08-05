package org.jlab.btm.persistence.projection;

import java.util.Date;
import org.jlab.btm.persistence.entity.CcAccHour;
import org.jlab.btm.persistence.entity.CcHallHour;
import org.jlab.btm.persistence.entity.CcMultiplicityHour;
import org.jlab.btm.persistence.entity.ExpHour;
import org.jlab.btm.persistence.enumeration.DurationUnits;
import org.jlab.btm.presentation.util.BtmFunctions;
import org.jlab.smoothness.persistence.enumeration.Hall;

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

  private final boolean highAbu;
  private final boolean lowAbu;
  private final boolean lowBanu;
  private final boolean highBna;
  private final boolean highOff;

  // Check messages
  private final String highHallPhysicsMessage;
  private final String highUpMessage;

  private final String highAbuAccMessage;
  private final String highBanuAccMessage;
  private final String highBnaAccMessage;

  private final String highAbuMessage;
  private final String lowAbuMessage;
  private final String lowBanuMessage;
  private final String highBnaMessage;
  private final String highOffMessage;

  public HallHourCrossCheck(
      Hall hall,
      Date dayAndHour,
      CcAccHour ccAccHour,
      CcMultiplicityHour opMultiHour,
      CcHallHour ccHallHour,
      ExpHour expHour) {
    this.hall = hall;
    this.dayAndHour = dayAndHour;
    /*this.opAccHour = opAccHour;
    this.opMultiHour = opMultiHour;
    this.opHallHour = opHallHour;
    this.expHallHour = expHallHour;*/

    // Beam mode and multiplicity
    highHallPhysicsMessage =
        "Operations Hall "
            + hall
            + " reports significantly more UP + TUNE + BNR + PHYSICS DOWN ("
            + BtmFunctions.formatDuration(
                ccHallHour.getUpSeconds()
                    + ccHallHour.getTuneSeconds()
                    + ccHallHour.getBnrSeconds()
                    + ccHallHour.getDownSeconds(),
                DurationUnits.MINUTES)
            + " minutes) than the Operations Accelerator reported PHYSICS ("
            + BtmFunctions.formatDuration(ccAccHour.getUpSeconds(), DurationUnits.MINUTES)
            + " minutes)";

    highUpMessage =
        "Operations reports significantly more UP ("
            + BtmFunctions.formatDuration(ccHallHour.getUpSeconds(), DurationUnits.MINUTES)
            + " minutes) for Hall "
            + hall
            + " than ANY UP ("
            + BtmFunctions.formatDuration(opMultiHour.getAnyHallUpSeconds(), DurationUnits.MINUTES)
            + " minutes)";

    // Accelerator Experimenter vs Operations
    highAbuAccMessage =
        "Experimenter Hall "
            + hall
            + " reports significantly more ABU ("
            + BtmFunctions.formatDuration(expHour.getAbuSeconds(), DurationUnits.MINUTES)
            + " minutes) than the Operations reported PHYSICS + STUDIES + RESTORE ("
            + BtmFunctions.formatDuration(
                ccAccHour.getUpSeconds()
                    + ccAccHour.getRestoreSeconds()
                    + ccAccHour.getStudiesSeconds(),
                DurationUnits.MINUTES)
            + " minutes)";
    highBanuAccMessage =
        "Experimenter Hall "
            + hall
            + " reports significantly more BANU ("
            + BtmFunctions.formatDuration(expHour.getBanuSeconds(), DurationUnits.MINUTES)
            + " minutes) than the Operations reported PHYSICS ("
            + BtmFunctions.formatDuration(ccAccHour.getUpSeconds(), DurationUnits.MINUTES)
            + " minutes)";
    highBnaAccMessage =
        "Experimenter Hall "
            + hall
            + " reports significantly more BNA ("
            + BtmFunctions.formatDuration(expHour.getBnaSeconds(), DurationUnits.MINUTES)
            + " minutes) than the Operations reported PHYSICS ("
            + BtmFunctions.formatDuration(ccAccHour.getUpSeconds(), DurationUnits.MINUTES)
            + " minutes)";

    // Hall Experimeneter vs Operations
    highAbuMessage =
        "Experimenter Hall "
            + hall
            + " reports significantly more ABU + BANU ("
            + BtmFunctions.formatDuration(
                expHour.getAbuSeconds() + expHour.getBanuSeconds(), DurationUnits.MINUTES)
            + " minutes) than the Operations reported UP + TUNE + BNR ("
            + BtmFunctions.formatDuration(
                ccHallHour.getUpSeconds()
                    + ccHallHour.getTuneSeconds()
                    + ccHallHour.getBnrSeconds(),
                DurationUnits.MINUTES)
            + " minutes)";
    lowAbuMessage =
        "Experimenter Hall "
            + hall
            + " reports significantly less ABU + BANU ("
            + BtmFunctions.formatDuration(
                expHour.getAbuSeconds() + expHour.getBanuSeconds(), DurationUnits.MINUTES)
            + " minutes) than the Operations reported UP + TUNE + BNR ("
            + BtmFunctions.formatDuration(
                ccHallHour.getUpSeconds()
                    + ccHallHour.getTuneSeconds()
                    + ccHallHour.getBnrSeconds(),
                DurationUnits.MINUTES)
            + " minutes)";
    lowBanuMessage =
        "Experimenter Hall "
            + hall
            + " reports significantly less BANU ("
            + BtmFunctions.formatDuration(expHour.getBanuSeconds(), DurationUnits.MINUTES)
            + " minutes) vs the Operations reported BNR ("
            + BtmFunctions.formatDuration(ccHallHour.getBnrSeconds(), DurationUnits.MINUTES)
            + " minutes)";
    highBnaMessage =
        "Experimenter Hall "
            + hall
            + " reports significantly more BNA ("
            + BtmFunctions.formatDuration(expHour.getBnaSeconds(), DurationUnits.MINUTES)
            + " minutes) than the Operations reported UP + TUNE + DOWN ("
            + BtmFunctions.formatDuration(
                ccHallHour.getUpSeconds()
                    + ccHallHour.getTuneSeconds()
                    + ccHallHour.getDownSeconds(),
                DurationUnits.MINUTES)
            + " minutes)";
    highOffMessage =
        "Experimenter Hall "
            + hall
            + " reports significantly more OFF ("
            + BtmFunctions.formatDuration(expHour.getOffSeconds(), DurationUnits.MINUTES)
            + " minutes) than the Operations reported OFF ("
            + BtmFunctions.formatDuration(ccHallHour.getOffSeconds(), DurationUnits.MINUTES)
            + " minutes)";

    // Beam mode and multiplicity
    highHallPhysics =
        ccHallHour.getUpSeconds()
                + ccHallHour.getTuneSeconds()
                + ccHallHour.getBnrSeconds()
                + ccHallHour.getDownSeconds()
            > ccAccHour.getUpSeconds() + TEN_MINUTES_OF_SECONDS;
    highUp = ccHallHour.getUpSeconds() > opMultiHour.getAnyHallUpSeconds() + TEN_MINUTES_OF_SECONDS;

    // Accelerator Experimenter vs Operations
    highAbuAccCheck =
        expHour.getAbuSeconds()
            > ccAccHour.getUpSeconds()
                + ccAccHour.getRestoreSeconds()
                + ccAccHour.getStudiesSeconds()
                + TEN_MINUTES_OF_SECONDS;
    highBanuAccCheck = expHour.getBanuSeconds() > ccAccHour.getUpSeconds() + TEN_MINUTES_OF_SECONDS;
    highBnaAccCheck = expHour.getBnaSeconds() > ccAccHour.getUpSeconds() + TEN_MINUTES_OF_SECONDS;

    // Hall Experimenter vs Operations
    highAbu =
        ((expHour.getAbuSeconds() + expHour.getBanuSeconds()) / 2
                > ccHallHour.getUpSeconds()
                    + ccHallHour.getTuneSeconds()
                    + ccHallHour.getBnrSeconds())
            && (ccHallHour.getUpSeconds() > HALF_HOUR_OF_SECONDS);
    lowAbu =
        (expHour.getAbuSeconds() + expHour.getBanuSeconds()
                < (ccHallHour.getUpSeconds()
                        + ccHallHour.getTuneSeconds()
                        + ccHallHour.getBnrSeconds())
                    / 2)
            && (ccHallHour.getUpSeconds() > HALF_HOUR_OF_SECONDS);
    lowBanu = expHour.getBanuSeconds() + TEN_MINUTES_OF_SECONDS < ccHallHour.getBnrSeconds();
    highBna =
        expHour.getBnaSeconds()
            > ccHallHour.getDownSeconds()
                + ccHallHour.getUpSeconds()
                + ccHallHour.getTuneSeconds()
                + TEN_MINUTES_OF_SECONDS;
    highOff = expHour.getOffSeconds() > ccHallHour.getOffSeconds() + TEN_MINUTES_OF_SECONDS;
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

  public boolean isHighAbuAccCheck() {
    return highAbuAccCheck;
  }

  public boolean isHighBanuAccCheck() {
    return highBanuAccCheck;
  }

  public boolean isHighBnaAccCheck() {
    return highBnaAccCheck;
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
}
