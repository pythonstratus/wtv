package com.entity.wtv.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ENT - Case/TIN Master Table
 * 
 * Source: ENTITYDEV.ENT (80+ columns)
 * Primary Key: TINSID
 * 
 * Used in WTV for:
 * - TIN (Taxpayer ID Number) display
 * - TINTT (TIN Type) for formatting
 * - TPCTRL (Taxpayer Control/Name) display
 * - TP (Taxpayer Name) display
 * 
 * Join: TIMETIN.TIMESID = ENT.TINSID
 * 
 * Note: Only essential columns for WTV are mapped here.
 * The full table has 80+ columns for case management.
 */
@Entity
@Table(name = "ENT")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ent {

    /**
     * TIN Session ID (Primary Key)
     * Links to TIMETIN.TIMESID
     */
    @Id
    @Column(name = "TINSID")
    private Long tinsid;

    /**
     * Exit date
     */
    @Column(name = "EXTRDT")
    private LocalDate extrdt;

    /**
     * Taxpayer Identification Number (9 digits)
     */
    @Column(name = "TIN")
    private Long tin;

    /**
     * TIN Filing Status
     */
    @Column(name = "TINFS")
    private Integer tinfs;

    /**
     * TIN Type (for formatting)
     * Used in legacy: TINFORMAT(TIN, TINTT)
     */
    @Column(name = "TINTT")
    private Integer tintt;

    /**
     * Taxpayer Name (primary)
     */
    @Column(name = "TP", length = 70)
    private String tp;

    /**
     * Taxpayer Name 2 (secondary)
     */
    @Column(name = "TP2", length = 70)
    private String tp2;

    /**
     * Taxpayer Control (4 chars)
     * Used in group queries as identifier
     */
    @Column(name = "TPCTRL", length = 4)
    private String tpctrl;

    /**
     * Street address
     */
    @Column(name = "STREET", length = 70)
    private String street;

    /**
     * City
     */
    @Column(name = "CITY", length = 25)
    private String city;

    /**
     * State
     */
    @Column(name = "STATE", length = 2)
    private String state;

    /**
     * Zip code
     */
    @Column(name = "ZIPCDE")
    private Long zipcde;

    /**
     * Case code
     */
    @Column(name = "CASECODE", length = 3)
    private String casecode;

    /**
     * Subcode
     */
    @Column(name = "SUBCODE", length = 3)
    private String subcode;

    /**
     * Grade
     */
    @Column(name = "GRADE")
    private Integer grade;

    /**
     * Total hours
     */
    @Column(name = "TOTHRS", precision = 7, scale = 2)
    private BigDecimal tothrs;

    /**
     * Status
     */
    @Column(name = "STATUS", length = 1)
    private String status;

    /**
     * Risk level
     */
    @Column(name = "RISK")
    private Integer risk;

    // =========================================================================
    // Helper Methods
    // =========================================================================

    /**
     * Format TIN for display based on TINTT (TIN Type)
     * 
     * Legacy: NVL(LPAD(TINFORMAT(TIN,TINTT),11),' ')
     * 
     * TINTT values (inferred):
     * - Type 1: SSN format (XXX-XX-XXXX)
     * - Type 2: EIN format (XX-XXXXXXX)
     */
    public String getFormattedTin() {
        if (tin == null) return "";
        
        String tinStr = String.format("%09d", tin);
        
        if (tintt != null && tintt == 2) {
            // EIN format: XX-XXXXXXX
            return tinStr.substring(0, 2) + "-" + tinStr.substring(2);
        } else {
            // SSN format: XXX-XX-XXXX (default)
            return tinStr.substring(0, 3) + "-" + tinStr.substring(3, 5) + "-" + tinStr.substring(5);
        }
    }

    /**
     * Get display name for taxpayer
     */
    public String getTaxpayerName() {
        if (tp != null && !tp.isBlank()) {
            return tp.trim();
        }
        return tpctrl != null ? tpctrl.trim() : "";
    }
}
