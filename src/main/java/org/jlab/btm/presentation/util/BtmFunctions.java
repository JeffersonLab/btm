package org.jlab.btm.presentation.util;

import org.jlab.btm.business.util.BtmTimeUtil;
import org.jlab.btm.persistence.entity.Staff;
import org.jlab.btm.persistence.enumeration.DurationUnits;
import org.jlab.btm.persistence.projection.HallPriority;
import org.jlab.smoothness.persistence.enumeration.Hall;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author ryans
 */
public final class BtmFunctions {

    private BtmFunctions() {
        // cannot instantiate publicly
    }

    public static String formatDurationLossy(Integer seconds, DurationUnits units) {
        String value = "";

        if (seconds != null) {
            DecimalFormat hourFormat = new DecimalFormat("#0.##");
            DecimalFormat minuteFormat = new DecimalFormat("##0");

            switch (units) {
                case HOURS:
                    value = hourFormat.format(seconds / 60f / 60f);
                    break;
                case MINUTES:
                    value = minuteFormat.format(seconds / 60f);
                    break;
                default:
                    value = String.valueOf(seconds);
            }
        }

        return value;
    }

    public static String formatDuration(Short seconds, DurationUnits units) {
        String value = "";
        if (seconds != null) {
            value = formatDuration((int) seconds, units);
        }
        return value;
    }

    public static String formatDuration(Integer seconds, DurationUnits units) {
        String value = "";

        if (seconds != null) {
            DecimalFormat format = new DecimalFormat("#0.####");

            switch (units) {
                case HOURS:
                    value = format.format(seconds / 60f / 60f);
                    break;
                case MINUTES:
                    value = format.format(seconds / 60f);
                    break;
                default:
                    value = String.valueOf(seconds);
            }
        }

        return value;
    }

    public static String formatStaff(Staff staff) {
        StringBuilder builder = new StringBuilder();

        if (staff != null) {
            builder.append(staff.getLastname());
            builder.append(", ");
            builder.append(staff.getFirstname());
            builder.append(" (");
            builder.append(staff.getUsername());
            builder.append(")");
        }

        return builder.toString();
    }

    public static String formatBoolean(Boolean value) {
        if (value == null) {
            return "";
        } else if (value) {
            return "Yes"; // true; Y; '✔' 
        } else {
            return "No"; // false; N; ' '
        }
    }

    public static String formatKiloToGiga(Integer kilos) {
        String result = "";

        if (kilos != null) {
            double giga = kilos / 1000000.0;

            DecimalFormat decimalFormat = new DecimalFormat("##.###");

            result = decimalFormat.format(giga);
        }

        return result;
    }

    public static String formatNanoToMicro(Integer nano) {
        String result = "";

        if (nano != null) {
            double micro = nano / 1000.0;

            DecimalFormat decimalFormat = new DecimalFormat("##.###");

            result = decimalFormat.format(micro);
        }

        return result;
    }

    public static List<HallPriority> parsePriorityString(String priorityString) {
        List<HallPriority> priorityList = new ArrayList<>();

        if (priorityString != null && !priorityString.isEmpty()) {
            String[] tokens = priorityString.split(",");

            Integer hallA = (priorityString.indexOf("A") == -1) ? null : priorityString.indexOf("A");
            Integer hallB = (priorityString.indexOf("B") == -1) ? null : priorityString.indexOf("B");
            Integer hallC = (priorityString.indexOf("C") == -1) ? null : priorityString.indexOf("C");
            Integer hallD = (priorityString.indexOf("D") == -1) ? null : priorityString.indexOf("D");

            if (hallA != null) {
                priorityList.add(new HallPriority(Hall.A, hallA));
            }

            if (hallB != null) {
                priorityList.add(new HallPriority(Hall.B, hallB));
            }

            if (hallC != null) {
                priorityList.add(new HallPriority(Hall.C, hallC));
            }

            if (hallD != null) {
                priorityList.add(new HallPriority(Hall.D, hallD));
            }
            Collections.sort(priorityList);

        }

        return priorityList;
    }

    public static String formatPriority(Integer hallA, Integer hallB, Integer hallC, Integer hallD, Integer hallAKeV,
                                        Integer hallBKeV, Integer hallCKeV, Integer hallDKeV) {
        List<HallPriority> priorities = new ArrayList<>();
        if (hallA != null && hallAKeV != null) {
            priorities.add(new HallPriority(Hall.A, hallA));
        }
        if (hallB != null && hallBKeV != null) {
            priorities.add(new HallPriority(Hall.B, hallB));
        }
        if (hallC != null && hallCKeV != null) {
            priorities.add(new HallPriority(Hall.C, hallC));
        }
        if (hallD != null && hallDKeV != null) {
            priorities.add(new HallPriority(Hall.D, hallD));
        }
        Collections.sort(priorities);

        StringBuilder builder = new StringBuilder();

        if(priorities.size() > 0) {
            builder.append(priorities.get(0).getHall());

            for(int i = 1; i < priorities.size(); i++) {
                HallPriority priority  = priorities.get(i);
                builder.append(",");
                builder.append(priority.getHall());
            }
        }

        return builder.toString();
    }

    public static boolean isToday(Date date) {
        return BtmTimeUtil.isToday(date);
    }
}
