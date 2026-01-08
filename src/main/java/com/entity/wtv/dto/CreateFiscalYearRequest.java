package com.entity.wtv.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Request DTO for creating a new Fiscal Year
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFiscalYearRequest {

    /**
     * Fiscal year to create (e.g., 2026)
     * FY2026 = October 2025 - September 2026
     */
    @NotNull(message = "Fiscal year is required")
    @Min(value = 2000, message = "Fiscal year must be 2000 or later")
    @Max(value = 2100, message = "Fiscal year must be 2100 or earlier")
    private Integer fiscalYear;

    /**
     * Optional start date override
     * If not provided, calculates first Sunday on or before Oct 1
     */
    private LocalDate startDate;

    /**
     * If true, creates empty fiscal year structure without auto-generating dates
     * User will need to manually enter all data
     * Default: false (auto-generate)
     */
    @Builder.Default
    private Boolean empty = false;

    /**
     * If true, copies structure from previous fiscal year
     * Useful when patterns are similar year to year
     */
    @Builder.Default
    private Boolean copyFromPrevious = false;

    /**
     * Source fiscal year to copy from (if copyFromPrevious is true)
     */
    private Integer sourceYear;
}
