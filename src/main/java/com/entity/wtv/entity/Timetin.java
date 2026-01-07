package com.entity.wtv.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * TIMETIN - Case/TIN Time Entries Table
 * 
 * Source: ENTITYDEV.TIMETIN (19 columns)
 * Composite Index: TIMESID, RPTDT, ROID, CODE, SUBCODE, EXTRDT
 * 
 * Used in WTV for:
 * - Case-specific time tracking (hours per TIN/taxpayer)
 * - Group view Hours Worked and Case Direct Time calculations
 * - Employee detail Case TIN table
 * 
 * Joins to ENT table via: TIMETIN.TIMESID = ENT.TINSID
 * to get TIN, TINTT (TIN Type), TPCTRL (Taxpayer Control/Name)
 */
@Entity
@Table(name = "TIMETIN")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Timetin {

    /**
     * Time Session ID (links to ENT.TINSID)
     */
    @Id
    @Column(name = "TIMESID")
    private Long timesid;

    /**
     * Report date
     */
    @Column(name = "RPTDT")
    private LocalDate rptdt;

    /**
     * Assignment ROID (FK to ENTEMP)
     */
    @Column(name = "ROID")
    private Long roid;

    /**
     * Control code
     */
    @Column(name = "CONTCD", length = 1)
    private String contcd;

    /**
     * Code
     */
    @Column(name = "CODE", length = 3)
    private String code;

    /**
     * Subcode
     */
    @Column(name = "SUBCODE", length = 3)
    private String subcode;

    /**
     * Hours worked (4 digits, 2 decimal places)
     */
    @Column(name = "HOURS", precision = 4, scale = 2)
    private BigDecimal hours;

    /**
     * Grade
     */
    @Column(name = "GRADE")
    private Integer grade;

    /**
     * Exit date
     */
    @Column(name = "EXTRDT")
    private LocalDate extrdt;

    /**
     * BOD code
     */
    @Column(name = "BODCD", length = 2)
    private String bodcd;

    /**
     * BOD class code
     */
    @Column(name = "BODCLCD", length = 3)
    private String bodclcd;

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

    /**
     * Segment indicator
     */
    @Column(name = "SEGIND", length = 1)
    private String segind;

    /**
     * TDA count
     */
    @Column(name = "TDACNT")
    private Integer tdacnt;

    /**
     * TDI count
     */
    @Column(name = "TDICNT")
    private Integer tdicnt;

    /**
     * Risk level
     */
    @Column(name = "RISK")
    private Integer risk;

    /**
     * Program name 1
     */
    @Column(name = "PRGNAME1", length = 40)
    private String prgname1;

    /**
     * Program name 2
     */
    @Column(name = "PRGNAME2", length = 40)
    private String prgname2;

    // =========================================================================
    // Helper Methods
    // =========================================================================

    /**
     * Get hours as double for calculations
     */
    public double getHoursAsDouble() {
        return hours != null ? hours.doubleValue() : 0.0;
    }
}
