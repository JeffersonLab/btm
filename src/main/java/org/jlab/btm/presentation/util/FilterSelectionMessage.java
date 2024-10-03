package org.jlab.btm.presentation.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author ryans
 */
public final class FilterSelectionMessage {

  private FilterSelectionMessage() {
    // Private constructor
  }

  public static String getHallAvailabilityReportCaption(Date start, Date end, double period) {
    // DecimalFormat numberFormatter = new DecimalFormat("###,###");
    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm");

    String message =
        "Hall Availability from "
            + dateFormatter.format(start)
            + " to "
            + dateFormatter.format(end);

    return message;
  }

  public static String getPhysicsReportCaption(String chart) {

    String chartTitle;

    switch (chart) {
      case "a":
        chartTitle = "Hall A Beam ";
        break;
      case "b":
        chartTitle = "Hall B Beam ";
        break;
      case "c":
        chartTitle = "Hall C Beam ";
        break;
      case "d":
        chartTitle = "Hall D Beam ";
        break;
      case "table":
        chartTitle = "Data Table ";
        break;
      default:
        chartTitle = "Available Beam ";
        break;
    }

    String message = chartTitle;

    return message;
  }

  public static String getSummaryReportCaption(Date start, Date end, double period) {
    // DecimalFormat numberFormatter = new DecimalFormat("###,###");
    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm");

    String message =
        "Beam Time Summary from "
            + dateFormatter.format(start)
            + " to "
            + dateFormatter.format(end);

    return message;
  }

  public static String getMessage(String lastname) {

    List<String> filters = new ArrayList<String>();

    if (lastname != null && !lastname.isEmpty()) {
      filters.add("Lastname \"" + lastname + "\"");
    }

    String message = "";

    if (!filters.isEmpty()) {
      for (String filter : filters) {
        message += " " + filter + " and";
      }

      // Remove trailing " and"
      message = message.substring(0, message.length() - 4);
    }

    return message;
  }
}
