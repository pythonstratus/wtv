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
-- Primary Key: RPTMONTH (e.g., "OCT2025", "NOV2025")
-- Fiscal Year: October to September (FY2026 = Oct 2025 - Sep 2026)
-- =============================================================================
CREATE TABLE ENTMONTH (
    RPTMONTH CHAR(7) PRIMARY KEY,
    STARTDT DATE,
    ENDDT DATE,
    WEEKS NUMBER(1),
    STARTCYC NUMBER(6),
    ENDCYC NUMBER(6),
    WORKDAYS NUMBER(2),
    RPTNATIONAL DATE,
    -- New columns for CTRS Calendar enhancements
    ACTIVE CHAR(1) DEFAULT 'Y',           -- Active status: Y=active, N=inactive
    HOLIDAYS NUMBER(2) DEFAULT 0,          -- Total holidays in the month
    HOURS NUMBER(3),                       -- Total hours (if different from workdays)
    WEEK_DATA VARCHAR2(1000)               -- JSON storage for week-level data
);

CREATE INDEX ENTMONTH_STARTDT_IX ON ENTMONTH(STARTDT);
CREATE INDEX ENTMONTH_ENDDT_IX ON ENTMONTH(ENDDT);
CREATE INDEX ENTMONTH_ACTIVE_IX ON ENTMONTH(ACTIVE);

-- =============================================================================
-- ENTCODE - Time Code Reference
-- =============================================================================
CREATE TABLE ENTCODE (
    CODE CHAR(5),
    TYPE CHAR(1),
    ENTDESC VARCHAR2(50),
    ACTIVE CHAR(1),
    CATEGORY CHAR(1),
    TINCATEGORY CHAR(1),
    TIMEDEF CHAR(1),
    PRIMARY KEY (CODE, TYPE)
);

CREATE INDEX ENTCODE_TIMEDEF_IX ON ENTCODE(TIMEDEF);

-- =============================================================================
-- ENT - TIN/Case Master
-- =============================================================================
CREATE TABLE ENT (
    TIMESID NUMBER(8) PRIMARY KEY,
    ENTNAME VARCHAR2(50),
    TIN CHAR(9),
    JURISDICTION CHAR(2),
    ASSGNDT DATE,
    STATUS CHAR(1),
    ROID NUMBER(8)
);

CREATE INDEX ENT_ROID_IX ON ENT(ROID);
CREATE INDEX ENT_TIN_IX ON ENT(TIN);

-- =============================================================================
-- TIMENON - Non-Case Time Entries
-- =============================================================================
CREATE TABLE TIMENON (
    ROID NUMBER(8),
    RPTDT DATE,
    TIMECODE CHAR(5),
    HOURS NUMBER(4,2),
    ENTRYDT DATE,
    QUARTER CHAR(2),
    PRIMARY KEY (ROID, RPTDT, TIMECODE)
);

CREATE INDEX TIMENON_RPTDT_IX ON TIMENON(RPTDT);
CREATE INDEX TIMENON_ROID_IX ON TIMENON(ROID);

-- =============================================================================
-- TIMETIN - Case/TIN Time Entries
-- =============================================================================
CREATE TABLE TIMETIN (
    ROID NUMBER(8),
    TIMESID NUMBER(8),
    RPTDT DATE,
    HOURS NUMBER(4,2),
    ENTRYDT DATE,
    PRIMARY KEY (ROID, TIMESID, RPTDT)
);

CREATE INDEX TIMETIN_RPTDT_IX ON TIMETIN(RPTDT);
CREATE INDEX TIMETIN_ROID_IX ON TIMETIN(ROID);
CREATE INDEX TIMETIN_TIMESID_IX ON TIMETIN(TIMESID);
