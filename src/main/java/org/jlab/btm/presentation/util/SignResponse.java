package org.jlab.btm.presentation.util;

/** The XML response to a request to sign a timesheet. */
public final class SignResponse {

  private SignResponse() {}

  /**
   * The warning shown when a timesheet is signed but its logbook entry could not be made. It names
   * the logbook, not why the entry failed: that goes to the server log, not to the user.
   *
   * @param logbook The logbook the entry was for
   * @return The warning
   */
  public static String logbookWarning(String logbook) {
    return "Signed, but the "
        + logbook
        + " logbook entry could not be made. Please make the entry by hand.";
  }

  /**
   * The response XML.
   *
   * @param errorReason Why the timesheet was not signed, or null if it was
   * @param warning A warning about a signed timesheet, or null for none
   * @return The XML
   */
  public static String toXml(String errorReason, String warning) {
    StringBuilder xml = new StringBuilder("<response>");

    if (errorReason == null) {
      xml.append("<span class=\"status\">Success</span>");

      if (warning != null) {
        xml.append("<span class=\"warning\">").append(escape(warning)).append("</span>");
      }
    } else {
      xml.append("<span class=\"status\">Error</span><span class=\"reason\">")
          .append(escape(errorReason))
          .append("</span>");
    }

    return xml.append("</response>").toString();
  }

  private static String escape(String text) {
    return text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
  }
}
