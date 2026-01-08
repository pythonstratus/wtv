package com.entity.wtv.dto;

import lombok.*;
import java.util.List;

/**
 * DTO for Fiscal Year overview in CTRS Calendar
 * 
 * A fiscal year runs from October to September
 * e.g., FY2026 = October 2025 - September 2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiscalYearDTO {

    /**
     * Fiscal year number (e.g., 2026)
     */
    private Integer fiscalYear;

    /**
     * Display label (e.g., "FY 2026" or "2026")
     */
    private String displayLabel;

    /**
     * Total weeks in the fiscal year (typically 52)
     */
    private Integer totalWeeks;

    /**
     * Total workdays in the fiscal year
     */
    private Integer totalWorkdays;

    /**
     * Total holidays in the fiscal year
     */
    private Integer totalHolidays;

    /**
     * Total hours in the fiscal year
     */
    private Integer totalHours;

    /**
     * List of 12 fiscal months (October - September)
     */
    private List<FiscalMonthDTO> months;

    /**
     * Active status - only one fiscal year version can be active
     */
    private Boolean active;

    /**
     * Status label for display (e.g., "Active", "Inactive")
     */
    private String status;
}
