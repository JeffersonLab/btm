package org.jlab.btm.business.service.epics;

import java.util.Date;
import org.epics.util.array.ListNumber;
import org.epics.vtype.AlarmProvider;
import org.epics.vtype.AlarmSeverity;
import org.epics.vtype.VEnum;
import org.epics.vtype.VNumber;
import org.epics.vtype.VNumberArray;
import org.epics.vtype.VString;
import org.epics.vtype.VStringArray;
import org.epics.vtype.VType;

/**
 * An immutable record of a PV's latest value and whether its channel is connected, with a local
 * timestamp of when the value arrived.
 */
public final class PVCacheEntry {
  /** The latest value; never null. */
  private final VType value;

  /** Whether the channel was connected when this entry was made. */
  private final boolean connected;

  /**
   * This is the local timestamp of when the value was received. This differs from the timestamp
   * provided by IOCs (which is also interesting, but often unreliable). We use the legacy Date
   * object because JSTL format expects a Date.
   */
  private final Date ts;

  /**
   * Create a new PVCacheEntry.
   *
   * @param value The value
   * @param connected Whether the channel is connected
   * @param ts The local time the value was received
   */
  public PVCacheEntry(VType value, boolean connected, Date ts) {
    this.value = value;
    this.connected = connected;
    this.ts = ts;
  }

  /**
   * Get the value.
   *
   * @return The value
   */
  public VType getValue() {
    return value;
  }

  /**
   * @return Whether the channel was connected when this entry was made
   */
  public boolean isConnected() {
    return connected;
  }

  /**
   * Get the timestamp.
   *
   * @return The timestamp
   */
  public Date getTs() {
    return ts;
  }

  /**
   * @return "Connected" or "Disconnected", with the value's alarm severity if it has one
   */
  public String getStatus() {
    String status = connected ? "Connected" : "Disconnected";

    if (connected
        && value instanceof AlarmProvider provider
        && provider.getAlarm().getSeverity() != AlarmSeverity.NONE) {
      status = status + " (" + provider.getAlarm().getSeverity() + " alarm)";
    }

    return status;
  }

  /**
   * @return The value as doubles: every element of a numeric array, or the one numeric scalar
   * @throws IllegalStateException If the value isn't numeric
   */
  public double[] getDoubleArray() {
    if (value instanceof VNumberArray array) {
      ListNumber data = array.getData();
      double[] doubles = new double[data.size()];

      for (int i = 0; i < doubles.length; i++) {
        doubles[i] = data.getDouble(i);
      }

      return doubles;
    }

    if (value instanceof VNumber number) {
      return new double[] {number.getValue().doubleValue()};
    }

    throw new IllegalStateException("Not a numeric value: " + value.getClass().getSimpleName());
  }

  /**
   * @return The value as a string: a string scalar, the first element of a string array, or an
   *     enumeration's label
   * @throws IllegalStateException If the value isn't a string or enumeration
   */
  public String getString() {
    if (value instanceof VString string) {
      return string.getValue();
    }

    if (value instanceof VStringArray array && !array.getData().isEmpty()) {
      return array.getData().get(0);
    }

    if (value instanceof VEnum enumeration) {
      return enumeration.getValue();
    }

    throw new IllegalStateException("Not a string value: " + value.getClass().getSimpleName());
  }
}
