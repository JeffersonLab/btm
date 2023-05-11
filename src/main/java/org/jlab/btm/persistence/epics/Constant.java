package org.jlab.btm.persistence.epics;

/**
 * All of the constant values needed by EPICS BTM.
 *
 * @author ryans
 */
public class Constant {
    public static final int NUMBER_OF_HOURS_IN_HISTORY = 168;
    public static final String SHIFT_CHANNEL_NAME = "comm1";
    public static final String CREW_CHIEF_CHANNEL_NAME = "comm2";
    public static final String OPERATORS_CHANNEL_NAME = "comm3";
    public static final String PROGRAM_CHANNEL_NAME = "comm6";
    public static final String PROGRAM_DEPUTY_CHANNEL_NAME = "comm5";
    public static final String COMMENTS_CHANNEL_NAME = "comm7";
    public static final String TIME_CHANNEL_NAME = "BOOMUNIXTIME_h";
    public static final String ACC_UP_CHANNEL_NAME = "BOOMCEBANULL_h";
    public static final String ACC_SAD_CHANNEL_NAME = "BOOMCEBAOFF_h";
    public static final String ACC_DOWN_CHANNEL_NAME = "BOOMCEBADOWN_h";
    public static final String ACC_STUDIES_CHANNEL_NAME = "BOOMCEBAMD_h";
    public static final String ACC_RESTORE_CHANNEL_NAME = "BOOMCEBARESTORE_h";
    public static final String ACC_ACC_CHANNEL_NAME = "BOOMCEBAACC_h";
    public static final String HALL_PREFIX = "BOOMHL";
    public static final String HALL_UP_SUFFIX = "UP_h";
    public static final String HALL_TUNE_SUFFIX = "TUNE_h";
    public static final String HALL_BNR_SUFFIX = "BANU_h";
    public static final String HALL_DOWN_SUFFIX = "DOWN_h";
    public static final String HALL_OFF_SUFFIX = "OFF_h";
    public static final String MULTI_ONE_UP = "BOOMONLY1HALLUP_h";
    public static final String MULTI_TWO_UP = "BOOMONLY2HALLUP_h";
    public static final String MULTI_THREE_UP = "BOOMONLY3HALLUP_h";
    public static final String MULTI_FOUR_UP = "BOOMALLHALLUP_h";
    public static final String MULTI_ANY_UP = "BOOMANYHALLUP_h";
    public static final String MULTI_ALL_UP = "BOOMALLSCHHALLUP_h";
    public static final String MULTI_DOWN = "BOOMDOWNHARD_h";
}
