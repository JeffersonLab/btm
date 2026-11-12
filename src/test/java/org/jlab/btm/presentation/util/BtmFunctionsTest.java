package org.jlab.btm.presentation.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.Test;

class BtmFunctionsTest {
  @Test
  void formatsTheHoursBetweenTwoTimes() {
    Date start = Date.from(Instant.parse("2026-01-01T05:00:00Z"));

    assertEquals(
        "1.50",
        BtmFunctions.formatHoursBetween(start, Date.from(Instant.parse("2026-01-01T06:30:00Z"))));
    assertEquals(
        "1,008.01",
        BtmFunctions.formatHoursBetween(start, Date.from(Instant.parse("2026-02-12T05:00:36Z"))));
    assertEquals("", BtmFunctions.formatHoursBetween(null, null));
  }

  @Test
  void measuresATimeSpanWithoutAnEndUntilNow() {
    Date anHourAgo = new Date(System.currentTimeMillis() - 3600_000);

    assertEquals("1.00", BtmFunctions.formatHoursBetween(anHourAgo, null));
  }
}
