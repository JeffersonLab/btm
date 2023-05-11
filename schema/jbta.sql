-- CREATE USER
CREATE USER "JBTA_OWNER" PROFILE "DEFAULT" IDENTIFIED BY "***"
    DEFAULT TABLESPACE "WEBAPPS"
    TEMPORARY TABLESPACE "TEMP"
    QUOTA UNLIMITED ON "WEBAPPS" ACCOUNT UNLOCK;

GRANT CREATE JOB TO "JBTA_OWNER";
GRANT CREATE PROCEDURE TO "JBTA_OWNER";
GRANT CREATE SEQUENCE TO "JBTA_OWNER";
GRANT CREATE SESSION TO "JBTA_OWNER";
GRANT CREATE SYNONYM TO "JBTA_OWNER";
GRANT CREATE TABLE TO "JBTA_OWNER";
GRANT CREATE TRIGGER TO "JBTA_OWNER";
GRANT CREATE TYPE TO "JBTA_OWNER";
GRANT CREATE VIEW TO "JBTA_OWNER";
-- This is a role
GRANT CONNECT TO "JBTA_OWNER";
-- Bridge between old and new
GRANT SELECT ON BTA_OWNER.HALL_AVAILABILITY TO JBTA_OWNER;
-- Bridge between old and new
GRANT SELECT ON BTA_OWNER.AVAILABILITY TO JBTA_OWNER;
grant select on bta_owner.shift_plans to jbta_owner;


grant select on pd_owner.shift_plans to jbta_owner;
grant execute on dtm_owner.interval_to_seconds to jbta_owner;
grant select on dtm_owner.event_first_incident to jbta_owner;

-- DTM uses BTM to discover program time (when was the machine running)
grant select on jbta_owner.op_acc_hour to dtm_owner;




--- Create BTM SHIFTER USER
create user btm_shifter profile default identified by "***" default tablespace jbta account unlock;
grant connect to btm_shifter;
grant select on jbta_owner.op_shift to btm_shifter;
grant select on jbta_owner.op_shift_id to btm_shifter;
grant insert on jbta_owner.op_shift to btm_shifter;




CREATE SEQUENCE JBTA_OWNER.EXP_HALL_EMAIL_RECIPIENT_ID
    INCREMENT BY 1
    START WITH 1;

CREATE SEQUENCE JBTA_OWNER.EXP_HALL_HOUR_ID
    INCREMENT BY 1
    START WITH 1;

CREATE SEQUENCE JBTA_OWNER.EXP_HALL_HOUR_REASON_TIME_ID
    INCREMENT BY 1
    START WITH 1;

CREATE SEQUENCE JBTA_OWNER.EXP_HALL_SHIFT_ID
    INCREMENT BY 1
    START WITH 1;

CREATE SEQUENCE JBTA_OWNER.EXP_HALL_SHIFT_PURPOSE_ID
    INCREMENT BY 1
    START WITH 1;

CREATE SEQUENCE JBTA_OWNER.EXP_HALL_SIGNATURE_ID
    INCREMENT BY 1
    START WITH 1;

CREATE SEQUENCE JBTA_OWNER.OP_ACC_HOUR_ID;

CREATE SEQUENCE JBTA_OWNER.OP_CROSS_CHECK_COMMENT_ID
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;

CREATE SEQUENCE JBTA_OWNER.OP_HALL_HOUR_ID
    INCREMENT BY 1
    START WITH 1;

CREATE SEQUENCE JBTA_OWNER.OP_MULTIPLICITY_HOUR_ID;

CREATE SEQUENCE JBTA_OWNER.OP_SHIFT_ID;

CREATE SEQUENCE JBTA_OWNER.OP_SIGNATURE_ID;

CREATE SEQUENCE JBTA_OWNER.SCHEDULE_DAY_ID
    INCREMENT BY 1
    START WITH 1
    NOCACHE
    ORDER;

CREATE SEQUENCE JBTA_OWNER.SCHEDULE_ID
    INCREMENT BY 1
    START WITH 1
    NOCACHE
    ORDER;

CREATE TABLE JBTA_OWNER.MONTHLY_SCHEDULE
(
    MONTHLY_SCHEDULE_ID INTEGER      NOT NULL,
    START_DAY           TIMESTAMP(0) NOT NULL
        CONSTRAINT MONTHLY_SCHEDULE_CK1 CHECK (EXTRACT(DAY FROM START_DAY) = 1 and EXTRACT(HOUR FROM START_DAY) = 0 and
                                               EXTRACT(MINUTE FROM START_DAY) = 0 AND
                                               EXTRACT(SECOND FROM START_DAY) = 0),
    VERSION             INTEGER      NOT NULL,
    PUBLISHED_DATE      DATE         NULL,
    CONSTRAINT MONTLY_SCHEDULE_PK PRIMARY KEY (MONTHLY_SCHEDULE_ID),
    CONSTRAINT MONTHLY_SCHEDULE_AK1 UNIQUE (START_DAY, VERSION)
);

CREATE TABLE JBTA_OWNER.SCHEDULE_DAY
(
    SCHEDULE_DAY_ID     INTEGER                NOT NULL,
    DAY_MONTH_YEAR      DATE                   NOT NULL
        CONSTRAINT SCHEDULE_DAY_CK1 CHECK (DAY_MONTH_YEAR = TRUNC(DAY_MONTH_YEAR)),
    MONTHLY_SCHEDULE_ID INTEGER                NOT NULL,
    ACC_PROGRAM         VARCHAR2(24 CHAR)      NOT NULL
        CONSTRAINT SCHEDULE_DAY_CK2 CHECK (ACC_PROGRAM IN
                                           ('PHYSICS', 'STUDIES', 'RESTORE', 'ACC', 'DOWN', 'OFF', 'TBD', 'FACDEV')),
    KILO_VOLTS_PER_PASS INTEGER                NULL,
    NOTE                VARCHAR2(256 CHAR)     NULL,
    HALL_A_PROGRAM_ID   INTEGER                NOT NULL,
    HALL_A_NANO_AMPS    INTEGER                NULL,
    HALL_A_KILO_VOLTS   INTEGER                NULL,
    HALL_A_PASSES       INTEGER                NULL,
    HALL_A_PRIORITY     INTEGER                NULL,
    HALL_A_POLARIZED    CHAR(1 BYTE) DEFAULT 1 NOT NULL
        CONSTRAINT SCHEDULE_DAY_CK3 CHECK (HALL_A_POLARIZED IN (0, 1)),
    HALL_B_PROGRAM_ID   INTEGER                NOT NULL,
    HALL_B_NANO_AMPS    INTEGER                NULL,
    HALL_B_KILO_VOLTS   INTEGER                NULL,
    HALL_B_PASSES       INTEGER                NULL,
    HALL_B_PRIORITY     INTEGER                NULL,
    HALL_B_POLARIZED    CHAR(1 BYTE) DEFAULT 1 NOT NULL
        CONSTRAINT VALIDATE_BOOLEAN_2098280771 CHECK (HALL_B_POLARIZED IN (0, 1)),
    HALL_C_PROGRAM_ID   INTEGER                NULL,
    HALL_C_NANO_AMPS    INTEGER                NULL,
    HALL_C_KILO_VOLTS   INTEGER                NULL,
    HALL_C_PASSES       INTEGER                NULL,
    HALL_C_PRIORITY     INTEGER                NULL,
    HALL_C_POLARIZED    CHAR(1 BYTE) DEFAULT 1 NOT NULL
        CONSTRAINT SCHEDULE_DAY_CK5 CHECK (HALL_C_POLARIZED IN (0, 1)),
    HALL_D_PROGRAM_ID   INTEGER                NULL,
    HALL_D_NANO_AMPS    INTEGER                NULL,
    HALL_D_KILO_VOLTS   INTEGER                NULL,
    HALL_D_PASSES       INTEGER                NULL,
    HALL_D_PRIORITY     INTEGER                NULL,
    HALL_D_POLARIZED    CHAR(1 BYTE) DEFAULT 1 NOT NULL
        CONSTRAINT SCHEDULE_DAY_CK6 CHECK (HALL_D_POLARIZED IN (0, 1)),
    HALL_A_NOTE         VARCHAR2(256 CHAR)     NULL,
    HALL_B_NOTE         VARCHAR2(256 CHAR)     NULL,
    HALL_C_NOTE         VARCHAR2(256 CHAR)     NULL,
    HALL_D_NOTE         VARCHAR2(256 CHAR)     NULL,
    MIN_HALL_COUNT      INTEGER                NULL,
    CONSTRAINT SCHEDULE_DAY_PK PRIMARY KEY (SCHEDULE_DAY_ID),
    CONSTRAINT SCHEDULE_DAY_AK1 UNIQUE (DAY_MONTH_YEAR, MONTHLY_SCHEDULE_ID),
    CONSTRAINT SCHEDULE_DAY_FK1 FOREIGN KEY (MONTHLY_SCHEDULE_ID) REFERENCES JBTA_OWNER.MONTHLY_SCHEDULE (MONTHLY_SCHEDULE_ID) ON DELETE CASCADE
);

CREATE TABLE JBTA_OWNER.OP_SIGNATURE
(
    OP_SIGNATURE_ID    INTEGER                           NOT NULL,
    START_DAY_AND_HOUR TIMESTAMP(0) WITH LOCAL TIME ZONE NOT NULL
        CONSTRAINT OP_SIGNATURE_CK1 CHECK (EXTRACT(HOUR FROM START_DAY_AND_HOUR) in (23, 7, 15) AND
                                           EXTRACT(MINUTE FROM START_DAY_AND_HOUR) = 0 AND
                                           EXTRACT(SECOND FROM START_DAY_AND_HOUR) = 0),
    SIGNED_BY          INTEGER                           NOT NULL,
    SIGNED_ROLE        VARCHAR2(20 CHAR)                 NOT NULL
        CONSTRAINT OP_SIGNATURE_CK2 CHECK (SIGNED_ROLE IN ('CREW_CHIEF', 'OPERABILITY_MANAGER')),
    SIGNED_DATE        DATE                              NOT NULL,
    CONSTRAINT OP_SIGNATURE_PK PRIMARY KEY (OP_SIGNATURE_ID),
    CONSTRAINT OP_SIGNATURE_AK1 UNIQUE (START_DAY_AND_HOUR, SIGNED_BY, SIGNED_ROLE)
);

CREATE TABLE JBTA_OWNER.OP_SHIFT
(
    OP_SHIFT_ID        NUMBER(38, 0)                     NOT NULL,
    START_DAY_AND_HOUR TIMESTAMP(0) WITH LOCAL TIME ZONE NOT NULL
        CONSTRAINT OP_SHIFT_CK1 CHECK (EXTRACT(HOUR FROM START_DAY_AND_HOUR) in (23, 7, 15) AND
                                       EXTRACT(MINUTE FROM START_DAY_AND_HOUR) = 0 AND
                                       EXTRACT(SECOND FROM START_DAY_AND_HOUR) = 0),
    CREW_CHIEF         VARCHAR2(64 CHAR)                 NULL,
    OPERATORS          VARCHAR2(256 CHAR)                NULL,
    REMARK             VARCHAR2(2048 CHAR)               NULL,
    PROGRAM            VARCHAR2(64 CHAR)                 NULL,
    PROGRAM_DEPUTY     VARCHAR2(64 CHAR)                 NULL,
    CONSTRAINT OP_SHIFT_PK PRIMARY KEY (OP_SHIFT_ID),
    CONSTRAINT OP_SHIFT_AK1 UNIQUE (START_DAY_AND_HOUR)
);

CREATE TABLE JBTA_OWNER.OP_MULTIPLICITY_HOUR
(
    OP_MULTIPLICITY_HOUR_ID NUMBER(38, 0)                     NOT NULL,
    DAY_AND_HOUR            TIMESTAMP(0) WITH LOCAL TIME ZONE NOT NULL
        CONSTRAINT OP_MULTIPLICITY_CK1 CHECK (EXTRACT(MINUTE FROM DAY_AND_HOUR) = 0 AND
                                              EXTRACT(SECOND FROM DAY_AND_HOUR) = 0),
    ONE_HALL_UP_SECONDS     NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT OP_MULTIPLICITY_CK2 CHECK (ONE_HALL_UP_SECONDS BETWEEN 0 AND 3600),
    TWO_HALL_UP_SECONDS     NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT OP_MULTIPLICITY_CK3 CHECK (TWO_HALL_UP_SECONDS BETWEEN 0 AND 3600),
    THREE_HALL_UP_SECONDS   NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT OP_MULTIPLICITY_CK4 CHECK (THREE_HALL_UP_SECONDS BETWEEN 0 AND 3600),
    ANY_HALL_UP_SECONDS     NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT OP_MULTIPLICITY_CK5 CHECK (ANY_HALL_UP_SECONDS BETWEEN 0 AND 3600),
    ALL_HALL_UP_SECONDS     NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT OP_MULTIPLICITY_CK6 CHECK (ALL_HALL_UP_SECONDS BETWEEN 0 AND 3600),
    DOWN_HARD_SECONDS       NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT OP_MULTIPLICITY_CK7 CHECK (DOWN_HARD_SECONDS BETWEEN 0 AND 3600),
    FOUR_HALL_UP_SECONDS    NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT OP_MULTIPLICITY_CK8 CHECK (FOUR_HALL_UP_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT OP_MULTIPLICITY_HOUR_PK PRIMARY KEY (OP_MULTIPLICITY_HOUR_ID),
    CONSTRAINT OP_MULTIPLICITY_HOUR_AK1 UNIQUE (DAY_AND_HOUR)
);

CREATE TABLE JBTA_OWNER.OP_HALL_HOUR
(
    OP_HALL_HOUR_ID NUMBER(38, 0)                     NOT NULL,
    DAY_AND_HOUR    TIMESTAMP(0) WITH LOCAL TIME ZONE NOT NULL
        CONSTRAINT OP_HALL_HOUR_CK1 CHECK (EXTRACT(MINUTE FROM DAY_AND_HOUR) = 0 AND
                                           EXTRACT(SECOND FROM DAY_AND_HOUR) = 0),
    HALL            CHAR(1 CHAR)                      NOT NULL
        CONSTRAINT OP_HALL_HOUR_CK2 CHECK (HALL IN ('A', 'B', 'C', 'D')),
    UP_SECONDS      NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT OP_HALL_HOUR_CK3 CHECK (UP_SECONDS BETWEEN 0 AND 3600),
    TUNE_SECONDS    NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT OP_HALL_HOUR_CK4 CHECK (TUNE_SECONDS BETWEEN 0 AND 3600),
    BNR_SECONDS     NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT OP_HALL_HOUR_CK5 CHECK (BNR_SECONDS BETWEEN 0 AND 3600),
    DOWN_SECONDS    NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT OP_HALL_HOUR_CK6 CHECK (DOWN_SECONDS BETWEEN 0 AND 3600),
    OFF_SECONDS     NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT OP_HALL_HOUR_CK7 CHECK (OFF_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT OP_HALL_HOUR_PK PRIMARY KEY (OP_HALL_HOUR_ID),
    CONSTRAINT OP_HALL_HOUR_AK1 UNIQUE (HALL, DAY_AND_HOUR),
    CONSTRAINT OP_HALL_HOUR_CK8 CHECK ( UP_SECONDS + TUNE_SECONDS + BNR_SECONDS + DOWN_SECONDS + OFF_SECONDS = 3600 )
);

CREATE TABLE JBTA_OWNER.OP_CROSS_CHECK_COMMENT
(
    OP_CHECK_COMMENT_ID INTEGER                           NOT NULL,
    START_DAY_AND_HOUR  TIMESTAMP(0) WITH LOCAL TIME ZONE NOT NULL,
    REMARK              VARCHAR2(2048 CHAR)               NULL,
    REVIEWER_REMARK     VARCHAR2(2048 CHAR)               NULL,
    CONSTRAINT OP_CROSS_CHECK_COMMENT_PK PRIMARY KEY (OP_CHECK_COMMENT_ID),
    CONSTRAINT OP_CROSS_CHECK_COMMENT_AK1 UNIQUE (START_DAY_AND_HOUR)
);

CREATE TABLE JBTA_OWNER.OP_ACC_HOUR
(
    OP_ACC_HOUR_ID  INTEGER                           NOT NULL,
    DAY_AND_HOUR    TIMESTAMP(0) WITH LOCAL TIME ZONE NOT NULL
        CONSTRAINT OP_ACC_HOUR_CK1 CHECK (EXTRACT(MINUTE FROM DAY_AND_HOUR) = 0 AND
                                          EXTRACT(SECOND FROM DAY_AND_HOUR) = 0),
    UP_SECONDS      NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT OP_ACC_HOUR_CK2 CHECK (UP_SECONDS BETWEEN 0 AND 3600),
    SAD_SECONDS     NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT OP_ACC_HOUR_CK3 CHECK (SAD_SECONDS BETWEEN 0 AND 3600),
    DOWN_SECONDS    NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT OP_ACC_HOUR_CK4 CHECK (DOWN_SECONDS BETWEEN 0 AND 3600),
    STUDIES_SECONDS NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT OP_ACC_HOUR_CK5 CHECK (STUDIES_SECONDS BETWEEN 0 AND 3600),
    ACC_SECONDS     NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT OP_ACC_HOUR_CK6 CHECK (ACC_SECONDS BETWEEN 0 AND 3600),
    RESTORE_SECONDS NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT OP_ACC_HOUR_CK7 CHECK (RESTORE_SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT OP_ACC_HOUR_PK PRIMARY KEY (OP_ACC_HOUR_ID),
    CONSTRAINT OP_ACC_HOUR_AK1 UNIQUE (DAY_AND_HOUR),
    CONSTRAINT OP_ACC_HOUR_CK8 CHECK (
                UP_SECONDS + SAD_SECONDS + DOWN_SECONDS + STUDIES_SECONDS + RESTORE_SECONDS + ACC_SECONDS = 3600 )
);

CREATE TABLE JBTA_OWNER.REVISION_INFO
(
    REV      NUMBER(38, 0) NOT NULL,
    REVTSTMP NUMBER(19, 0) NOT NULL,
    USERNAME VARCHAR2(64)  NULL,
    ADDRESS  VARCHAR2(64)  NULL,
    CONSTRAINT REVISION_INFO_PK PRIMARY KEY (REV)
);

CREATE TABLE JBTA_OWNER.EXP_HALL_SIGNATURE_AUD
(
    EXP_HALL_SIGNATURE_ID NUMBER(38, 0) NOT NULL,
    REV                   NUMBER(38, 0) NOT NULL,
    REVTYPE               NUMBER(3, 0)  NULL,
    HALL                  CHAR(1)       NOT NULL
        CONSTRAINT VALID_HALL_1014052681 CHECK (HALL IN ('A', 'B', 'C', 'D')),
    START_DAY_AND_HOUR    TIMESTAMP     NOT NULL
        CONSTRAINT VALIDATE_ON_HOUR_873101454 CHECK (EXTRACT(MINUTE FROM START_DAY_AND_HOUR) = 0 AND
                                                     EXTRACT(SECOND FROM START_DAY_AND_HOUR) = 0),
    SIGNED_BY             NUMBER(10, 0) NOT NULL,
    SIGNED_ROLE           VARCHAR2(20)  NOT NULL,
    SIGNED_DATE           DATE          NOT NULL,
    CONSTRAINT EXP_HALL_SIGNATURE_AUD_PK PRIMARY KEY (EXP_HALL_SIGNATURE_ID),
    CONSTRAINT EXP_HALL_SIGNATURE_AUD_FK1 FOREIGN KEY (REV) REFERENCES JBTA_OWNER.REVISION_INFO (REV) ON DELETE SET NULL
);

CREATE TABLE JBTA_OWNER.EXP_HALL_SIGNATURE
(
    EXP_HALL_SIGNATURE_ID NUMBER(38, 0)                     NOT NULL,
    HALL                  CHAR(1)                           NOT NULL
        CONSTRAINT VALID_HALL_1850370422 CHECK (HALL IN ('A', 'B', 'C', 'D')),
    START_DAY_AND_HOUR    TIMESTAMP(0) WITH LOCAL TIME ZONE NOT NULL,
    SIGNED_BY             NUMBER(10, 0)                     NOT NULL,
    SIGNED_ROLE           VARCHAR2(20)                      NOT NULL,
    SIGNED_DATE           DATE                              NOT NULL,
    CONSTRAINT EXP_HALL_SIGNATURE_PK PRIMARY KEY (EXP_HALL_SIGNATURE_ID),
    CONSTRAINT EXP_HALL_SIGNATURE_AK1 UNIQUE (HALL, START_DAY_AND_HOUR, SIGNED_BY),
    CONSTRAINT EXP_HALL_SIGNATURE_AK2 UNIQUE (HALL, START_DAY_AND_HOUR, SIGNED_ROLE)
);

CREATE TABLE JBTA_OWNER.EXP_HALL_SHIFT_AUD
(
    EXP_HALL_SHIFT_ID  NUMBER(38, 0)                     NOT NULL,
    REV                NUMBER(38, 0)                     NULL,
    REVTYPE            NUMBER(3, 0)                      NULL,
    HALL               CHAR(1)                           NOT NULL
        CONSTRAINT VALID_HALL_1767666532 CHECK (HALL IN ('A', 'B', 'C', 'D')),
    START_DAY_AND_HOUR TIMESTAMP(0) WITH LOCAL TIME ZONE NOT NULL
        CONSTRAINT VALIDATE_ON_HOUR_640146630 CHECK (EXTRACT(MINUTE FROM START_DAY_AND_HOUR) = 0 AND
                                                     EXTRACT(SECOND FROM START_DAY_AND_HOUR) = 0),
    PURPOSE_ID         NUMBER(38, 0)                     NULL,
    LEADER             VARCHAR2(64)                      NULL,
    WORKERS            VARCHAR2(20)                      NULL,
    REMARK             VARCHAR2(2048)                    NULL,
    CONSTRAINT EXP_HALL_SHIFT_AUD_PK PRIMARY KEY (EXP_HALL_SHIFT_ID),
    CONSTRAINT EXP_HALL_SHIFT_AUD_FK1 FOREIGN KEY (REV) REFERENCES JBTA_OWNER.REVISION_INFO (REV) ON DELETE SET NULL
);

CREATE TABLE JBTA_OWNER.EXP_HALL_SHIFT_PURPOSE
(
    EXP_HALL_SHIFT_PURPOSE_ID NUMBER(38, 0)      NOT NULL,
    HALL                      CHAR(1 CHAR)       NOT NULL
        CONSTRAINT VALID_HALL_1129331774 CHECK (HALL IN ('A', 'B', 'C', 'D')),
    NAME                      VARCHAR2(64)       NOT NULL,
    EXPERIMENT                CHAR(1)            NOT NULL
        CONSTRAINT VALIDATE_BOOLEAN_1083331658 CHECK (EXPERIMENT IN (0, 1)),
    ACTIVE                    CHAR(1) DEFAULT 1  NOT NULL
        CONSTRAINT VALIDATE_BOOLEAN_1982618953 CHECK (ACTIVE IN (0, 1)),
    ALIAS                     VARCHAR2(64)       NULL,
    URL                       VARCHAR2(512 CHAR) NULL,
    CONSTRAINT EXP_HALL_SHIFT_PURPOSE_PK PRIMARY KEY (EXP_HALL_SHIFT_PURPOSE_ID),
    CONSTRAINT EXP_HALL_SHIFT_PURPOSE_AK1 UNIQUE (HALL, NAME),
    CONSTRAINT EXP_HALL_SHIFT_PURPOSE_AK2 UNIQUE (HALL, EXP_HALL_SHIFT_PURPOSE_ID)
);

CREATE TABLE JBTA_OWNER.EXP_HALL_SHIFT
(
    EXP_HALL_SHIFT_ID  NUMBER(38, 0)  NOT NULL,
    HALL               CHAR(1 CHAR)   NOT NULL
        CONSTRAINT VALID_HALL_337122339 CHECK (HALL IN ('A', 'B', 'C', 'D')),
    START_DAY_AND_HOUR TIMESTAMP(0)   NOT NULL
        CONSTRAINT VALIDATE_START_DATE_842010344 CHECK (EXTRACT(HOUR FROM START_DAY_AND_HOUR) in (0, 8, 16) AND
                                                        EXTRACT(MINUTE FROM START_DAY_AND_HOUR) = 0 AND
                                                        EXTRACT(SECOND FROM START_DAY_AND_HOUR) = 0),
    PURPOSE_ID         NUMBER(38, 0)  NOT NULL,
    LEADER             VARCHAR2(64)   NULL,
    WORKERS            VARCHAR2(256)  NULL,
    REMARK             VARCHAR2(2048) NULL,
    CONSTRAINT EXP_HALL_SHIFT_PK PRIMARY KEY (EXP_HALL_SHIFT_ID),
    CONSTRAINT EXP_HALL_SHIFT_AK1 UNIQUE (HALL, START_DAY_AND_HOUR),
    CONSTRAINT EXPERIMENTER_SHIFT_FK1 FOREIGN KEY (HALL, PURPOSE_ID) REFERENCES JBTA_OWNER.EXP_HALL_SHIFT_PURPOSE (HALL, EXP_HALL_SHIFT_PURPOSE_ID)
);

CREATE TABLE JBTA_OWNER.EXP_HALL_HOUR_REASON_TIME_AUD
(
    EXP_HALL_HOUR_REASON_TIME_ID NUMBER(38, 0)          NOT NULL,
    REV                          NUMBER(38, 0)          NULL,
    REVTYPE                      NUMBER(3, 0)           NOT NULL,
    HALL                         CHAR(1)                NOT NULL
        CONSTRAINT VALID_HALL_406878022 CHECK (HALL IN ('A', 'B', 'C', 'D')),
    SECONDS                      NUMBER(4, 0) DEFAULT 0 NOT NULL
        CONSTRAINT VALID_SECONDS_644898583 CHECK (SECONDS BETWEEN 0 AND 3600),
    HOUR_ID                      NUMBER(38, 0)          NULL,
    REASON_ID                    NUMBER(38, 0)          NULL,
    CONSTRAINT EXP_HALL_HOUR_REASON_TIME_AUD_ PRIMARY KEY (EXP_HALL_HOUR_REASON_TIME_ID),
    CONSTRAINT HOUR_REASON_TIME_AUD_FK1 FOREIGN KEY (REV) REFERENCES JBTA_OWNER.REVISION_INFO (REV) ON DELETE SET NULL
);

CREATE TABLE JBTA_OWNER.EXP_HALL_REASON
(
    EXP_HALL_REASON_ID NUMBER(38, 0)     NOT NULL,
    HALL               CHAR(1 CHAR)      NOT NULL
        CONSTRAINT VALID_HALL_470228078 CHECK (HALL IN ('A', 'B', 'C', 'D')),
    NAME               VARCHAR2(64)      NOT NULL,
    ACTIVE             CHAR(1) DEFAULT 1 NOT NULL
        CONSTRAINT VALIDATE_BOOLEAN_712788492 CHECK (ACTIVE IN (0, 1)),
    CONSTRAINT EXP_HALL_REASON_PK PRIMARY KEY (EXP_HALL_REASON_ID),
    CONSTRAINT EXP_HALL_REASON_AK1 UNIQUE (HALL, NAME)
);

CREATE TABLE JBTA_OWNER.EXP_HALL_HOUR
(
    EXP_HALL_HOUR_ID NUMBER(38, 0)                     NOT NULL,
    HALL             CHAR(1)                           NOT NULL
        CONSTRAINT EXP_HALL_HOUR_CK1 CHECK (HALL IN ('A', 'B', 'C', 'D')),
    DAY_AND_HOUR     TIMESTAMP(0) WITH LOCAL TIME ZONE NOT NULL
        CONSTRAINT EXP_HALL_HOUR_CK2 CHECK (EXTRACT(MINUTE FROM DAY_AND_HOUR) = 0 AND
                                            EXTRACT(SECOND FROM DAY_AND_HOUR) = 0),
    ABU_SECONDS      NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT EXP_HALL_HOUR_CK3 CHECK (ABU_SECONDS BETWEEN 0 AND 3600),
    BANU_SECONDS     NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT EXP_HALL_HOUR_CK4 CHECK (BANU_SECONDS BETWEEN 0 AND 3600),
    BNA_SECONDS      NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT EXP_HALL_HOUR_CK5 CHECK (BNA_SECONDS BETWEEN 0 AND 3600),
    ACC_SECONDS      NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT EXP_HALL_HOUR_CK6 CHECK (ACC_SECONDS BETWEEN 0 AND 3600),
    ER_SECONDS       NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT EXP_HALL_HOUR_CK7 CHECK (ER_SECONDS BETWEEN 0 AND 3600),
    PCC_SECONDS      NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT EXP_HALL_HOUR_CK8 CHECK (PCC_SECONDS BETWEEN 0 AND 3600),
    UED_SECONDS      NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT EXP_HALL_HOUR_CK9 CHECK (UED_SECONDS BETWEEN 0 AND 3600),
    SCHED_SECONDS    NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT EXP_HALL_HOUR_CK10 CHECK (SCHED_SECONDS BETWEEN 0 AND 3600),
    STUDIES_SECONDS  NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT EXP_HALL_HOUR_CK11 CHECK (STUDIES_SECONDS BETWEEN 0 AND 3600),
    OFF_SECONDS      NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT EXP_HALL_HOUR_CK12 CHECK (OFF_SECONDS BETWEEN 0 AND 3600),
    REMARK           VARCHAR2(2048 CHAR)               NULL,
    CONSTRAINT EXP_HALL_HOUR_PK PRIMARY KEY (EXP_HALL_HOUR_ID),
    CONSTRAINT EXP_HALL_HOUR_AK1 UNIQUE (HALL, DAY_AND_HOUR),
    CONSTRAINT EXP_HALL_HOUR_AK2 UNIQUE (HALL, EXP_HALL_HOUR_ID),
    CONSTRAINT EXP_HALL_HOUR_CK13 CHECK ( abu_seconds + banu_seconds + bna_seconds + acc_seconds + off_seconds = 3600 ),
    CONSTRAINT EXP_HALL_HOUR_CK14 CHECK ( er_seconds + pcc_seconds + ued_seconds + off_seconds = 3600 )
);

CREATE TABLE JBTA_OWNER.EXP_HALL_HOUR_REASON_TIME
(
    EXP_HALL_HOUR_REASON_TIME_ID NUMBER(38, 0)          NOT NULL,
    HALL                         CHAR(1 CHAR)           NOT NULL
        CONSTRAINT VALID_HALL_1023666172 CHECK (HALL IN ('A', 'B', 'C', 'D')),
    EXP_HALL_HOUR_ID             NUMBER(38, 0)          NULL,
    REASON                       VARCHAR2(64)           NOT NULL,
    SECONDS                      NUMBER(4, 0) DEFAULT 0 NOT NULL
        CONSTRAINT VALID_SECONDS_2051951525 CHECK (SECONDS BETWEEN 0 AND 3600),
    CONSTRAINT EXP_HALL_HOUR_REASON_TIME_PK PRIMARY KEY (EXP_HALL_HOUR_REASON_TIME_ID),
    CONSTRAINT EXP_HALL_HOUR_REASON_TIME_AK1 UNIQUE (HALL, REASON, EXP_HALL_HOUR_ID),
    CONSTRAINT R_35 FOREIGN KEY (HALL, EXP_HALL_HOUR_ID) REFERENCES JBTA_OWNER.EXP_HALL_HOUR (HALL, EXP_HALL_HOUR_ID) ON DELETE SET NULL,
    CONSTRAINT EXPERIMENTER_HOUR_REASON_FK1 FOREIGN KEY (HALL, REASON) REFERENCES JBTA_OWNER.EXP_HALL_REASON (HALL, NAME) ON DELETE CASCADE
);

CREATE TABLE JBTA_OWNER.EXP_HALL_HOUR_AUD
(
    EXP_HALL_HOUR_ID NUMBER(38, 0)                     NOT NULL,
    REV              NUMBER(38, 0)                     NULL,
    REVTYPE          NUMBER(3, 0)                      NOT NULL,
    HALL             CHAR(1)                           NOT NULL
        CONSTRAINT VALID_HALL_928673883 CHECK (HALL IN ('A', 'B', 'C', 'D')),
    DAY_AND_HOUR     TIMESTAMP(0) WITH LOCAL TIME ZONE NOT NULL
        CONSTRAINT VALIDATE_ON_HOUR_1361939924 CHECK (EXTRACT(MINUTE FROM DAY_AND_HOUR) = 0 AND
                                                      EXTRACT(SECOND FROM DAY_AND_HOUR) = 0),
    ABU_SECONDS      NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT VALID_SECONDS_333911395 CHECK (ABU_SECONDS BETWEEN 0 AND 3600),
    BANU_SECONDS     NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT VALID_SECONDS_1852003 CHECK (BANU_SECONDS BETWEEN 0 AND 3600),
    BNA_SECONDS      NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT VALID_SECONDS_351469923 CHECK (BNA_SECONDS BETWEEN 0 AND 3600),
    ACC_SECONDS      NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT VALID_SECONDS_333972323 CHECK (ACC_SECONDS BETWEEN 0 AND 3600),
    ER_SECONDS       NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT VALID_SECONDS_352184630 CHECK (ER_SECONDS BETWEEN 0 AND 3600),
    PCC_SECONDS      NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT VALID_SECONDS_585630563 CHECK (PCC_SECONDS BETWEEN 0 AND 3600),
    UED_SECONDS      NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT VALID_SECONDS_669647971 CHECK (UED_SECONDS BETWEEN 0 AND 3600),
    SCHED_SECONDS    NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT VALID_SECONDS_809517648 CHECK (SCHED_SECONDS BETWEEN 0 AND 3600),
    STUDIES_SECONDS  NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT VALID_SECONDS_1866360999 CHECK (STUDIES_SECONDS BETWEEN 0 AND 3600),
    OFF_SECONDS      NUMBER(4, 0) DEFAULT 0            NOT NULL
        CONSTRAINT VALID_SECONDS_569050723 CHECK (OFF_SECONDS BETWEEN 0 AND 3600),
    REMARK           VARCHAR2(2048)                    NULL,
    CONSTRAINT EXP_HALL_HOUR_AUD_PK PRIMARY KEY (EXP_HALL_HOUR_ID),
    CONSTRAINT EXP_HALL_HOUR_AUD_FK1 FOREIGN KEY (REV) REFERENCES JBTA_OWNER.REVISION_INFO (REV) ON DELETE SET NULL
);

CREATE TABLE JBTA_OWNER.EXP_HALL_EMAIL_RECIPIENT
(
    EXP_HALL_EMAIL_RECIPIENT_ID NUMBER(38, 0) NOT NULL,
    HALL                        CHAR(1)       NOT NULL
        CONSTRAINT VALID_HALL_1044712479 CHECK (HALL IN ('A', 'B', 'C', 'D')),
    EMAIL                       VARCHAR2(256) NOT NULL,
    CONSTRAINT EXP_HALL_EMAIL_RECIPIENT_PK PRIMARY KEY (EXP_HALL_EMAIL_RECIPIENT_ID),
    CONSTRAINT EXP_HALL_EMAIL_RECIPIENT_AK1 UNIQUE (HALL, EMAIL)
);

-- CREATE VIEWS


CREATE OR REPLACE FORCE VIEW "JBTA_OWNER"."PD_SHIFT_PLAN" ("START_DAY_AND_HOUR", "PHYSICS_SECONDS", "STUDIES_SECONDS",
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

CREATE OR REPLACE VIEW JBTA_OWNER.EXP_HALL_SHIFT_TIME_LEGACY AS
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
    FROM JBTA_OWNER.EXP_HALL_SHIFT_TIME
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

CREATE OR REPLACE VIEW JBTA_OWNER.AVAILABILITY AS
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
    FROM JBTA_OWNER.EXP_HALL_SHIFT_TIME x
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
