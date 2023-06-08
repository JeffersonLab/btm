alter session set container = XEPDB1;

-- Sequences

CREATE SEQUENCE BTM_OWNER.EXP_EMAIL_RECIPIENT_ID
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;

CREATE SEQUENCE BTM_OWNER.EXP_HOUR_ID
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;

CREATE SEQUENCE BTM_OWNER.EXP_HOUR_REASON_TIME_ID
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;

CREATE SEQUENCE BTM_OWNER.EXP_REASON_ID
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;

CREATE SEQUENCE BTM_OWNER.EXP_SHIFT_ID
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;

CREATE SEQUENCE BTM_OWNER.EXP_SHIFT_PURPOSE_ID
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;

CREATE SEQUENCE BTM_OWNER.EXP_SIGNATURE_ID
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;

CREATE SEQUENCE BTM_OWNER.HIBERNATE_SEQUENCE
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;

CREATE SEQUENCE BTM_OWNER.CC_ACC_HOUR_ID
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;

CREATE SEQUENCE BTM_OWNER.CC_CROSS_CHECK_COMMENT_ID
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;

CREATE SEQUENCE BTM_OWNER.CC_HALL_HOUR_ID
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;

CREATE SEQUENCE BTM_OWNER.CC_MULTIPLICITY_HOUR_ID
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;

CREATE SEQUENCE BTM_OWNER.CC_SHIFT_ID
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;

CREATE SEQUENCE BTM_OWNER.CC_SIGNATURE_ID
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;

CREATE SEQUENCE BTM_OWNER.SCHEDULE_DAY_ID
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;

CREATE SEQUENCE BTM_OWNER.SCHEDULE_ID
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;

-- Tables

CREATE TABLE BTM_OWNER.MONTHLY_SCHEDULE
(
    MONTHLY_SCHEDULE_ID INTEGER      NOT NULL,
    START_DAY           TIMESTAMP(0) NOT NULL,
    VERSION             INTEGER      NOT NULL,
    PUBLISHED_DATE      DATE         NULL,
    CONSTRAINT MONTHLY_SCHEDULE_PK PRIMARY KEY (MONTHLY_SCHEDULE_ID),
    CONSTRAINT MONTHLY_SCHEDULE_AK1 UNIQUE (START_DAY, VERSION),
    CONSTRAINT MONTHLY_SCHEDULE_CK1 CHECK (EXTRACT(DAY FROM START_DAY) = 1 and EXTRACT(HOUR FROM START_DAY) = 0 and
                                           EXTRACT(MINUTE FROM START_DAY) = 0 AND
                                           EXTRACT(SECOND FROM START_DAY) = 0)
);

CREATE TABLE BTM_OWNER.SCHEDULE_DAY
(
    SCHEDULE_DAY_ID     INTEGER                NOT NULL,
    DAY_MONTH_YEAR      DATE                   NOT NULL,
    MONTHLY_SCHEDULE_ID INTEGER                NOT NULL,
    ACC_PROGRAM         VARCHAR2(24 CHAR)      NOT NULL,
    KILO_VOLTS_PER_PASS INTEGER                NULL,
    NOTE                VARCHAR2(256 CHAR)     NULL,
    HALL_A_PROGRAM_ID   INTEGER                NOT NULL,
    HALL_A_NANO_AMPS    INTEGER                NULL,
    HALL_A_KILO_VOLTS   INTEGER                NULL,
    HALL_A_PASSES       INTEGER                NULL,
    HALL_A_PRIORITY     INTEGER                NULL,
    HALL_A_POLARIZED    CHAR(1 BYTE) DEFAULT 1 NOT NULL,
    HALL_B_PROGRAM_ID   INTEGER                NOT NULL,
    HALL_B_NANO_AMPS    INTEGER                NULL,
    HALL_B_KILO_VOLTS   INTEGER                NULL,
    HALL_B_PASSES       INTEGER                NULL,
    HALL_B_PRIORITY     INTEGER                NULL,
    HALL_B_POLARIZED    CHAR(1 BYTE) DEFAULT 1 NOT NULL,
    HALL_C_PROGRAM_ID   INTEGER                NULL,
    HALL_C_NANO_AMPS    INTEGER                NULL,
    HALL_C_KILO_VOLTS   INTEGER                NULL,
    HALL_C_PASSES       INTEGER                NULL,
    HALL_C_PRIORITY     INTEGER                NULL,
    HALL_C_POLARIZED    CHAR(1 BYTE) DEFAULT 1 NOT NULL,
    HALL_D_PROGRAM_ID   INTEGER                NULL,
    HALL_D_NANO_AMPS    INTEGER                NULL,
    HALL_D_KILO_VOLTS   INTEGER                NULL,
    HALL_D_PASSES       INTEGER                NULL,
    HALL_D_PRIORITY     INTEGER                NULL,
    HALL_D_POLARIZED    CHAR(1 BYTE) DEFAULT 1 NOT NULL,
    HALL_A_NOTE         VARCHAR2(256 CHAR)     NULL,
    HALL_B_NOTE         VARCHAR2(256 CHAR)     NULL,
    HALL_C_NOTE         VARCHAR2(256 CHAR)     NULL,
    HALL_D_NOTE         VARCHAR2(256 CHAR)     NULL,
    MIN_HALL_COUNT      INTEGER                NULL,
    CONSTRAINT SCHEDULE_DAY_PK PRIMARY KEY (SCHEDULE_DAY_ID),
    CONSTRAINT SCHEDULE_DAY_AK1 UNIQUE (DAY_MONTH_YEAR, MONTHLY_SCHEDULE_ID),
    CONSTRAINT SCHEDULE_DAY_FK1 FOREIGN KEY (MONTHLY_SCHEDULE_ID) REFERENCES BTM_OWNER.MONTHLY_SCHEDULE (MONTHLY_SCHEDULE_ID) ON DELETE CASCADE,
    CONSTRAINT SCHEDULE_DAY_CK1 CHECK (DAY_MONTH_YEAR = TRUNC(DAY_MONTH_YEAR)),
    CONSTRAINT SCHEDULE_DAY_CK2 CHECK (ACC_PROGRAM IN
                                       ('PHYSICS', 'STUDIES', 'RESTORE', 'ACC', 'DOWN', 'OFF', 'TBD', 'FACDEV')),
    CONSTRAINT SCHEDULE_DAY_CK3 CHECK (HALL_A_POLARIZED IN (0, 1)),
    CONSTRAINT SCHEDULE_DAY_CK4 CHECK (HALL_B_POLARIZED IN (0, 1)),
    CONSTRAINT SCHEDULE_DAY_CK5 CHECK (HALL_C_POLARIZED IN (0, 1)),
    CONSTRAINT SCHEDULE_DAY_CK6 CHECK (HALL_D_POLARIZED IN (0, 1))
);

CREATE TABLE BTM_OWNER.CC_SIGNATURE
(
    CC_SIGNATURE_ID    INTEGER                           NOT NULL,
    START_DAY_AND_HOUR TIMESTAMP(0) WITH LOCAL TIME ZONE NOT NULL,
    SIGNED_BY          VARCHAR2(64 CHAR)                 NOT NULL,
    SIGNED_ROLE        VARCHAR2(20 CHAR)                 NOT NULL,
    SIGNED_DATE        DATE                              NOT NULL,
    CONSTRAINT CC_SIGNATURE_PK PRIMARY KEY (CC_SIGNATURE_ID),
    CONSTRAINT CC_SIGNATURE_AK1 UNIQUE (START_DAY_AND_HOUR, SIGNED_BY, SIGNED_ROLE),
    CONSTRAINT CC_SIGNATURE_CK1 CHECK (EXTRACT(HOUR FROM START_DAY_AND_HOUR) in (23, 7, 15) AND
                                       EXTRACT(MINUTE FROM START_DAY_AND_HOUR) = 0 AND
                                       EXTRACT(SECOND FROM START_DAY_AND_HOUR) = 0),
    CONSTRAINT CC_SIGNATURE_CK2 CHECK (SIGNED_ROLE IN ('CREW_CHIEF', 'OPERABILITY_MANAGER'))
);

CREATE TABLE BTM_OWNER.CC_SHIFT
(
    CC_SHIFT_ID        NUMBER(38, 0)                     NOT NULL,
    START_DAY_AND_HOUR TIMESTAMP(0) WITH LOCAL TIME ZONE NOT NULL,
    CREW_CHIEF         VARCHAR2(64 CHAR)                 NULL,
    OPERATORS          VARCHAR2(256 CHAR)                NULL,
    REMARK             VARCHAR2(2048 CHAR)               NULL,
    PROGRAM            VARCHAR2(64 CHAR)                 NULL,
    PROGRAM_DEPUTY     VARCHAR2(64 CHAR)                 NULL,
    CONSTRAINT CC_SHIFT_PK PRIMARY KEY (CC_SHIFT_ID),
    CONSTRAINT CC_SHIFT_AK1 UNIQUE (START_DAY_AND_HOUR),
    CONSTRAINT CC_SHIFT_CK1 CHECK (EXTRACT(HOUR FROM START_DAY_AND_HOUR) in (23, 7, 15) AND
                                   EXTRACT(MINUTE FROM START_DAY_AND_HOUR) = 0 AND
                                   EXTRACT(SECOND FROM START_DAY_AND_HOUR) = 0)
);

CREATE TABLE BTM_OWNER.CC_MULTIPLICITY_HOUR
(
    CC_MULTIPLICITY_HOUR_ID NUMBER(38, 0)                     NOT NULL,
    DAY_AND_HOUR            TIMESTAMP(0) WITH LOCAL TIME ZONE NOT NULL,
    ONE_HALL_UP_SECONDS     NUMBER(4, 0) DEFAULT 0            NOT NULL,
    TWO_HALL_UP_SECONDS     NUMBER(4, 0) DEFAULT 0            NOT NULL,
    THREE_HALL_UP_SECONDS   NUMBER(4, 0) DEFAULT 0            NOT NULL,
    ANY_HALL_UP_SECONDS     NUMBER(4, 0) DEFAULT 0            NOT NULL,
    ALL_HALL_UP_SECONDS     NUMBER(4, 0) DEFAULT 0            NOT NULL,
    DOWN_HARD_SECONDS       NUMBER(4, 0) DEFAULT 0            NOT NULL,
    FOUR_HALL_UP_SECONDS    NUMBER(4, 0) DEFAULT 0            NOT NULL,
    CONSTRAINT CC_MULTIPLICITY_HOUR_PK PRIMARY KEY (CC_MULTIPLICITY_HOUR_ID),
    CONSTRAINT CC_MULTIPLICITY_HOUR_AK1 UNIQUE (DAY_AND_HOUR),
    CONSTRAINT CC_MULTIPLICITY_CK1 CHECK (EXTRACT(MINUTE FROM DAY_AND_HOUR) = 0 AND
                                          EXTRACT(SECOND FROM DAY_AND_HOUR) = 0),
    CONSTRAINT CC_MULTIPLICITY_CK2 CHECK (ONE_HALL_UP_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT CC_MULTIPLICITY_CK3 CHECK (TWO_HALL_UP_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT CC_MULTIPLICITY_CK4 CHECK (THREE_HALL_UP_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT CC_MULTIPLICITY_CK5 CHECK (ANY_HALL_UP_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT CC_MULTIPLICITY_CK6 CHECK (ALL_HALL_UP_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT CC_MULTIPLICITY_CK7 CHECK (DOWN_HARD_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT CC_MULTIPLICITY_CK8 CHECK (FOUR_HALL_UP_SECONDS BETWEEN 0 AND 3600)
);

CREATE TABLE BTM_OWNER.CC_HALL_HOUR
(
    CC_HALL_HOUR_ID NUMBER(38, 0)                     NOT NULL,
    DAY_AND_HOUR    TIMESTAMP(0) WITH LOCAL TIME ZONE NOT NULL,
    HALL            CHAR(1 CHAR)                      NOT NULL,
    UP_SECONDS      NUMBER(4, 0) DEFAULT 0            NOT NULL,
    TUNE_SECONDS    NUMBER(4, 0) DEFAULT 0            NOT NULL,
    BNR_SECONDS     NUMBER(4, 0) DEFAULT 0            NOT NULL,
    DOWN_SECONDS    NUMBER(4, 0) DEFAULT 0            NOT NULL,
    OFF_SECONDS     NUMBER(4, 0) DEFAULT 0            NOT NULL,
    CONSTRAINT CC_HALL_HOUR_PK PRIMARY KEY (CC_HALL_HOUR_ID),
    CONSTRAINT CC_HALL_HOUR_AK1 UNIQUE (HALL, DAY_AND_HOUR),
    CONSTRAINT CC_HALL_HOUR_CK8 CHECK ( UP_SECONDS + TUNE_SECONDS + BNR_SECONDS + DOWN_SECONDS + OFF_SECONDS = 3600 ),
    CONSTRAINT CC_HALL_HOUR_CK1 CHECK (EXTRACT(MINUTE FROM DAY_AND_HOUR) = 0 AND
                                       EXTRACT(SECOND FROM DAY_AND_HOUR) = 0),
    CONSTRAINT CC_HALL_HOUR_CK2 CHECK (HALL IN ('A', 'B', 'C', 'D')),
    CONSTRAINT CC_HALL_HOUR_CK3 CHECK (UP_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT CC_HALL_HOUR_CK4 CHECK (TUNE_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT CC_HALL_HOUR_CK5 CHECK (BNR_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT CC_HALL_HOUR_CK6 CHECK (DOWN_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT CC_HALL_HOUR_CK7 CHECK (OFF_SECONDS BETWEEN 0 AND 3600)
);

CREATE TABLE BTM_OWNER.CC_CROSS_CHECK_COMMENT
(
    CC_CHECK_COMMENT_ID INTEGER                           NOT NULL,
    START_DAY_AND_HOUR  TIMESTAMP(0) WITH LOCAL TIME ZONE NOT NULL,
    REMARK              VARCHAR2(2048 CHAR)               NULL,
    REVIEWER_REMARK     VARCHAR2(2048 CHAR)               NULL,
    CONSTRAINT CC_CROSS_CHECK_COMMENT_PK PRIMARY KEY (CC_CHECK_COMMENT_ID),
    CONSTRAINT CC_CROSS_CHECK_COMMENT_AK1 UNIQUE (START_DAY_AND_HOUR)
);

CREATE TABLE BTM_OWNER.CC_ACC_HOUR
(
    CC_ACC_HOUR_ID  INTEGER                           NOT NULL,
    DAY_AND_HOUR    TIMESTAMP(0) WITH LOCAL TIME ZONE NOT NULL,
    UP_SECONDS      NUMBER(4, 0) DEFAULT 0            NOT NULL,
    SAD_SECONDS     NUMBER(4, 0) DEFAULT 0            NOT NULL,
    DOWN_SECONDS    NUMBER(4, 0) DEFAULT 0            NOT NULL,
    STUDIES_SECONDS NUMBER(4, 0) DEFAULT 0            NOT NULL,
    ACC_SECONDS     NUMBER(4, 0) DEFAULT 0            NOT NULL,
    RESTORE_SECONDS NUMBER(4, 0) DEFAULT 0            NOT NULL,
    CONSTRAINT CC_ACC_HOUR_PK PRIMARY KEY (CC_ACC_HOUR_ID),
    CONSTRAINT CC_ACC_HOUR_AK1 UNIQUE (DAY_AND_HOUR),
    CONSTRAINT CC_ACC_HOUR_CK1 CHECK (EXTRACT(MINUTE FROM DAY_AND_HOUR) = 0 AND
                                      EXTRACT(SECOND FROM DAY_AND_HOUR) = 0),
    CONSTRAINT CC_ACC_HOUR_CK2 CHECK (UP_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT CC_ACC_HOUR_CK3 CHECK (SAD_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT CC_ACC_HOUR_CK4 CHECK (DOWN_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT CC_ACC_HOUR_CK5 CHECK (STUDIES_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT CC_ACC_HOUR_CK6 CHECK (ACC_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT CC_ACC_HOUR_CK7 CHECK (RESTORE_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT CC_ACC_HOUR_CK8 CHECK (
                UP_SECONDS + SAD_SECONDS + DOWN_SECONDS + STUDIES_SECONDS + RESTORE_SECONDS + ACC_SECONDS = 3600 )
);

CREATE TABLE BTM_OWNER.REVISION_INFO
(
    REV      NUMBER(38, 0) NOT NULL,
    REVTSTMP NUMBER(19, 0) NOT NULL,
    USERNAME VARCHAR2(64)  NULL,
    ADDRESS  VARCHAR2(64)  NULL,
    CONSTRAINT REVISION_INFO_PK PRIMARY KEY (REV)
);

CREATE TABLE BTM_OWNER.EXP_SIGNATURE
(
    EXP_SIGNATURE_ID NUMBER(38, 0)                     NOT NULL,
    HALL                  CHAR(1)                           NOT NULL,
    START_DAY_AND_HOUR    TIMESTAMP(0) WITH LOCAL TIME ZONE NOT NULL,
    SIGNED_BY             VARCHAR2(64 CHAR)                 NOT NULL,
    SIGNED_ROLE           VARCHAR2(20 CHAR)                 NOT NULL,
    SIGNED_DATE           DATE                              NOT NULL,
    CONSTRAINT EXP_SIGNATURE_PK PRIMARY KEY (EXP_SIGNATURE_ID),
    CONSTRAINT EXP_SIGNATURE_AK1 UNIQUE (HALL, START_DAY_AND_HOUR, SIGNED_BY),
    CONSTRAINT EXP_SIGNATURE_AK2 UNIQUE (HALL, START_DAY_AND_HOUR, SIGNED_ROLE),
    CONSTRAINT EXP_SIGNATURE_CK1 CHECK (HALL IN ('A', 'B', 'C', 'D'))
);

CREATE TABLE BTM_OWNER.EXP_SHIFT_AUD
(
    EXP_SHIFT_ID       NUMBER(38, 0)                     NOT NULL,
    REV                NUMBER(38, 0)                     NULL,
    REVTYPE            NUMBER(3, 0)                      NULL,
    HALL               CHAR(1)                           NOT NULL,
    START_DAY_AND_HOUR TIMESTAMP(0) WITH LOCAL TIME ZONE NOT NULL,
    PURPOSE_ID         NUMBER(38, 0)                     NULL,
    LEADER             VARCHAR2(64)                      NULL,
    WORKERS            VARCHAR2(20)                      NULL,
    REMARK             VARCHAR2(2048)                    NULL,
    CONSTRAINT EXP_SHIFT_AUD_PK PRIMARY KEY (EXP_SHIFT_ID),
    CONSTRAINT EXP_SHIFT_AUD_FK1 FOREIGN KEY (REV) REFERENCES BTM_OWNER.REVISION_INFO (REV) ON DELETE SET NULL,
    CONSTRAINT EXP_SHIFT_AUD_CK1 CHECK (HALL IN ('A', 'B', 'C', 'D')),
    CONSTRAINT EXP_SHIFT_AUD_CH2 CHECK (EXTRACT(MINUTE FROM START_DAY_AND_HOUR) = 0 AND
                                             EXTRACT(SECOND FROM START_DAY_AND_HOUR) = 0)
);

CREATE TABLE BTM_OWNER.EXP_SHIFT_PURPOSE
(
    EXP_SHIFT_PURPOSE_ID      NUMBER(38, 0)      NOT NULL,
    HALL                      CHAR(1 CHAR)       NOT NULL,
    NAME                      VARCHAR2(64)       NOT NULL,
    EXPERIMENT                CHAR(1)            NOT NULL,
    ACTIVE                    CHAR(1) DEFAULT 1  NOT NULL,
    ALIAS                     VARCHAR2(64)       NULL,
    URL                       VARCHAR2(512 CHAR) NULL,
    CONSTRAINT EXP_SHIFT_PURPOSE_PK PRIMARY KEY (EXP_SHIFT_PURPOSE_ID),
    CONSTRAINT EXP_SHIFT_PURPOSE_AK1 UNIQUE (HALL, NAME),
    CONSTRAINT EXP_SHIFT_PURPOSE_AK2 UNIQUE (HALL, EXP_SHIFT_PURPOSE_ID),
    CONSTRAINT EXP_SHIFT_PURPOSE_CK1 CHECK (HALL IN ('A', 'B', 'C', 'D')),
    CONSTRAINT EXP_SHIFT_PURPOSE_CK2 CHECK (EXPERIMENT IN (0, 1)),
    CONSTRAINT EXP_SHIFT_PURPOSE_CK3 CHECK (ACTIVE IN (0, 1))
);

CREATE TABLE BTM_OWNER.EXP_SHIFT
(
    EXP_SHIFT_ID       NUMBER(38, 0)  NOT NULL,
    HALL               CHAR(1 CHAR)   NOT NULL,
    START_DAY_AND_HOUR TIMESTAMP(0)   NOT NULL,
    PURPOSE_ID         NUMBER(38, 0)  NOT NULL,
    LEADER             VARCHAR2(64)   NULL,
    WORKERS            VARCHAR2(256)  NULL,
    REMARK             VARCHAR2(2048) NULL,
    CONSTRAINT EXP_SHIFT_PK PRIMARY KEY (EXP_SHIFT_ID),
    CONSTRAINT EXP_SHIFT_AK1 UNIQUE (HALL, START_DAY_AND_HOUR),
    CONSTRAINT EXP_SHIFT_FK1 FOREIGN KEY (HALL, PURPOSE_ID) REFERENCES BTM_OWNER.EXP_SHIFT_PURPOSE (HALL, EXP_SHIFT_PURPOSE_ID),
    CONSTRAINT EXP_SHIFT_CK1 CHECK (HALL IN ('A', 'B', 'C', 'D')),
    CONSTRAINT EXP_SHIFT_CK2 CHECK (EXTRACT(HOUR FROM START_DAY_AND_HOUR) in (0, 8, 16) AND
                                         EXTRACT(MINUTE FROM START_DAY_AND_HOUR) = 0 AND
                                         EXTRACT(SECOND FROM START_DAY_AND_HOUR) = 0)
);

CREATE TABLE BTM_OWNER.EXP_HOUR_REASON_TIME_AUD
(
    EXP_HOUR_REASON_TIME_ID NUMBER(38, 0)          NOT NULL,
    REV                          NUMBER(38, 0)          NOT NULL,
    REVTYPE                      NUMBER(3, 0)           NOT NULL,
    HALL                         CHAR(1)                NOT NULL,
    SECONDS                      NUMBER(4, 0) DEFAULT 0 NOT NULL,
    EXP_HOUR_ID             NUMBER(38, 0)          NOT NULL,
    EXP_REASON_ID           NUMBER(38, 0)          NOT NULL,
    CONSTRAINT EXP_HOUR_REASON_TIME_AUD_PK PRIMARY KEY (EXP_HOUR_REASON_TIME_ID),
    CONSTRAINT EXP_HOUR_REASON_TIME_AUD_FK1 FOREIGN KEY (REV) REFERENCES BTM_OWNER.REVISION_INFO (REV) ON DELETE CASCADE,
    CONSTRAINT EXP_HOUR_REASON_TIME_AUD_CK1 CHECK (HALL IN ('A', 'B', 'C', 'D')),
    CONSTRAINT EXP_HOUR_REASON_TIME_AUD_CK2 CHECK (SECONDS BETWEEN 0 AND 3600)
);

CREATE TABLE BTM_OWNER.EXP_REASON
(
    EXP_REASON_ID NUMBER(38, 0)     NOT NULL,
    HALL               CHAR(1 CHAR)      NOT NULL,
    NAME               VARCHAR2(64)      NOT NULL,
    ACTIVE             CHAR(1) DEFAULT 1 NOT NULL,
    CONSTRAINT EXP_REASON_PK PRIMARY KEY (EXP_REASON_ID),
    CONSTRAINT EXP_REASON_AK1 UNIQUE (HALL, EXP_REASON_ID),
    CONSTRAINT EXP_REASON_AK2 UNIQUE (HALL, NAME),
    CONSTRAINT EXP_REASON_CK1 CHECK (HALL IN ('A', 'B', 'C', 'D')),
    CONSTRAINT EXP_REASON_CK2 CHECK (ACTIVE IN (0, 1))
);

CREATE TABLE BTM_OWNER.EXP_HOUR
(
    EXP_HOUR_ID NUMBER(38, 0)                     NOT NULL,
    HALL             CHAR(1)                           NOT NULL,
    DAY_AND_HOUR     TIMESTAMP(0) WITH LOCAL TIME ZONE NOT NULL,
    ABU_SECONDS      NUMBER(4, 0) DEFAULT 0            NOT NULL,
    BANU_SECONDS     NUMBER(4, 0) DEFAULT 0            NOT NULL,
    BNA_SECONDS      NUMBER(4, 0) DEFAULT 0            NOT NULL,
    ACC_SECONDS      NUMBER(4, 0) DEFAULT 0            NOT NULL,
    ER_SECONDS       NUMBER(4, 0) DEFAULT 0            NOT NULL,
    PCC_SECONDS      NUMBER(4, 0) DEFAULT 0            NOT NULL,
    UED_SECONDS      NUMBER(4, 0) DEFAULT 0            NOT NULL,
    SCHED_SECONDS    NUMBER(4, 0) DEFAULT 0            NOT NULL,
    STUDIES_SECONDS  NUMBER(4, 0) DEFAULT 0            NOT NULL,
    OFF_SECONDS      NUMBER(4, 0) DEFAULT 0            NOT NULL,
    REMARK           VARCHAR2(2048 CHAR)               NULL,
    CONSTRAINT EXP_HOUR_PK PRIMARY KEY (EXP_HOUR_ID),
    CONSTRAINT EXP_HOUR_AK1 UNIQUE (HALL, DAY_AND_HOUR),
    CONSTRAINT EXP_HOUR_AK2 UNIQUE (HALL, EXP_HOUR_ID),
    CONSTRAINT EXP_HOUR_CK1 CHECK (HALL IN ('A', 'B', 'C', 'D')),
    CONSTRAINT EXP_HOUR_CK2 CHECK (EXTRACT(MINUTE FROM DAY_AND_HOUR) = 0 AND
                                        EXTRACT(SECOND FROM DAY_AND_HOUR) = 0),
    CONSTRAINT EXP_HOUR_CK3 CHECK (ABU_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT EXP_HOUR_CK4 CHECK (BANU_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT EXP_HOUR_CK5 CHECK (BNA_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT EXP_HOUR_CK6 CHECK (ACC_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT EXP_HOUR_CK7 CHECK (ER_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT EXP_HOUR_CK8 CHECK (PCC_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT EXP_HOUR_CK9 CHECK (UED_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT EXP_HOUR_CK10 CHECK (SCHED_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT EXP_HOUR_CK11 CHECK (STUDIES_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT EXP_HOUR_CK12 CHECK (OFF_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT EXP_HOUR_CK13 CHECK ( abu_seconds + banu_seconds + bna_seconds + acc_seconds + off_seconds = 3600 ),
    CONSTRAINT EXP_HOUR_CK14 CHECK ( er_seconds + pcc_seconds + ued_seconds + off_seconds = 3600 )
);

CREATE TABLE BTM_OWNER.EXP_HOUR_REASON_TIME
(
    EXP_HOUR_REASON_TIME_ID NUMBER(38, 0)          NOT NULL,
    HALL                    CHAR(1 CHAR)           NOT NULL,
    EXP_HOUR_ID             NUMBER(38, 0)          NOT NULL,
    EXP_REASON_ID           NUMBER(38, 0)          NOT NULL,
    SECONDS                 NUMBER(4, 0) DEFAULT 0 NOT NULL,
    CONSTRAINT EXP_HOUR_REASON_TIME_PK PRIMARY KEY (EXP_HOUR_REASON_TIME_ID),
    CONSTRAINT EXP_HOUR_REASON_TIME_AK1 UNIQUE (HALL, EXP_REASON_ID, EXP_HOUR_ID),
    CONSTRAINT EXP_HOUR_REASON_TIME_FK1 FOREIGN KEY (HALL, EXP_HOUR_ID) REFERENCES BTM_OWNER.EXP_HOUR (HALL, EXP_HOUR_ID) ON DELETE CASCADE,
    CONSTRAINT EXP_HOUR_REASON_TIME_FK2 FOREIGN KEY (HALL, EXP_REASON_ID) REFERENCES BTM_OWNER.EXP_REASON (HALL, EXP_REASON_ID) ON DELETE CASCADE,
    CONSTRAINT EXP_HOUR_REASON_TIME_CK1 CHECK (HALL IN ('A', 'B', 'C', 'D')),
    CONSTRAINT EXP_HOUR_REASON_TIME_CK2 CHECK (SECONDS BETWEEN 0 AND 3600)
);

CREATE TABLE BTM_OWNER.EXP_HOUR_AUD
(
    EXP_HOUR_ID      NUMBER(38, 0)                     NOT NULL,
    REV              NUMBER(38, 0)                     NULL,
    REVTYPE          NUMBER(3, 0)                      NOT NULL,
    HALL             CHAR(1)                           NOT NULL,
    DAY_AND_HOUR     TIMESTAMP(0) WITH LOCAL TIME ZONE NOT NULL,
    ABU_SECONDS      NUMBER(4, 0) DEFAULT 0            NOT NULL,
    BANU_SECONDS     NUMBER(4, 0) DEFAULT 0            NOT NULL,
    BNA_SECONDS      NUMBER(4, 0) DEFAULT 0            NOT NULL,
    ACC_SECONDS      NUMBER(4, 0) DEFAULT 0            NOT NULL,
    ER_SECONDS       NUMBER(4, 0) DEFAULT 0            NOT NULL,
    PCC_SECONDS      NUMBER(4, 0) DEFAULT 0            NOT NULL,
    UED_SECONDS      NUMBER(4, 0) DEFAULT 0            NOT NULL,
    SCHED_SECONDS    NUMBER(4, 0) DEFAULT 0            NOT NULL,
    STUDIES_SECONDS  NUMBER(4, 0) DEFAULT 0            NOT NULL,
    OFF_SECONDS      NUMBER(4, 0) DEFAULT 0            NOT NULL,
    REMARK           VARCHAR2(2048)                    NULL,
    CONSTRAINT EXP_HOUR_AUD_PK PRIMARY KEY (EXP_HOUR_ID),
    CONSTRAINT EXP_HOUR_AUD_FK1 FOREIGN KEY (REV) REFERENCES BTM_OWNER.REVISION_INFO (REV) ON DELETE SET NULL,
    CONSTRAINT EXP_HOUR_AUD_CK1 CHECK (HALL IN ('A', 'B', 'C', 'D')),
    CONSTRAINT EXP_HOUR_AUD_CK2 CHECK (EXTRACT(MINUTE FROM DAY_AND_HOUR) = 0 AND
                                       EXTRACT(SECOND FROM DAY_AND_HOUR) = 0),
    CONSTRAINT EXP_HOUR_AUD_CK3 CHECK (ABU_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT EXP_HOUR_AUD_CK4 CHECK (BANU_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT EXP_HOUR_AUD_CK5 CHECK (BNA_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT EXP_HOUR_AUD_CK6 CHECK (ACC_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT EXP_HOUR_AUD_CK7 CHECK (ER_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT EXP_HOUR_AUD_CK8 CHECK (PCC_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT EXP_HOUR_AUD_CK9 CHECK (UED_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT EXP_HOUR_AUD_CK10 CHECK (SCHED_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT EXP_HOUR_AUD_CK11 CHECK (STUDIES_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT EXP_HOUR_AUD_CK12 CHECK (OFF_SECONDS BETWEEN 0 AND 3600)
);

CREATE TABLE BTM_OWNER.EXP_EMAIL_RECIPIENT
(
    EXP_EMAIL_RECIPIENT_ID NUMBER(38, 0) NOT NULL,
    HALL                   CHAR(1)       NOT NULL,
    EMAIL                  VARCHAR2(256) NOT NULL,
    CONSTRAINT EXP_EMAIL_RECIPIENT_PK PRIMARY KEY (EXP_EMAIL_RECIPIENT_ID),
    CONSTRAINT EXP_EMAIL_RECIPIENT_AK1 UNIQUE (HALL, EMAIL),
    CONSTRAINT EXP_EMAIL_RECIPIENT_CK1 CHECK (HALL IN ('A', 'B', 'C', 'D'))
);

CREATE TABLE BTM_OWNER.PD_SHIFT_PLAN
(
    START_DAY_AND_HOUR  DATE                   NOT NULL,
    PHYSICS_SECONDS     NUMBER(4, 0) DEFAULT 0 NOT NULL,
    STUDIES_SECONDS     NUMBER(4, 0) DEFAULT 0 NOT NULL,
    RESTORE_SECONDS     NUMBER(4, 0) DEFAULT 0 NOT NULL,
    ACC_SECONDS         NUMBER(4, 0) DEFAULT 0 NOT NULL,
    DOWN_SECONDS        NUMBER(4, 0) DEFAULT 0 NOT NULL,
    SAD_SECONDS         NUMBER(4, 0) DEFAULT 0 NOT NULL,
    HALL_A_UP_SECONDS   NUMBER(4, 0) DEFAULT 0 NOT NULL,
    HALL_A_TUNE_SECONDS NUMBER(4, 0) DEFAULT 0 NOT NULL,
    HALL_A_BNR_SECONDS  NUMBER(4, 0) DEFAULT 0 NOT NULL,
    HALL_A_DOWN_SECONDS NUMBER(4, 0) DEFAULT 0 NOT NULL,
    HALL_A_OFF_SECONDS  NUMBER(4, 0) DEFAULT 0 NOT NULL,
    HALL_B_UP_SECONDS   NUMBER(4, 0) DEFAULT 0 NOT NULL,
    HALL_B_TUNE_SECONDS NUMBER(4, 0) DEFAULT 0 NOT NULL,
    HALL_B_BNR_SECONDS  NUMBER(4, 0) DEFAULT 0 NOT NULL,
    HALL_B_DOWN_SECONDS NUMBER(4, 0) DEFAULT 0 NOT NULL,
    HALL_B_OFF_SECONDS  NUMBER(4, 0) DEFAULT 0 NOT NULL,
    HALL_C_UP_SECONDS   NUMBER(4, 0) DEFAULT 0 NOT NULL,
    HALL_C_TUNE_SECONDS NUMBER(4, 0) DEFAULT 0 NOT NULL,
    HALL_C_BNR_SECONDS  NUMBER(4, 0) DEFAULT 0 NOT NULL,
    HALL_C_DOWN_SECONDS NUMBER(4, 0) DEFAULT 0 NOT NULL,
    HALL_C_OFF_SECONDS  NUMBER(4, 0) DEFAULT 0 NOT NULL,
    HALL_D_UP_SECONDS   NUMBER(4, 0) DEFAULT 0 NOT NULL,
    HALL_D_TUNE_SECONDS NUMBER(4, 0) DEFAULT 0 NOT NULL,
    HALL_D_BNR_SECONDS  NUMBER(4, 0) DEFAULT 0 NOT NULL,
    HALL_D_DOWN_SECONDS NUMBER(4, 0) DEFAULT 0 NOT NULL,
    HALL_D_OFF_SECONDS  NUMBER(4, 0) DEFAULT 0 NOT NULL
);

/*
GRANT SELECT ON PD_OWNER.SHIFT_PLANS TO BTM_OWNER;
GRANT SELECT ON BTA_OWNER.SHIFT_PLANS TO BTM_OWNER;
CREATE OR REPLACE FORCE VIEW "BTM_OWNER"."PD_SHIFT_PLAN" ("START_DAY_AND_HOUR", "PHYSICS_SECONDS", "STUDIES_SECONDS",
                                                           "RESTORE_SECONDS", "ACC_SECONDS", "DOWN_SECONDS",
                                                           "SAD_SECONDS", "HALL_A_UP_SECONDS", "HALL_A_TUNE_SECONDS",
                                                           "HALL_A_BNR_SECONDS", "HALL_A_DOWN_SECONDS",
                                                           "HALL_A_OFF_SECONDS", "HALL_B_UP_SECONDS",
                                                           "HALL_B_TUNE_SECONDS", "HALL_B_BNR_SECONDS",
                                                           "HALL_B_DOWN_SECONDS", "HALL_B_OFF_SECONDS",
                                                           "HALL_C_UP_SECONDS", "HALL_C_TUNE_SECONDS",
                                                           "HALL_C_BNR_SECONDS", "HALL_C_DOWN_SECONDS",
                                                           "HALL_C_OFF_SECONDS", "HALL_D_UP_SECONDS",
                                                           "HALL_D_TUNE_SECONDS", "HALL_D_BNR_SECONDS",
                                                           "HALL_D_DOWN_SECONDS", "HALL_D_OFF_SECONDS") AS
    SELECT begins_at               AS start_day_and_hour,
           physics * 60 * 60       AS physics_seconds,
           beam_studies * 60 * 60  AS studies_seconds,
           restore * 60 * 60       AS restore_seconds,
           config_change * 60 * 60 AS acc_seconds,
           NULL                    AS down_seconds,
           OFF * 60 * 60           AS sad_seconds,
           a_hours * 60 * 60       AS hall_a_up_seconds,
           NULL                    AS hall_a_tune_seconds,
           NULL                    AS hall_a_bnr_seconds,
           NULL                    AS hall_a_down_seconds,
           NULL                    AS hall_a_off_seconds,
           b_hours * 60 * 60       AS hall_b_up_seconds,
           NULL                    AS hall_b_tune_seconds,
           NULL                    AS hall_b_bnr_seconds,
           NULL                    AS hall_b_down_seconds,
           NULL                    AS hall_b_off_seconds,
           c_hours * 60 * 60       AS hall_c_up_seconds,
           NULL                    AS hall_c_tune_seconds,
           NULL                    AS hall_c_bnr_seconds,
           NULL                    AS hall_c_down_seconds,
           NULL                    AS hall_c_off_seconds,
           d_hours * 60 * 60       AS hall_d_up_seconds,
           NULL                    AS hall_d_tune_seconds,
           NULL                    AS hall_d_bnr_seconds,
           NULL                    AS hall_d_down_seconds,
           NULL                    AS hall_d_off_seconds
    FROM PD_OWNER.shift_plans
    WHERE begins_at >= to_date('2016-01-01', 'YYYY-MM-DD')
    UNION ALL
    SELECT start_time                  AS start_day_and_hour,
           NULL                        AS physics_seconds,
           beam_studies * 60 * 60      AS studies_seconds,
           acc_restore * 60 * 60       AS restore_seconds,
           acc_config_change * 60 * 60 AS acc_seconds,
           NULL                        AS down_seconds,
           downtime * 60 * 60          AS sad_seconds,
           halla_hours * 60 * 60       AS hall_a_up_seconds,
           NULL                        AS hall_a_tune_seconds,
           NULL                        AS hall_a_bnr_seconds,
           NULL                        AS hall_a_down_seconds,
           NULL                        AS hall_a_off_seconds,
           hallb_hours * 60 * 60       AS hall_b_up_seconds,
           NULL                        AS hall_b_tune_seconds,
           NULL                        AS hall_b_bnr_seconds,
           NULL                        AS hall_b_down_seconds,
           NULL                        AS hall_b_off_seconds,
           hallc_hours * 60 * 60       AS hall_c_up_seconds,
           NULL                        AS hall_c_tune_seconds,
           NULL                        AS hall_c_bnr_seconds,
           NULL                        AS hall_c_down_seconds,
           NULL                        AS hall_c_off_seconds,
           halld_hours * 60 * 60       AS hall_d_up_seconds,
           NULL                        AS hall_d_tune_seconds,
           NULL                        AS hall_d_bnr_seconds,
           NULL                        AS hall_d_down_seconds,
           NULL                        AS hall_d_off_seconds
    FROM BTA_OWNER.shift_plans
    WHERE start_time < to_date('2016-01-01', 'YYYY-MM-DD');
*/


CREATE TABLE BTM_OWNER.EVENT_FIRST_INCIDENT
(
    EVENT_ID            INTEGER                           NOT NULL,
    EVENT_TITLE         VARCHAR2(128 CHAR)                NOT NULL,
    TIME_UP             TIMESTAMP(0) WITH LOCAL TIME ZONE NULL,
    EVENT_TYPE_ID       INTEGER                           NOT NULL,
    INCIDENT_ID         INTEGER                           NOT NULL,
    TITLE               VARCHAR2(128 CHAR)                NOT NULL,
    TIME_DOWN           TIMESTAMP(0) WITH LOCAL TIME ZONE NOT NULL,
    NUMBER_OF_INCIDENTS INTEGER                           NOT NULL
);

/*
GRANT SELECT ON DTM_OWNER.EVENT_FIRST_INCIDENT TO BTM_OWNER;
CREATE OR REPLACE SYNONYM BTM_OWNER.EVENT_FIRST_INCIDENT
FOR DTM_OWNER.EVENT_FIRST_INCIDENT;
*/

-- Functions

CREATE FUNCTION BTM_OWNER.INTERVAL_TO_SECONDS(p_interval INTERVAL DAY TO SECOND) RETURN NUMBER IS
BEGIN
    RETURN EXTRACT(DAY FROM p_interval) * 86400 + EXTRACT(HOUR FROM p_interval) * 3600 +
           EXTRACT(MINUTE FROM p_interval) * 60 + EXTRACT(SECOND FROM p_interval);
END;
/