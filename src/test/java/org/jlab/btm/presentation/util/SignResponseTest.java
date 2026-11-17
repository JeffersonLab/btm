package org.jlab.btm.presentation.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.ConnectException;
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
    String warning =
        SignResponse.logbookWarning(
            "HDLOG",
            new IOException("Unable to submit", new ConnectException("Connection refused")));

    assertEquals(
        "Signed, but the HDLOG logbook entry could not be made: Connection refused."
            + " Please make the entry by hand.",
        warning);
    assertEquals(
        "<response><span class=\"status\">Success</span><span class=\"warning\">"
            + warning
            + "</span></response>",
        SignResponse.toXml(null, warning));
  }

  @Test
  void namesTheExceptionWhenItHasNoMessage() {
    assertEquals(
        "Signed, but the ELOG logbook entry could not be made: NullPointerException."
            + " Please make the entry by hand.",
        SignResponse.logbookWarning("ELOG", new NullPointerException()));
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
