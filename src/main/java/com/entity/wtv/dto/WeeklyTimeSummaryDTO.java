package com.entity.wtv.dto;

import lombok.*;
import java.math.BigDecimal;

/**
 * DTO for Group Weekly Hours Verification main table
 * 
 * Maps to the 10 columns displayed in the group view:
 * ROID, DUTY, ADJUST, WORKED, DIRECT, CODE, OVRHD, DAYS, TOD, RPTDAY
 * 
 * Legacy source: getTimeVerifyData() in entity_common.pc
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyTimeSummaryDTO {

    /**
     * Assignment Number (ROID)
     * Column 1: Assignment #
     */
    private Long assignmentNumber;

    /**
     * Employee Name (from ENTEMP.NAME)
     * For display in detail view header
     */
    private String employeeName;

    /**
     * Tour of Duty Hours
     * Column 2: Tour of Duty Hours
     * 
     * Calculation: SUM(TIMENON hours where TIMEDEF in M,U,C,G,N,R,O,E)
     *              + SUM(TIMETIN hours)
     *              - SUM(TIMENON hours where TIMEDEF='A')
     *              - SUM(TIMENON hours where TIMEDEF='S')
     */
    @Builder.Default
    private BigDecimal tourOfDutyHours = BigDecimal.ZERO;

    /**
     * Adjusted Tour
     * Column 3: Adjusted Tour
     * 
     * Calculation: SUM(TIMENON hours where TIMEDEF='A')
     *              - SUM(TIMENON hours where TIMEDEF='S')
     */
    @Builder.Default
    private BigDecimal adjustedTour = BigDecimal.ZERO;

    /**
     * Hours Worked
     * Column 4: Hours Worked
     * 
     * Calculation: SUM(TIMETIN hours)
     */
    @Builder.Default
    private BigDecimal hoursWorked = BigDecimal.ZERO;

    /**
     * Case Direct Time
     * Column 5: Case Direct Time
     * 
     * Calculation: SUM(TIMETIN hours)
     */
    @Builder.Default
    private BigDecimal caseDirectTime = BigDecimal.ZERO;

    /**
     * Code Direct Time
     * Column 6: Code Direct Time
     * 
     * Calculation: SUM(TIMENON hours where TIMEDEF in G,M,C,U,N,E)
     */
    @Builder.Default
    private BigDecimal codeDirectTime = BigDecimal.ZERO;

    /**
     * Overhead Time
     * Column 7: Overhead Time
     * 
     * Calculation: SUM(TIMENON hours where TIMEDEF in O,R)
     */
    @Builder.Default
    private BigDecimal overheadTime = BigDecimal.ZERO;

    /**
     * Report Days
     * Column 8: Report Days
     * 
     * Calculation: COUNT(DISTINCT rptdt from TIMENON where timecode != '750')
     *              + COUNT(DISTINCT rptdt from TIMETIN where not exists matching TIMENON)
     */
    @Builder.Default
    private Integer reportDays = 0;

    /**
     * Tour of Duty Type
     * Column 9: Tour of Duty Type
     * 
     * Values: REG, 5/4/9, 4/10, PT, MAXI, or '-'
     * From: ENTEMP.TOUR decoded
     */
    @Builder.Default
    private String tourOfDutyType = "-";

    /**
     * Last Date EOD
     * Column 10: Last Date EOD
     * 
     * Calculation: MAX(rptdt) from TIMETIN or TIMENON
     */
    private String lastDateEod;

    /**
     * Tour value (1-5) from ENTEMP for sorting/grouping
     */
    private Integer tour;
}
