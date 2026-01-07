-- =============================================================================
-- WTV Service - H2 Schema (Oracle compatible)
-- Matches ENTITYDEV schema structure
-- =============================================================================

-- Drop tables if exist (for clean restart)
DROP TABLE IF EXISTS TIMETIN;
DROP TABLE IF EXISTS TIMENON;
DROP TABLE IF EXISTS ENT;
DROP TABLE IF EXISTS ENTEMP;
DROP TABLE IF EXISTS ENTMONTH;
DROP TABLE IF EXISTS ENTCODE;

-- =============================================================================
-- ENTEMP - Employee/Assignment Master (39 columns - all columns for entity)
-- =============================================================================
CREATE TABLE ENTEMP (
    ROID NUMBER(8) PRIMARY KEY,
    NAME VARCHAR2(35),
    GRADE NUMBER(2),
    TYPE CHAR(1),
    ICSACC CHAR(1),
    BADGE VARCHAR2(10),
    TITLE VARCHAR2(25),
    AREACD NUMBER(3),
    PHONE NUMBER(7),
    EXT NUMBER(7),
    SEID CHAR(5),
    EMAIL VARCHAR2(50),
    POSTYPE CHAR(1),
    AREA CHAR(1),
    TOUR NUMBER(1),
    PODIND CHAR(1),
    TPSIND CHAR(1),
    CSUIND CHAR(1),
    AIDEIND CHAR(1),
    FLEXIND CHAR(1),
    EMPDT DATE,
    ADJDT DATE,
    ADJREASON CHAR(4),
    ADJPERCENT NUMBER(3),
    PREVID NUMBER(8),
    EACTIVE CHAR(1),
    UNIX VARCHAR2(8),
    ELEVEL NUMBER(1),
    EXTRDT DATE,
    PRIMARY_ROID VARCHAR2(1),
    PODCD CHAR(3),
    ORG CHAR(2),
    LASTLOGIN DATE,
    GS9CNT NUMBER(4),
    GS11CNT NUMBER(4),
    GS12CNT NUMBER(4),
    GS13CNT NUMBER(4),
    LOGOFF DATE,
    IP_ADDR VARCHAR2(39)
);

CREATE INDEX ENTEMP_SEID_IX ON ENTEMP(SEID);
CREATE INDEX ENTEMP_ROID_IX ON ENTEMP(ROID);

-- =============================================================================
-- ENTMONTH - Pay Period / Reporting Month (CTRS Calendar)
-- Primary Key: RPTMONTH (e.g., "OCT2026", "NOV2026")
-- Fiscal Year: October to September
-- =============================================================================
CREATE TABLE ENTMONTH (
    RPTMONTH CHAR(7) PRIMARY KEY,
    STARTDT DATE,
    ENDDT DATE,
    WEEKS NUMBER(1),
    STARTCYC NUMBER(6),
    ENDCYC NUMBER(6),
    WORKDAYS NUMBER(2),
    RPTNATIONAL DATE
);

CREATE INDEX ENTMONTH_STARTDT_IX ON ENTMONTH(STARTDT);
CREATE INDEX ENTMONTH_ENDDT_IX ON ENTMONTH(ENDDT);

-- =============================================================================
-- ENTCODE - Time Code Reference
-- =============================================================================
CREATE TABLE ENTCODE (
    CODE CHAR(3),
    TYPE CHAR(1),
    CDNAME VARCHAR2(35),
    AREA NUMBER(4),
    EXTRDT DATE,
    ACTIVE CHAR(1),
    MGR CHAR(1),
    CLERK CHAR(1),
    PROF CHAR(1),
    PARA CHAR(1),
    DISP CHAR(1),
    TIMEDEF CHAR(1),
    CTRSDEF NUMBER(2),
    CTRSLN NUMBER(2),
    PRIMARY KEY (CODE, TYPE)
);

-- =============================================================================
-- TIMENON - Non-Case Time Entries
-- =============================================================================
CREATE TABLE TIMENON (
    RPTDT DATE,
    ROID NUMBER(8),
    TIMECODE CHAR(3),
    CONTCD CHAR(1),
    TIMEDEF CHAR(1),
    HOURS NUMBER(4,2),
    EXTRDT DATE,
    EMPIDNUM VARCHAR2(10),
    LATEFLAG CHAR(1),
    PRIMARY KEY (RPTDT, ROID, TIMECODE)
);

CREATE INDEX TIMENON_ROID_IX ON TIMENON(ROID);
CREATE INDEX TIMENON_RPTDT_IX ON TIMENON(RPTDT);

-- =============================================================================
-- TIMETIN - Case/TIN Time Entries
-- =============================================================================
CREATE TABLE TIMETIN (
    TIMESID NUMBER(10) PRIMARY KEY,
    RPTDT DATE,
    ROID NUMBER(8),
    CONTCD CHAR(1),
    CODE CHAR(3),
    SUBCODE CHAR(3),
    HOURS NUMBER(4,2),
    GRADE NUMBER(2),
    EXTRDT DATE,
    BODCD CHAR(2),
    BODCLCD CHAR(3),
    EMPIDNUM VARCHAR2(10),
    LATEFLAG CHAR(1),
    SEGIND CHAR(1),
    TDACNT NUMBER(3),
    TDICNT NUMBER(3),
    RISK NUMBER(3),
    PRGNAME1 VARCHAR2(40),
    PRGNAME2 VARCHAR2(40)
);

CREATE INDEX TIMETIN_ROID_IX ON TIMETIN(ROID);
CREATE INDEX TIMETIN_RPTDT_IX ON TIMETIN(RPTDT);

-- =============================================================================
-- ENT - Case/TIN Master (key columns only)
-- =============================================================================
CREATE TABLE ENT (
    TINSID NUMBER(10) PRIMARY KEY,
    EXTRDT DATE,
    TIN NUMBER(9),
    TINFS NUMBER(1),
    TINTT NUMBER(1),
    TP VARCHAR2(70),
    TP2 VARCHAR2(70),
    TPCTRL CHAR(4),
    STREET VARCHAR2(70),
    CITY VARCHAR2(25),
    STATE CHAR(2),
    ZIPCDE NUMBER(12),
    CASECODE CHAR(3),
    SUBCODE CHAR(3),
    GRADE NUMBER(2),
    TOTHRS NUMBER(7,2),
    STATUS CHAR(1),
    RISK NUMBER(3)
);

CREATE INDEX ENT_TIN_IX ON ENT(TIN);
