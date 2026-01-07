package com.entity.wtv.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Request DTO for updating a Fiscal Month
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFiscalMonthRequest {

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
     * Number of workdays
     */
    @Min(value = 1, message = "Workdays must be at least 1")
    @Max(value = 31, message = "Workdays cannot exceed 31")
    private Integer workdays;
}
