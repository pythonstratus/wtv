package com.entity.wtv.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * TIMENON - Non-Case Time Entries Table
 * 
 * Source: ENTITYDEV.TIMENON (9 columns)
 * Composite Index: RPTDT, ROID, TIMECODE, TIMEDEF
 * 
 * Used in WTV for:
 * - Non-case time tracking (overhead, admin, leave, etc.)
 * - Group view calculations (Tour of Duty, Adjusted Tour, Code Direct, Overhead)
 * - Employee detail Non-Case Time table
 * 
 * Key calculations from legacy Pro*C:
 * - Tour of Duty Hours: SUM where TIMEDEF in ('M','U','C','G','N','R','O','E')
 * - Adjusted Tour: TIMEDEF='A' hours - TIMEDEF='S' hours
 * - Code Direct: SUM where TIMEDEF in ('G','M','C','U','N','E')
 * - Overhead: SUM where TIMEDEF in ('O','R')
 * 
 * Special handling:
 * - Timecode 750: Excluded from Report Days count
 * - Timecode 760: Uses binary presence indicator (decode(hours,0,1,0))
 */
@Entity
@Table(name = "TIMENON")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(TimenonId.class)
public class Timenon {

    /**
     * Report date
     */
    @Id
    @Column(name = "RPTDT")
    private LocalDate rptdt;

    /**
     * Assignment ROID (FK to ENTEMP)
     */
    @Id
    @Column(name = "ROID")
    private Long roid;

    /**
     * Time code (FK to ENTCODE)
     */
    @Id
    @Column(name = "TIMECODE", length = 3)
    private String timecode;

    /**
     * Control code
     */
    @Column(name = "CONTCD", length = 1)
    private String contcd;

    /**
     * Time definition (from ENTCODE, denormalized)
     */
    @Column(name = "TIMEDEF", length = 1)
    private String timedef;

    /**
     * Hours worked (4 digits, 2 decimal places)
     */
    @Column(name = "HOURS", precision = 4, scale = 2)
    private BigDecimal hours;

    /**
     * Exit date
     */
    @Column(name = "EXTRDT")
    private LocalDate extrdt;

    /**
     * Employee ID number
     */
    @Column(name = "EMPIDNUM", length = 10)
    private String empidnum;

    /**
     * Late flag
     */
    @Column(name = "LATEFLAG", length = 1)
    private String lateflag;

    // =========================================================================
    // Helper Methods
    // =========================================================================

    /**
     * Get hours as double for calculations
     */
    public double getHoursAsDouble() {
        return hours != null ? hours.doubleValue() : 0.0;
    }

    /**
     * Check if this is a Tour/Duty related entry
     */
    public boolean isTourDutyEntry() {
        return timedef != null && "MUCGNROE".contains(timedef);
    }

    /**
     * Check if this is an Adjustment entry (TIMEDEF = 'A')
     * Note: Adjustment hours are NEGATED in calculations
     */
    public boolean isAdjustmentEntry() {
        return "A".equals(timedef);
    }

    /**
     * Check if this is a Schedule entry (TIMEDEF = 'S')
     */
    public boolean isScheduleEntry() {
        return "S".equals(timedef);
    }

    /**
     * Check if this is Code Direct entry
     */
    public boolean isCodeDirectEntry() {
        return timedef != null && "GMCUNE".contains(timedef);
    }

    /**
     * Check if this is Overhead entry
     */
    public boolean isOverheadEntry() {
        return timedef != null && "OR".contains(timedef);
    }

    /**
     * TODO: Revisit - Timecode 750 is excluded from Report Days count.
     * Need clarification from Samuel on what this code represents.
     */
    public boolean isExcludedFromDaysCount() {
        return "750".equals(timecode);
    }

    /**
     * TODO: Revisit - Timecode 760 uses special binary logic.
     * Returns 1 if hours=0, else 0. Need clarification from Samuel.
     */
    public int getCode760BinaryValue() {
        if ("760".equals(timecode)) {
            return (hours == null || hours.compareTo(BigDecimal.ZERO) == 0) ? 1 : 0;
        }
        return 0;
    }
}
