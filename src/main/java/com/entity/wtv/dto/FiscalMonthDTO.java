package com.entity.wtv.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for a single Fiscal Month in CTRS Calendar
 * 
 * Maps to ENTMONTH table record with expanded week details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiscalMonthDTO {

    /**
     * RPTMONTH value (e.g., "OCT2025")
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
     * Total workdays for the month (sum of all weeks)
     */
    private Integer workdays;

    /**
     * Total holidays for the month (sum of all weeks)
     */
    private Integer holidays;

    /**
     * Total hours for the month (sum of all weeks)
     */
    private Integer hours;

    /**
     * Start cycle number (e.g., 202501)
     */
    private Integer startCycle;

    /**
     * End cycle number (e.g., 202504 or 202505)
     */
    private Integer endCycle;

    /**
     * Individual weeks/posting cycles within this month
     */
    private List<WeekCycleDTO> weeks;

    /**
     * National date (legacy field)
     */
    private LocalDate rptNational;

    /**
     * Whether this month can be expanded to show weeks
     */
    @Builder.Default
    private Boolean expandable = true;

    /**
     * Inner class for week/posting cycle details
     * Each week represents one posting cycle (e.g., 202501, 202502)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeekCycleDTO {
        
        /**
         * Posting cycle number (e.g., 202501, 202502)
         */
        private Integer cycleNumber;

        /**
         * Week number within the month (1, 2, 3, 4, or 5)
         */
        private Integer weekNumber;

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
         * Workdays in this week (typically 5, can be less due to holidays)
         */
        private Integer workdays;

        /**
         * Holidays in this week (0, 1, or more)
         */
        private Integer holidays;

        /**
         * Hours for this week (workdays * 8, or custom value)
         */
        private Integer hours;
    }
}
