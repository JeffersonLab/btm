package org.jlab.btm.presentation.util;

import org.jlab.smoothness.business.util.ExceptionUtil;

/** The XML response to a request to sign a timesheet. */
public final class SignResponse {

  private SignResponse() {}

  /**
   * The warning shown when a timesheet is signed but its logbook entry could not be made.
   *
   * @param logbook The logbook the entry was for
   * @param e Why the entry could not be made
   * @return The warning
   */
  public static String logbookWarning(String logbook, Throwable e) {
    Throwable rootCause = e == null ? null : ExceptionUtil.getRootCause(e);
    String reason = null;

    if (rootCause != null) {
      reason = rootCause.getMessage();

      if (reason == null || reason.isBlank()) {
        reason = rootCause.getClass().getSimpleName();
      }
    }

    return "Signed, but the "
        + logbook
        + " logbook entry could not be made"
        + (reason == null ? "" : ": " + reason)
        + ". Please make the entry by hand.";
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
