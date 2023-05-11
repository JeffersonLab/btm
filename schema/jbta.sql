

-- CREATE VIEWS




CREATE VIEW EXP_HALL_SHIFT_BOUNDS AS
SELECT EXP_HALL_SHIFT_ID,
       HALL,
       START_DAY_AND_HOUR,
       CAST(TRUNC(START_DAY_AND_HOUR) + DECODE(EXTRACT(HOUR FROM START_DAY_AND_HOUR), 0, 7, 8, 15, 16, 23, NULL) /
                                        24 AS TIMESTAMP) END_DAY_AND_HOUR,
       PURPOSE_ID,
       LEADER,
       WORKERS,
       REMARK
FROM EXP_HALL_SHIFT;

CREATE VIEW EXPERIMENT AS
SELECT EXP_HALL_SHIFT_PURPOSE_ID AS EXPERIMENT_ID, HALL, NAME, ACTIVE
FROM EXP_HALL_SHIFT_PURPOSE
WHERE EXPERIMENT = 1;

CREATE VIEW EXP_HALL_SHIFT_TIME AS
SELECT HALL,
       TRUNC(DAY_AND_HOUR)                                                                                 AS DAY,
       DECODE(TRUNC(EXTRACT(HOUR FROM DAY_AND_HOUR) / 8), '0', 'OWL', '1', 'DAY', '2', 'SWING', 'UNKNOWN') AS SHIFT,
       MIN(DAY_AND_HOUR)                                                                                   AS START_HOUR,
       SUM(ABU_SECONDS)                                                                                    AS ABU_SECONDS,
       SUM(BANU_SECONDS)                                                                                   AS BANU_SECONDS,
       SUM(BNA_SECONDS)                                                                                    AS BNA_SECONDS,
       SUM(ACC_SECONDS)                                                                                    AS ACC_SECONDS,
       SUM(ER_SECONDS)                                                                                     AS ER_SECONDS,
       SUM(PCC_SECONDS)                                                                                    AS PCC_SECONDS,
       SUM(UED_SECONDS)                                                                                    AS UED_SECONDS,
       SUM(SCHED_SECONDS)                                                                                  AS SCHED_SECONDS,
       SUM(STUDIES_SECONDS)                                                                                AS STUDIES_SECONDS,
       SUM(OFF_SECONDS)                                                                                    AS OFF_SECONDS
FROM EXP_HALL_HOUR
GROUP BY HALL, TRUNC(DAY_AND_HOUR),
         DECODE(TRUNC(EXTRACT(HOUR FROM DAY_AND_HOUR) / 8), '0', 'OWL', '1', 'DAY', '2', 'SWING', 'UNKNOWN');

CREATE OR REPLACE VIEW BTM_OWNER.EXP_HALL_SHIFT_TIME_LEGACY AS
    SELECT HALL,
           DAY,
           SHIFT,
           START_HOUR,
           ABU_SECONDS,
           BANU_SECONDS,
           BNA_SECONDS,
           ACC_SECONDS,
           ER_SECONDS,
           PCC_SECONDS,
           UED_SECONDS,
           SCHED_SECONDS,
           STUDIES_SECONDS,
           OFF_SECONDS
    FROM BTM_OWNER.EXP_HALL_SHIFT_TIME
    UNION ALL
    SELECT HALL,
           TRUNC(START_TIME)                                              as DAY,
           DECODE(SHIFT, 'O', 'OWL', 'D', 'DAY', 'S', 'SWING', 'UNKNOWN') AS SHIFT,
           CAST(START_TIME AS TIMESTAMP)                                  AS START_HOUR,
           TRUNC(ABU * 3600)                                              AS ABU_SECONDS,
           TRUNC(BANU * 3600)                                             AS BANU_SECONDS,
           TRUNC(BNA * 3600)                                              AS BNA_SECONDS,
           TRUNC(ACC * 3600)                                              AS ACC_SECONDS,
           TRUNC(ER * 3600)                                               AS ER_SECONDS,
           TRUNC(PCC * 3600)                                              AS PCC_SECONDS,
           TRUNC(UED * 3600)                                              AS UED_SECONDS,
           NULL                                                           AS SCHED_SECONDS,
           NULL                                                           AS STUDIES_SECONDS,
           ((GET_HOURS_IN_SHIFT(START_TIME) - (ER + PCC + UED)) * 3600)   AS OFF_SECONDS
    FROM BTA_OWNER.HALL_AVAILABILITY a
             INNER JOIN BTA_OWNER.AVAILABILITY b on a.AVAILABILITY_ID = b.AVAILABILITY_ID WITH READ ONLY;

CREATE OR REPLACE VIEW BTM_OWNER.AVAILABILITY AS
    SELECT HALL,
           START_TIME,
           SHIFT,
           ABU,
           BANU,
           BNA,
           ACC,
           ER,
           PCC,
           UED,
           NULL AS BEAM_OFF,
           1    AS SIGNED
    FROM BTA_OWNER.HALL_AVAILABILITY a
             INNER JOIN BTA_OWNER.AVAILABILITY b on a.AVAILABILITY_ID = b.AVAILABILITY_ID
    UNION ALL
    SELECT x.HALL,
           x.START_HOUR                                                   AS START_TIME,
           DECODE(SHIFT, 'OWL', 'O', 'DAY', 'D', 'SWING', 'S', 'UNKNOWN') AS SHIFT,
           (ABU_SECONDS / 3600)                                           AS ABU,
           (BANU_SECONDS / 3600)                                          AS BANU,
           (BNA_SECONDS / 3600)                                           AS BNA,
           (ACC_SECONDS / 3600)                                           AS ACC,
           (ER_SECONDS / 3600)                                            AS ER,
           (PCC_SECONDS / 3600)                                           AS PCC,
           (UED_SECONDS / 3600)                                           AS UED,
           (OFF_SECONDS / 3600)                                           AS BEAM_OFF,
           (SELECT COUNT(*)
            FROM EXP_HALL_SIGNATURE d
            WHERE d.SIGNED_ROLE = 'OPERABILITY_MANAGER'
              AND d.HALL = x.HALL
              AND d.START_DAY_AND_HOUR = x.START_HOUR)                    AS SIGNED
    FROM BTM_OWNER.EXP_HALL_SHIFT_TIME x
    WITH READ ONLY;


-- CREATE FUNCTIONS

CREATE OR REPLACE FUNCTION GET_2007_DAYLIGHT_START(start_year IN DATE) RETURN DATE IS
    v_Date      Date;
    v_LoopIndex Integer;
Begin
    --Set the date to the 8th day of March which will effectively skip the first Sunday.
    v_Date := to_date('03/08/' || to_char(start_year, 'YYYY') || '02:00:00 AM', 'MM/DD/YYYY HH:MI:SS PM');
    --Advance to the second Sunday.
    FOR v_LoopIndex IN 0..6
        LOOP
            If (RTRIM(to_char(v_Date + v_LoopIndex, 'DAY')) = 'SUNDAY') Then
                Return v_Date + v_LoopIndex;
            End If;
        END LOOP;

END;

CREATE OR REPLACE FUNCTION GET_1987_DAYLIGHT_START(start_year IN DATE) RETURN DATE IS
    v_Date      Date;
    v_LoopIndex Integer;
Begin
    --Set the date to the 8th day of March which will effectively skip the first Sunday.
    v_Date := to_date('04/01/' || to_char(start_year, 'YYYY') || '02:00:00 AM', 'MM/DD/YYYY HH:MI:SS PM');
    --Advance to the first Sunday.
    FOR v_LoopIndex IN 0..6
        LOOP
            If (RTRIM(to_char(v_Date + v_LoopIndex, 'DAY')) = 'SUNDAY') Then
                Return v_Date + v_LoopIndex;
            End If;
        END LOOP;
END;

CREATE OR REPLACE FUNCTION GET_2007_DAYLIGHT_END(end_year IN DATE) RETURN DATE IS
    v_Date      Date;
    v_LoopIndex Integer;
Begin
    --Set Date to the first of November this year
    v_Date := to_date('11/01/' || to_char(end_year, 'YYYY') || '02:00:00 AM', 'MM/DD/YYYY HH:MI:SS PM');
    --Advance to the first Sunday
    FOR v_LoopIndex IN 0..6
        LOOP
            If (RTRIM(to_char(v_Date + v_LoopIndex, 'DAY')) = 'SUNDAY') Then
                Return v_Date + v_LoopIndex;
            End If;
        END LOOP;
End;

CREATE OR REPLACE FUNCTION GET_1987_DAYLIGHT_END(end_year IN DATE) RETURN DATE IS
    v_Date      Date;
    v_LoopIndex Integer;
Begin
    --Set Date to the last day of October this year
    v_Date := last_day(to_date('10/01/' || to_char(end_year, 'YYYY') || '02:00:00 AM', 'MM/DD/YYYY HH:MI:SS PM'));
    --Advance to the first Sunday
    FOR v_LoopIndex IN 0..6
        LOOP
            If (RTRIM(to_char(v_Date - v_LoopIndex, 'DAY')) = 'SUNDAY') Then
                Return v_Date - v_LoopIndex;
            End If;
        END LOOP;
End;

CREATE OR REPLACE FUNCTION GET_DAYLIGHT_SAVINGS_START(start_year IN TIMESTAMP) RETURN DATE IS
    vyear INTEGER;
BEGIN
    vyear := EXTRACT(YEAR FROM start_year);

    IF vyear < 2007 THEN
        RETURN GET_1987_DAYLIGHT_START(start_year);
    ELSE
        RETURN GET_2007_DAYLIGHT_START(start_year);
    END IF;
END;

CREATE OR REPLACE FUNCTION GET_DAYLIGHT_SAVINGS_END(end_year IN TIMESTAMP) RETURN DATE IS
    vyear INTEGER;
BEGIN
    vyear := EXTRACT(YEAR FROM end_year);

    IF vyear < 2007 THEN
        RETURN GET_1987_DAYLIGHT_END(end_year);
    ELSE
        RETURN GET_2007_DAYLIGHT_END(end_year);
    END IF;
END;

CREATE OR REPLACE FUNCTION IS_DAYLIGHT_SAVINGS_START(start_day IN DATE) RETURN INTEGER IS
    result INTEGER;
BEGIN
    result := 0;

    IF TRUNC(GET_DAYLIGHT_SAVINGS_START(start_day)) = TRUNC(start_day) THEN
        result := 1;
    END IF;

    RETURN result;
END IS_DAYLIGHT_SAVINGS_START;

CREATE OR REPLACE FUNCTION IS_DAYLIGHT_SAVINGS_END(end_day IN DATE) RETURN INTEGER IS
    result INTEGER;
BEGIN
    result := 0;

    IF TRUNC(GET_DAYLIGHT_SAVINGS_END(end_day)) = TRUNC(end_day) THEN
        result := 1;
    END IF;

    RETURN result;
END IS_DAYLIGHT_SAVINGS_END;

CREATE OR REPLACE FUNCTION GET_HOURS_IN_SHIFT(start_hour IN TIMESTAMP) RETURN INTEGER IS
    result INTEGER;
    vhour  INTEGER;
BEGIN
    result := 8;
    vhour := EXTRACT(HOUR FROM start_hour);

    IF vhour = 0 THEN
        IF IS_DAYLIGHT_SAVINGS_START(start_hour) = 1 THEN
            result := 7;
        ELSIF IS_DAYLIGHT_SAVINGS_END(start_hour) = 1 THEN
            result := 9;
        END IF;
    END IF;

    RETURN result;
END GET_HOURS_IN_SHIFT;

-- TRIGGER TO ENSURE SCHEDULE_DAY WITHIN MONTH OF SCHEDULE
CREATE OR REPLACE TRIGGER DAY_IS_IN_MONTH
    AFTER INSERT OR UPDATE
    ON SCHEDULE_DAY
    FOR EACH ROW
DECLARE
    schedule_month varchar2(3);
BEGIN
    SELECT to_char(start_day, 'MON')
    INTO schedule_month
    FROM monthly_schedule
    WHERE monthly_schedule_id = :NEW.monthly_schedule_id;
    IF (to_char(:NEW.DAY_MONTH_YEAR, 'MON') != schedule_month) THEN
        raise_application_error(-20020, 'day is not within schedule month');
    END IF;
END;
/
