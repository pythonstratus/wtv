package com.entity.wtv.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * ENTCODE - Time Code Reference Table
 * 
 * Source: ENTITYDEV.ENTCODE (14 columns)
 * Primary Key: CODE + TYPE (composite)
 * 
 * Used in WTV for:
 * - Time code descriptions (CDNAME)
 * - Time definition categorization (TIMEDEF)
 * - Filtering active codes (ACTIVE in 'Y','C')
 * 
 * TIMEDEF Values (from legacy Pro*C analysis):
 * - M, U, C, G, N, R, O, E = Tour/Duty related hours
 * - A = Adjustment hours (negated in calculations)
 * - S = Schedule hours
 * - I = Info/Other (special handling for code 760)
 * 
 * TIMEDEF Groupings for calculations:
 * - Tour of Duty: M, U, C, G, N, R, O, E
 * - Code Direct: G, M, C, U, N, E
 * - Overhead: O, R
 * - Adjustment: A, S
 */
@Entity
@Table(name = "ENTCODE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(EntcodeId.class)
public class Entcode {

    /**
     * Time code (3 characters)
     * Examples: ABCD, EFGH, IJKL, etc.
     */
    @Id
    @Column(name = "CODE", length = 3)
    private String code;

    /**
     * Code type
     * 'T' = Time code
     */
    @Id
    @Column(name = "TYPE", length = 1)
    private String type;

    /**
     * Code description/name for display
     * Max 35 characters
     */
    @Column(name = "CDNAME", length = 35)
    private String cdname;

    @Column(name = "AREA")
    private Integer area;

    @Column(name = "EXTRDT")
    private LocalDate extrdt;

    /**
     * Active status
     * 'Y' = Yes (active)
     * 'C' = Current (active)
     */
    @Column(name = "ACTIVE", length = 1)
    private String active;

    @Column(name = "MGR", length = 1)
    private String mgr;

    @Column(name = "CLERK", length = 1)
    private String clerk;

    @Column(name = "PROF", length = 1)
    private String prof;

    @Column(name = "PARA", length = 1)
    private String para;

    @Column(name = "DISP", length = 1)
    private String disp;

    /**
     * Time definition category
     * M, U, C, G, N, R, O, E = Tour/Duty
     * A = Adjustment (negated)
     * S = Schedule
     * I = Info/Other
     */
    @Column(name = "TIMEDEF", length = 1)
    private String timedef;

    @Column(name = "CTRSDEF")
    private Integer ctrsdef;

    @Column(name = "CTRSLN")
    private Integer ctrsln;

    // =========================================================================
    // Helper Methods
    // =========================================================================

    /**
     * Check if code is active
     */
    public boolean isActive() {
        return "Y".equals(active) || "C".equals(active);
    }

    /**
     * Check if this is a Tour/Duty related code
     */
    public boolean isTourDutyCode() {
        return timedef != null && "MUCGNROE".contains(timedef);
    }

    /**
     * Check if this is a Code Direct code
     */
    public boolean isCodeDirectCode() {
        return timedef != null && "GMCUNE".contains(timedef);
    }

    /**
     * Check if this is an Overhead code
     */
    public boolean isOverheadCode() {
        return timedef != null && "OR".contains(timedef);
    }

    /**
     * Check if this is an Adjustment code
     */
    public boolean isAdjustmentCode() {
        return "A".equals(timedef);
    }

    /**
     * Check if this is a Schedule code
     */
    public boolean isScheduleCode() {
        return "S".equals(timedef);
    }

    /**
     * Get truncated display name (12 chars as per legacy)
     */
    public String getDisplayName() {
        if (cdname == null) return "";
        return cdname.length() > 12 ? cdname.substring(0, 12) : cdname;
    }
}
