-- ============================================================================
-- WEEK_DATA, HOLIDAYS, and HOURS Population Script for ENTMONTH Table
-- ============================================================================
-- Purpose: Automatically generates WEEK_DATA JSON and calculates HOLIDAYS/HOURS
--          for all rows in ENTMONTH
-- Author: WTV Modernization Team
-- Date: January 2025
-- 
-- Columns Updated:
--   - WEEK_DATA: JSON array with week-level details
--   - HOLIDAYS:  Total holidays for the month (calculated as WEEKS*5 - WORKDAYS)
--   - HOURS:     Total hours for the month (calculated as WORKDAYS * 8)
--
-- How to Run in TOAD:
--   1. View -> DBMS Output (F8), click green "+" to enable
--   2. Paste this script in Editor window
--   3. Press F5 (Execute as Script)
--   4. Check DBMS Output window for progress
--   5. Run verification query
--   6. COMMIT if satisfied, ROLLBACK if not
-- ============================================================================

DECLARE
    v_week_data         VARCHAR2(4000);
    v_week_json         VARCHAR2(500);
    v_week_start        DATE;
    v_week_end          DATE;
    v_cycle             NUMBER;
    v_workdays_remaining NUMBER;
    v_week_workdays     NUMBER;
    v_week_holidays     NUMBER;
    v_week_hours        NUMBER;
    v_total_weeks       NUMBER;
    v_row_count         NUMBER := 0;
    
    -- Month-level calculated values
    v_month_holidays    NUMBER;
    v_month_hours       NUMBER;
    
    CURSOR c_months IS
        SELECT RPTMONTH, STARTDT, ENDDT, WEEKS, STARTCYC, WORKDAYS, HOLIDAYS, HOURS
        FROM ENTMONTH
        WHERE STARTDT IS NOT NULL
          AND WEEKS IS NOT NULL
          AND WEEKS > 0
        ORDER BY STARTDT;
        
BEGIN
    DBMS_OUTPUT.PUT_LINE('=============================================================');
    DBMS_OUTPUT.PUT_LINE('WEEK_DATA, HOLIDAYS, HOURS Population Script - Starting');
    DBMS_OUTPUT.PUT_LINE('=============================================================');
    DBMS_OUTPUT.PUT_LINE('');
    
    FOR rec IN c_months LOOP
        v_week_data := '[';
        v_week_start := rec.STARTDT;
        v_cycle := NVL(rec.STARTCYC, 202501);
        v_workdays_remaining := NVL(rec.WORKDAYS, rec.WEEKS * 5);
        v_total_weeks := rec.WEEKS;
        
        -- Calculate month-level HOLIDAYS and HOURS
        -- HOLIDAYS = Total possible workdays (WEEKS * 5) - Actual WORKDAYS
        v_month_holidays := (rec.WEEKS * 5) - NVL(rec.WORKDAYS, rec.WEEKS * 5);
        
        -- HOURS = WORKDAYS * 8 hours per day
        v_month_hours := NVL(rec.WORKDAYS, rec.WEEKS * 5) * 8;
        
        -- Generate WEEK_DATA JSON
        FOR i IN 1..rec.WEEKS LOOP
            v_week_end := v_week_start + 6;
            
            IF i = rec.WEEKS THEN
                v_week_workdays := v_workdays_remaining;
            ELSE
                v_week_workdays := LEAST(5, v_workdays_remaining);
            END IF;
            
            v_workdays_remaining := v_workdays_remaining - v_week_workdays;
            v_week_holidays := GREATEST(0, 5 - v_week_workdays);
            v_week_hours := v_week_workdays * 8;
            
            v_week_json := '{' ||
                '"cycle":' || v_cycle || ',' ||
                '"weekNumber":' || i || ',' ||
                '"startDate":"' || TO_CHAR(v_week_start, 'YYYY-MM-DD') || '",' ||
                '"endDate":"' || TO_CHAR(v_week_end, 'YYYY-MM-DD') || '",' ||
                '"dateRange":"' || TRIM(TO_CHAR(v_week_start, 'Month')) || ' ' || TO_CHAR(v_week_start, 'DD') || ' - ' || TRIM(TO_CHAR(v_week_end, 'Month')) || ' ' || TO_CHAR(v_week_end, 'DD') || '",' ||
                '"workdays":' || v_week_workdays || ',' ||
                '"holidays":' || v_week_holidays || ',' ||
                '"hours":' || v_week_hours ||
                '}';
            
            IF i < rec.WEEKS THEN
                v_week_data := v_week_data || v_week_json || ',';
            ELSE
                v_week_data := v_week_data || v_week_json;
            END IF;
            
            v_week_start := v_week_start + 7;
            v_cycle := v_cycle + 1;
        END LOOP;
        
        v_week_data := v_week_data || ']';
        
        -- Update all three columns
        UPDATE ENTMONTH
        SET WEEK_DATA = v_week_data,
            HOLIDAYS = v_month_holidays,
            HOURS = v_month_hours
        WHERE RPTMONTH = rec.RPTMONTH;
        
        v_row_count := v_row_count + 1;
        
        DBMS_OUTPUT.PUT_LINE('Updated: ' || RPAD(rec.RPTMONTH, 10) || 
                           ' | Weeks: ' || rec.WEEKS || 
                           ' | Workdays: ' || rec.WORKDAYS ||
                           ' | Holidays: ' || v_month_holidays ||
                           ' | Hours: ' || v_month_hours);
        
    END LOOP;
    
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('=============================================================');
    DBMS_OUTPUT.PUT_LINE('Complete! Updated ' || v_row_count || ' rows.');
    DBMS_OUTPUT.PUT_LINE('=============================================================');
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('Columns updated: WEEK_DATA, HOLIDAYS, HOURS');
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('Next: Run verification query, then COMMIT or ROLLBACK');
    
END;
/


-- ============================================================================
-- VERIFICATION QUERY - Run this after executing the script above
-- ============================================================================
/*
SELECT 
    RPTMONTH,
    STARTDT,
    ENDDT,
    WEEKS,
    WORKDAYS,
    HOLIDAYS,
    HOURS,
    LENGTH(WEEK_DATA) AS WEEK_DATA_LEN
FROM ENTMONTH
ORDER BY STARTDT;
*/

-- ============================================================================
-- Expected Results for FY2025:
-- ============================================================================
-- RPTMONTH   STARTDT     WEEKS  WORKDAYS  HOLIDAYS  HOURS  WEEK_DATA_LEN
-- ---------  ----------  -----  --------  --------  -----  -------------
-- OCT2025    9/28/2025      4       19         1     152         ~450
-- NOV2025    10/26/2025     4       19         1     152         ~450
-- DEC2025    11/23/2025     5       21         4     168         ~550
-- JAN2025    12/29/2024     4       17         3     136         ~450
-- FEB2025    1/26/2025      4       19         1     152         ~450
-- MAR2025    2/23/2025      5       25         0     200         ~550
-- APR2025    3/30/2025      4       20         0     160         ~450
-- MAY2025    4/27/2025      4       20         0     160         ~450
-- JUN2025    5/25/2025      5       23         2     184         ~550
-- JUL2025    6/29/2025      4       19         1     152         ~450
-- AUG2025    7/27/2025      4       20         0     160         ~450
-- SEP2025    8/24/2025      5       24         1     192         ~550
-- ============================================================================
