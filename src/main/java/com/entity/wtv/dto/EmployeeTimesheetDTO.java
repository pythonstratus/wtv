package com.entity.wtv.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;

/**
 * DTO for Complete Employee Timesheet Detail Response
 * 
 * Contains all three tables for the employee drill-down view:
 * 1. Daily Summary (Tour, Holiday, Credit, Worked)
 * 2. Case TIN entries
 * 3. Non-Case Time entries
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeTimesheetDTO {

    // =========================================================================
    // Header Information
    // =========================================================================

    /**
     * Assignment Number (ROID)
     */
    private Long assignmentNumber;

    /**
     * Employee Name (from ENTEMP.NAME)
     */
    private String employeeName;

    /**
     * Week start date (Sunday)
     */
    private LocalDate weekStartDate;

    /**
     * Week end date (Saturday)
     */
    private LocalDate weekEndDate;

    /**
     * Reporting month
     */
    private String reportingMonth;

    /**
     * Day labels with dates for column headers
     * e.g., ["Sunday 10/29", "Monday 10/30", ...]
     */
    @Builder.Default
    private List<String> dayLabels = new ArrayList<>();

    // =========================================================================
    // Table 1: Daily Summary
    // =========================================================================

    /**
     * Daily summary rows (Tour, Holiday, Credit, Worked)
     */
    @Builder.Default
    private List<DailySummaryDTO> dailySummary = new ArrayList<>();

    // =========================================================================
    // Table 2: Case TIN
    // =========================================================================

    /**
     * Case time entries by TIN
     */
    @Builder.Default
    private List<CaseTimeEntryDTO> caseTimeEntries = new ArrayList<>();

    /**
     * Total Direct Case Time (sum of all case entries)
     */
    @Builder.Default
    private BigDecimal totalDirectCaseTime = BigDecimal.ZERO;

    // =========================================================================
    // Table 3: Non-Case Time
    // =========================================================================

    /**
     * Non-case time entries by time code
     */
    @Builder.Default
    private List<NonCaseTimeEntryDTO> nonCaseTimeEntries = new ArrayList<>();

    /**
     * Total Non Credit Direct Case Time
     */
    @Builder.Default
    private BigDecimal totalNonCreditDirectCaseTime = BigDecimal.ZERO;

    // =========================================================================
    // Helper Methods
    // =========================================================================

    /**
     * Initialize day labels based on week start date
     */
    public void initializeDayLabels() {
        if (weekStartDate == null) return;
        
        dayLabels = new ArrayList<>();
        String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStartDate.plusDays(i);
            String label = String.format("%s %d/%d", 
                dayNames[i], 
                date.getMonthValue(), 
                date.getDayOfMonth());
            dayLabels.add(label);
        }
    }

    /**
     * Calculate total direct case time from entries
     */
    public void calculateTotalDirectCaseTime() {
        if (caseTimeEntries == null || caseTimeEntries.isEmpty()) {
            totalDirectCaseTime = BigDecimal.ZERO;
            return;
        }
        totalDirectCaseTime = caseTimeEntries.stream()
                .map(CaseTimeEntryDTO::getTotalHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate total non-credit direct case time from entries
     */
    public void calculateTotalNonCreditDirectCaseTime() {
        if (nonCaseTimeEntries == null || nonCaseTimeEntries.isEmpty()) {
            totalNonCreditDirectCaseTime = BigDecimal.ZERO;
            return;
        }
        totalNonCreditDirectCaseTime = nonCaseTimeEntries.stream()
                .map(NonCaseTimeEntryDTO::getTotalHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get week range as display string
     */
    public String getWeekRange() {
        if (weekStartDate == null || weekEndDate == null) return "";
        return String.format("%s - %s", 
            weekStartDate.toString(), 
            weekEndDate.toString());
    }
}
