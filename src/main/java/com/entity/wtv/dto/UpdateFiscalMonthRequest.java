package com.entity.wtv.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for updating a Fiscal Month
 * 
 * Supports both month-level and individual week-level updates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFiscalMonthRequest {

    /**
     * RPTMONTH identifier (for bulk updates)
     */
    private String rptMonth;

    /**
     * Start date of the month period (Sunday)
     */
    private LocalDate startDate;

    /**
     * End date of the month period (Saturday)
     */
    private LocalDate endDate;

    /**
     * Number of weeks (4 or 5)
     */
    @Min(value = 4, message = "Weeks must be 4 or 5")
    @Max(value = 5, message = "Weeks must be 4 or 5")
    private Integer weeks;

    /**
     * Start cycle number
     */
    private Integer startCycle;

    /**
     * End cycle number
     */
    private Integer endCycle;

    /**
     * Total workdays for the month
     */
    @Min(value = 1, message = "Workdays must be at least 1")
    @Max(value = 31, message = "Workdays cannot exceed 31")
    private Integer workdays;

    /**
     * Total holidays for the month
     */
    @Min(value = 0, message = "Holidays cannot be negative")
    @Max(value = 10, message = "Holidays cannot exceed 10")
    private Integer holidays;

    /**
     * Total hours for the month
     */
    @Min(value = 0, message = "Hours cannot be negative")
    private Integer hours;

    /**
     * Individual week updates (optional)
     * If provided, updates specific weeks within the month
     */
    private List<WeekUpdateDTO> weekUpdates;

    /**
     * Inner class for individual week updates
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeekUpdateDTO {
        
        /**
         * Posting cycle number to update (e.g., 202501)
         */
        @NotNull(message = "Cycle number is required")
        private Integer cycleNumber;

        /**
         * Workdays for this week
         */
        @Min(value = 0, message = "Workdays cannot be negative")
        @Max(value = 7, message = "Workdays cannot exceed 7")
        private Integer workdays;

        /**
         * Holidays for this week
         */
        @Min(value = 0, message = "Holidays cannot be negative")
        @Max(value = 7, message = "Holidays cannot exceed 7")
        private Integer holidays;

        /**
         * Hours for this week
         */
        @Min(value = 0, message = "Hours cannot be negative")
        private Integer hours;

        /**
         * Start date override (rarely used)
         */
        private LocalDate startDate;

        /**
         * End date override (rarely used)
         */
        private LocalDate endDate;
    }
}
