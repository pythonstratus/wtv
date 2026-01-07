package com.entity.wtv.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for creating a new Fiscal Year
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFiscalYearRequest {

    /**
     * Fiscal year to create (e.g., 2027)
     */
    @NotNull(message = "Fiscal year is required")
    @Min(value = 2000, message = "Fiscal year must be 2000 or later")
    @Max(value = 2100, message = "Fiscal year must be before 2100")
    private Integer fiscalYear;

    /**
     * Start date of the fiscal year (first Sunday of October period)
     * If not provided, will be calculated
     */
    private LocalDate startDate;

    /**
     * Optional: Provide month details if not auto-generating
     * If empty, months will be auto-generated
     */
    private List<MonthInput> months;

    /**
     * Inner class for optional month input
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthInput {
        private String monthAbbrev;  // e.g., "OCT", "NOV"
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer weeks;
        private Integer workdays;
    }
}
