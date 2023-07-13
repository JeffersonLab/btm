alter session set container = XEPDB1;

-- Monthly Schedule
INSERT INTO BTM_OWNER.MONTHLY_SCHEDULE (MONTHLY_SCHEDULE_ID, START_DAY, VERSION, PUBLISHED_DATE) VALUES (1, DATE '2023-01-01', 1, DATE '2023-02-01');

DROP SEQUENCE BTM_OWNER.SCHEDULE_ID;
CREATE SEQUENCE BTM_OWNER.SCHEDULE_ID
    INCREMENT BY 1
    START WITH 2
    NOCYCLE
    NOCACHE
    ORDER;

-- Schedule Day
INSERT INTO BTM_OWNER.SCHEDULE_DAY (SCHEDULE_DAY_ID, DAY_MONTH_YEAR, MONTHLY_SCHEDULE_ID, ACC_PROGRAM, HALL_A_PROGRAM_ID, HALL_B_PROGRAM_ID, HALL_C_PROGRAM_ID, HALL_D_PROGRAM_ID) VALUES (1, DATE '2023-01-01', 1, 'PHYSICS', 2, 7, 12, 17);
INSERT INTO BTM_OWNER.SCHEDULE_DAY (SCHEDULE_DAY_ID, DAY_MONTH_YEAR, MONTHLY_SCHEDULE_ID, ACC_PROGRAM, HALL_A_PROGRAM_ID, HALL_B_PROGRAM_ID, HALL_C_PROGRAM_ID, HALL_D_PROGRAM_ID) VALUES (2, DATE '2023-01-02', 1, 'PHYSICS', 2, 7, 12, 17);

DROP SEQUENCE BTM_OWNER.SCHEDULE_DAY_ID;
CREATE SEQUENCE BTM_OWNER.SCHEDULE_DAY_ID
    INCREMENT BY 1
    START WITH 3
    NOCYCLE
    NOCACHE
    ORDER;

-- Run
INSERT INTO BTM_OWNER.RUN (RUN_ID, START_DAY_MONTH_YEAR, END_DAY_MONTH_YEAR) VALUES (1, DATE '2023-01-01', DATE '2023-02-03');

DROP SEQUENCE BTM_OWNER.RUN_ID;
CREATE SEQUENCE BTM_OWNER.RUN_ID
    INCREMENT BY 1
    START WITH 2
    NOCYCLE
    NOCACHE
    ORDER;