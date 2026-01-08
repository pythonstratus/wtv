package com.entity.wtv.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * ENTMONTH - Pay Period / Reporting Month Reference Table
 * 
 * Source: ENTITYDEV.ENTMONTH
 * Primary Key: RPTMONTH (e.g., "OCT2019", "NOV2019")
 * 
 * Used in:
 * - WTV for Reporting Month dropdown and week selection
 * - CTRS Calendar for fiscal year management
 * 
 * Format: RPTMONTH = MMMyyyy (e.g., "OCT2026", "NOV2026")
 * Fiscal Year runs October to September
 */
@Entity
@Table(name = "ENTMONTH")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Entmonth {

    /**
     * Reporting Month identifier - Primary Key
     * Format: "MMMyyyy" (e.g., "OCT2026", "NOV2026")
     */
    @Id
    @Column(name = "RPTMONTH", length = 7)
    private String rptmonth;

    /**
     * Start date of the pay period (Sunday)
     */
    @Column(name = "STARTDT")
    private LocalDate startdt;

    /**
     * End date of the pay period (Saturday)
     */
    @Column(name = "ENDDT")
    private LocalDate enddt;

    /**
     * Number of weeks in the period (4 or 5)
     */
    @Column(name = "WEEKS")
    private Integer weeks;

    /**
     * Start cycle number (e.g., 202501)
     * Format: yyyyww where ww is week number
     */
    @Column(name = "STARTCYC")
    private Integer startcyc;

    /**
     * End cycle number (e.g., 202505)
     */
    @Column(name = "ENDCYC")
    private Integer endcyc;

    /**
     * Number of workdays in the period (typically 20 for 4 weeks, 25 for 5 weeks)
     */
    @Column(name = "WORKDAYS")
    private Integer workdays;

    /**
     * National reporting date (legacy - typically 1900-01-01)
     */
    @Column(name = "RPTNATIONAL")
    private LocalDate rptnational;

    /**
     * Active status - 'Y' for active, 'N' for inactive
     * Only one version of a fiscal year should be active
     */
    @Column(name = "ACTIVE", length = 1)
    @Builder.Default
    private String active = "Y";

    /**
     * Total holidays for this month (new field for CTRS Calendar)
     */
    @Column(name = "HOLIDAYS")
    @Builder.Default
    private Integer holidays = 0;

    /**
     * Total hours for this month (calculated or override)
     */
    @Column(name = "HOURS")
    private Integer hours;

    /**
     * Week-level data stored as JSON (for holidays/hours per week)
     * Format: [{"cycle":202501,"workdays":5,"holidays":0,"hours":40}, ...]
     */
    @Column(name = "WEEK_DATA", length = 1000)
    private String weekData;

    // =========================================================================
    // Helper Methods
    // =========================================================================

    /**
     * Extract fiscal year from RPTMONTH
     * e.g., "OCT2026" -> 2027 (FY2027), "JAN2026" -> 2026 (FY2026)
     * 
     * Note: OCT, NOV, DEC of year YYYY belong to FY(YYYY+1)
     */
    public Integer getFiscalYear() {
        if (rptmonth == null || rptmonth.length() < 7) return null;
        try {
            int calendarYear = Integer.parseInt(rptmonth.substring(3));
            String monthAbbrev = rptmonth.substring(0, 3).toUpperCase();
            // OCT, NOV, DEC belong to the NEXT fiscal year
            if ("OCT".equals(monthAbbrev) || "NOV".equals(monthAbbrev) || "DEC".equals(monthAbbrev)) {
                return calendarYear + 1;
            }
            return calendarYear;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Extract month abbreviation from RPTMONTH
     * e.g., "OCT2026" -> "OCT"
     */
    public String getMonthAbbrev() {
        if (rptmonth == null || rptmonth.length() < 3) return null;
        return rptmonth.substring(0, 3);
    }

    /**
     * Get full month name
     */
    public String getMonthName() {
        String abbrev = getMonthAbbrev();
        if (abbrev == null) return null;
        return switch (abbrev.toUpperCase()) {
            case "JAN" -> "January";
            case "FEB" -> "February";
            case "MAR" -> "March";
            case "APR" -> "April";
            case "MAY" -> "May";
            case "JUN" -> "June";
            case "JUL" -> "July";
            case "AUG" -> "August";
            case "SEP" -> "September";
            case "OCT" -> "October";
            case "NOV" -> "November";
            case "DEC" -> "December";
            default -> abbrev;
        };
    }

    /**
     * Get formatted display string for the period dates
     */
    public String getDisplayPeriod() {
        if (startdt != null && enddt != null) {
            return String.format("%s - %s", startdt.toString(), enddt.toString());
        }
        return rptmonth;
    }

    /**
     * Check if a date falls within this pay period
     */
    public boolean containsDate(LocalDate date) {
        if (startdt == null || enddt == null || date == null) {
            return false;
        }
        return !date.isBefore(startdt) && !date.isAfter(enddt);
    }

    /**
     * Check if this month is active
     */
    public boolean isActive() {
        return "Y".equals(active);
    }

    /**
     * Set active status
     */
    public void setActiveStatus(boolean isActive) {
        this.active = isActive ? "Y" : "N";
    }

    /**
     * Calculate hours (workdays * 8 if not explicitly set)
     */
    public Integer getCalculatedHours() {
        if (hours != null) {
            return hours;
        }
        return workdays != null ? workdays * 8 : null;
    }
}
