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
     * List of 12 fiscal months (October - September)
     */
    private List<FiscalMonthDTO> months;

    /**
     * Active status (if versioning is used)
     */
    private Boolean active;

    /**
     * Version identifier (e.g., "Active 10282025")
     */
    private String version;
}
