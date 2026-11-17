package org.jlab.btm.presentation.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SignResponseTest {
  @Test
  void signsWithoutAWarningWhenTheLogbookEntryIsMade() {
    assertEquals(
        "<response><span class=\"status\">Success</span></response>",
        SignResponse.toXml(null, null));
  }

  @Test
  void warnsOfAFailedLogbookEntryOnASignedTimesheet() {
    String warning = SignResponse.logbookWarning("HDLOG");

    assertEquals(
        "Signed, but the HDLOG logbook entry could not be made. Please make the entry by hand.",
        warning);
    assertEquals(
        "<response><span class=\"status\">Success</span><span class=\"warning\">"
            + warning
            + "</span></response>",
        SignResponse.toXml(null, warning));
  }

  @Test
  void escapesTheMessages() {
    assertEquals(
        "<response><span class=\"status\">Success</span><span class=\"warning\">"
            + "a &lt;b&gt; &amp; &quot;c&quot;</span></response>",
        SignResponse.toXml(null, "a <b> & \"c\""));
    assertEquals(
        "<response><span class=\"status\">Error</span><span class=\"reason\">"
            + "Access Denied</span></response>",
        SignResponse.toXml("Access Denied", "ignored"));
  }
}
