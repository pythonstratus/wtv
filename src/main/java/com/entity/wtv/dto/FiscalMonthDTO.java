package com.entity.wtv.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for a single Fiscal Month in CTRS Calendar
 * 
 * Maps to ENTMONTH table record
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiscalMonthDTO {

    /**
     * RPTMONTH value (e.g., "OCT2026")
     */
    private String rptMonth;

    /**
     * Month name for display (e.g., "October")
     */
    private String monthName;

    /**
     * Number of posting cycles/weeks (4 or 5)
     */
    private Integer postingCycles;

    /**
     * Start date of the month period (Sunday)
     */
    private LocalDate startDate;

    /**
     * End date of the month period (Saturday)
     */
    private LocalDate endDate;

    /**
     * Display format for dates (e.g., "Sep 29 - Oct 26")
     */
    private String dateRange;

    /**
     * Number of workdays
     */
    private Integer workdays;

    /**
     * Number of holidays (calculated or stored)
     */
    private Integer holidays;

    /**
     * Hours (typically same as workdays in UI)
     */
    private Integer hours;

    /**
     * Start cycle number (e.g., 202601)
     */
    private Integer startCycle;

    /**
     * End cycle number (e.g., 202604)
     */
    private Integer endCycle;

    /**
     * Individual weeks within this month
     */
    private List<WeekCycleDTO> weeks;

    /**
     * National date (legacy field)
     */
    private LocalDate rptNational;

    /**
     * Inner class for week/cycle details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeekCycleDTO {
        /**
         * Cycle number (e.g., 202601)
         */
        private Integer cycleNumber;

        /**
         * Week start date (Sunday)
         */
        private LocalDate startDate;

        /**
         * Week end date (Saturday)
         */
        private LocalDate endDate;

        /**
         * Display format (e.g., "September 29 - October 3")
         */
        private String dateRange;

        /**
         * Workdays in this week (typically 5)
         */
        private Integer workdays;
    }
}
