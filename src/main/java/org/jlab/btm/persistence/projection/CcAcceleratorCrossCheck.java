package org.jlab.btm.persistence.projection;

import org.jlab.btm.persistence.enumeration.DurationUnits;
import org.jlab.btm.presentation.util.BtmFunctions;

/**
 * @author ryans
 */
public class CcAcceleratorCrossCheck {

  private static final int TEN_MINUTES_OF_SECONDS = 600;
  private final String[] halls = new String[] {"A", "B", "C", "D"};
  private final boolean passed;
  private final boolean[] hallPassed = new boolean[4];
  private final boolean[] highAbu = new boolean[4];
  private final boolean[] highBanu = new boolean[4];
  private final boolean[] highBna = new boolean[4];

  private final String[] highAbuMessage = new String[4];
  private final String[] highBanuMessage = new String[4];
  private final String[] highBnaMessage = new String[4];

  public CcAcceleratorCrossCheck(
      CcAccShiftTotals acc,
      ExpShiftTotals a,
      ExpShiftTotals b,
      ExpShiftTotals c,
      ExpShiftTotals d) {

    ExpShiftTotals[] times = new ExpShiftTotals[] {a, b, c, d};

    for (int i = 0; i < 4; i++) {
      highAbuMessage[i] =
          "Experimenter Hall "
              + halls[i]
              + " reports significantly more ABU ("
              + BtmFunctions.formatDuration(times[i].getAbuSeconds(), DurationUnits.HOURS)
              + " hours) than the Operations reported PHYSICS + STUDIES + RESTORE ("
              + BtmFunctions.formatDuration(
                  acc.getUpSeconds() + acc.getRestoreSeconds() + acc.getStudiesSeconds(),
                  DurationUnits.HOURS)
              + " hours)";
      highBanuMessage[i] =
          "Experimenter Hall "
              + halls[i]
              + " reports significantly more BANU ("
              + BtmFunctions.formatDuration(times[i].getBanuSeconds(), DurationUnits.HOURS)
              + " hours) than the Operations reported PHYSICS ("
              + BtmFunctions.formatDuration(acc.getUpSeconds(), DurationUnits.HOURS)
              + " hours)";
      highBnaMessage[i] =
          "Experimenter Hall "
              + halls[i]
              + " reports significantly more BNA ("
              + BtmFunctions.formatDuration(times[i].getBnaSeconds(), DurationUnits.HOURS)
              + " hours) than the Operations reported PHYSICS ("
              + BtmFunctions.formatDuration(acc.getUpSeconds(), DurationUnits.HOURS)
              + " hours)";
    }

    int accSeconds = acc.getUpSeconds() + acc.getRestoreSeconds() + acc.getStudiesSeconds();

    highAbu[0] = a.getAbuSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;
    highAbu[1] = b.getAbuSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;
    highAbu[2] = c.getAbuSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;
    highAbu[3] = d.getAbuSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;

    accSeconds = acc.getUpSeconds();
    highBanu[0] = a.getBanuSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;
    highBanu[1] = b.getBanuSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;
    highBanu[2] = c.getBanuSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;
    highBanu[3] = d.getBanuSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;

    accSeconds = acc.getUpSeconds();
    highBna[0] = a.getBnaSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;
    highBna[1] = b.getBnaSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;
    highBna[2] = c.getBnaSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;
    highBna[3] = d.getBnaSeconds() <= accSeconds + TEN_MINUTES_OF_SECONDS;

    hallPassed[0] = true;
    hallPassed[1] = true;
    hallPassed[2] = true;
    hallPassed[3] = true;

    for (int i = 0; i < 4; i++) {
      if (!highAbu[i]) {
        hallPassed[i] = false;
      }
      if (!highBanu[i]) {
        hallPassed[i] = false;
      }
      if (!highBna[i]) {
        hallPassed[i] = false;
      }
    }

    passed = hallPassed[0] && hallPassed[1] && hallPassed[2] && hallPassed[3];
  }

  public boolean isPassed() {
    return passed;
  }

  public boolean[] getHallPassed() {
    return hallPassed;
  }

  public boolean[] getHighAbu() {
    return highAbu;
  }

  public boolean[] getHighBanu() {
    return highBanu;
  }

  public boolean[] getHighBna() {
    return highBna;
  }

  public String[] getHighAbuMessage() {
    return highAbuMessage;
  }

  public String[] getHighBanuMessage() {
    return highBanuMessage;
  }

  public String[] getHighBnaMessage() {
    return highBnaMessage;
  }
}
