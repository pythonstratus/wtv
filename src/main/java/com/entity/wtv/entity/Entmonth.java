package com.entity.wtv.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * ENTMONTH - Pay Period / Reporting Month Reference Table
 * 
 * Source: ENTITYDEV.ENTMONTH (8 columns)
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

    // =========================================================================
    // Helper Methods
    // =========================================================================

    /**
     * Extract fiscal year from RPTMONTH
     * e.g., "OCT2026" -> 2026, "JAN2026" -> 2026
     */
    public Integer getFiscalYear() {
        if (rptmonth == null || rptmonth.length() < 7) return null;
        try {
            return Integer.parseInt(rptmonth.substring(3));
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
     * Calculate hours (workdays * 8, but UI shows same as workdays)
     */
    public Integer getHours() {
        return workdays;
    }
}
