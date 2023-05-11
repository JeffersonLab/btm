package org.jlab.btm.persistence.projection;

import java.util.Calendar;
import java.util.Date;

public class DailyCharge {
        public Date d;
        public long nC; /*Nano Coulombs*/
        public long nA; /*Nano Amps*/
        public String program; /* Usually an experiment */

        public String toString() {
            return d.toString() + "; " + nC;
        }

        public Date getD() {
        return d;
        }

        public long getnA() {
            return nA;
        }

        public long getnC() {
            return nC;
        }

        public double getCoulombs() {
            return nC * 0.000000001;
        }

        public long getTimestamp() {
            return getLocalTime(d);
            //return d.getTime();
        }

        public String getProgram() {
            return program;
        }

    /**
     * Returns the number of milliseconds since Jan 01 1970, but in local time, not UTC like usual.
     * This is useful because web browsers / JavaScript generally can't figure out daylight savings
     * or timezone offsets for varying points in time (they generally only know the fixed/constant
     * offset being applied on the client at present).
     *
     * @param date The date (milliseconds since Epoch in UTC)
     * @return milliseconds since Epoch in local time
     */
    private long getLocalTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        long localOffset = cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET);

        return cal.getTimeInMillis() + localOffset;
    }
}
