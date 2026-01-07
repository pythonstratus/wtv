package com.entity.wtv.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

/**
 * DTO for Reporting Month with nested Weeks
 * 
 * Used to populate:
 * - Reporting Month dropdown
 * - Week dropdown (based on selected month)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportingMonthDTO {

    /**
     * Unique identifier for the month (RPTMONTH value, e.g., "OCT2024")
     */
    private String id;

    /**
     * Display label (e.g., "November 2023")
     */
    private String displayLabel;

    /**
     * Reporting month code (e.g., "2023-11")
     */
    private String rptmonth;

    /**
     * Month start date
     */
    private LocalDate startDate;

    /**
     * Month end date
     */
    private LocalDate endDate;

    /**
     * Fiscal year
     */
    private Integer fiscalYear;

    /**
     * Fiscal month name
     */
    private String fiscalMonth;

    /**
     * Number of weeks in this month
     */
    private Integer weekCount;

    /**
     * List of weeks within this month
     */
    @Builder.Default
    private List<WeekDTO> weeks = new ArrayList<>();

    // =========================================================================
    // Nested Week DTO
    // =========================================================================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WeekDTO {

        /**
         * Unique identifier for the week/pay period
         */
        private Long id;

        /**
         * Week number within the month (1, 2, 3, 4, 5)
         */
        private Integer weekNumber;

        /**
         * Posting cycle code (e.g., 202501)
         */
        private Integer postingCycle;

        /**
         * Week start date (Sunday)
         */
        private LocalDate startDate;

        /**
         * Week end date (Saturday)
         */
        private LocalDate endDate;

        /**
         * Display label (e.g., "10/29/2023 - 11/04/2023")
         */
        private String displayLabel;

        /**
         * Number of workdays in this week
         */
        private Integer workdays;

        /**
         * Number of holidays in this week
         */
        private Integer holidays;

        /**
         * Standard hours for this week
         */
        private Integer hours;

        /**
         * Generate display label from dates
         */
        public String generateDisplayLabel() {
            if (startDate == null || endDate == null) return "";
            return String.format("%d/%d/%d - %d/%d/%d",
                startDate.getMonthValue(), startDate.getDayOfMonth(), startDate.getYear(),
                endDate.getMonthValue(), endDate.getDayOfMonth(), endDate.getYear());
        }
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    /**
     * Add a week to this month
     */
    public void addWeek(WeekDTO week) {
        if (weeks == null) {
            weeks = new ArrayList<>();
        }
        weeks.add(week);
        weekCount = weeks.size();
    }

    /**
     * Get week by number
     */
    public WeekDTO getWeekByNumber(int weekNumber) {
        if (weeks == null) return null;
        return weeks.stream()
                .filter(w -> w.getWeekNumber() != null && w.getWeekNumber() == weekNumber)
                .findFirst()
                .orElse(null);
    }

    /**
     * Get week containing a specific date
     */
    public WeekDTO getWeekContainingDate(LocalDate date) {
        if (weeks == null || date == null) return null;
        return weeks.stream()
                .filter(w -> w.getStartDate() != null && w.getEndDate() != null)
                .filter(w -> !date.isBefore(w.getStartDate()) && !date.isAfter(w.getEndDate()))
                .findFirst()
                .orElse(null);
    }
}
