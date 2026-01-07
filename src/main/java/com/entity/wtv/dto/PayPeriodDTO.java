package com.entity.wtv.dto;

import lombok.*;
import java.time.LocalDate;

/**
 * DTO for Pay Period Navigation
 * 
 * Used for Previous/Next week navigation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayPeriodDTO {

    /**
     * Pay period identifier
     */
    private Long id;

    /**
     * Pay period number
     */
    private Integer periodNumber;

    /**
     * Week number within the pay period
     */
    private Integer weekNumber;

    /**
     * Posting cycle code
     */
    private Integer postingCycle;

    /**
     * Start date of the week (Sunday)
     */
    private LocalDate startDate;

    /**
     * End date of the week (Saturday)
     */
    private LocalDate endDate;

    /**
     * Reporting month
     */
    private String reportingMonth;

    /**
     * Display label
     */
    private String displayLabel;

    /**
     * Previous pay period ID (for navigation)
     */
    private Long previousPayPeriodId;

    /**
     * Next pay period ID (for navigation)
     */
    private Long nextPayPeriodId;

    /**
     * Has previous week available
     */
    @Builder.Default
    private boolean hasPrevious = false;

    /**
     * Has next week available
     */
    @Builder.Default
    private boolean hasNext = false;

    // =========================================================================
    // Helper Methods
    // =========================================================================

    /**
     * Get display string for the period
     */
    public String getDisplayPeriod() {
        if (startDate == null || endDate == null) return displayLabel;
        return String.format("Pay Period %d Week %d: %s - %s",
            periodNumber != null ? periodNumber : 0,
            weekNumber != null ? weekNumber : 0,
            startDate.toString(),
            endDate.toString());
    }

    /**
     * Calculate previous week dates (subtract 7 days)
     */
    public PayPeriodDTO calculatePreviousWeek() {
        if (startDate == null) return null;
        return PayPeriodDTO.builder()
                .startDate(startDate.minusDays(7))
                .endDate(endDate != null ? endDate.minusDays(7) : startDate.minusDays(1))
                .build();
    }

    /**
     * Calculate next week dates (add 7 days)
     */
    public PayPeriodDTO calculateNextWeek() {
        if (startDate == null) return null;
        return PayPeriodDTO.builder()
                .startDate(startDate.plusDays(7))
                .endDate(endDate != null ? endDate.plusDays(7) : startDate.plusDays(13))
                .build();
    }
}
