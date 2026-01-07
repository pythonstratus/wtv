package com.entity.wtv.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * ENTEMP - Employee/Assignment Master Table
 * 
 * Source: ENTITYDEV.ENTEMP (39 columns)
 * Primary Key: ROID
 * 
 * Used in WTV for:
 * - Employee assignment information
 * - Tour of Duty type (TOUR column: 1=REG, 2=5/4/9, 3=4/10, 4=PT, 5=MAXI)
 * - Active status filtering (EACTIVE in 'A','Y')
 * - Employee type filtering (TYPE in 'C','M','P','R','T')
 * - Position type filtering (POSTYPE not in 'B','V')
 */
@Entity
@Table(name = "ENTEMP")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Entemp {

    @Id
    @Column(name = "ROID")
    private Long roid;

    @Column(name = "NAME", length = 35)
    private String name;

    @Column(name = "GRADE")
    private Integer grade;

    @Column(name = "TYPE", length = 1)
    private String type;

    @Column(name = "ICSACC", length = 1)
    private String icsacc;

    @Column(name = "BADGE", length = 10)
    private String badge;

    @Column(name = "TITLE", length = 25)
    private String title;

    @Column(name = "AREACD")
    private Integer areacd;

    @Column(name = "PHONE")
    private Integer phone;

    @Column(name = "EXT")
    private Integer ext;

    @Column(name = "SEID", length = 5)
    private String seid;

    @Column(name = "EMAIL", length = 50)
    private String email;

    @Column(name = "POSTYPE", length = 1)
    private String postype;

    @Column(name = "AREA", length = 1)
    private String area;

    /**
     * Tour of Duty Type
     * 1 = REG (Regular)
     * 2 = 5/4/9
     * 3 = 4/10
     * 4 = PT (Part-Time)
     * 5 = MAXI
     */
    @Column(name = "TOUR")
    private Integer tour;

    @Column(name = "PODIND", length = 1)
    private String podind;

    @Column(name = "TPSIND", length = 1)
    private String tpsind;

    @Column(name = "CSUIND", length = 1)
    private String csuind;

    @Column(name = "AIDEIND", length = 1)
    private String aideind;

    @Column(name = "FLEXIND", length = 1)
    private String flexind;

    @Column(name = "EMPDT")
    private LocalDate empdt;

    @Column(name = "ADJDT")
    private LocalDate adjdt;

    @Column(name = "ADJREASON", length = 4)
    private String adjreason;

    @Column(name = "ADJPERCENT")
    private Integer adjpercent;

    @Column(name = "PREVID")
    private Long previd;

    /**
     * Active Status
     * 'A' = Active
     * 'Y' = Yes (Active)
     */
    @Column(name = "EACTIVE", length = 1)
    private String eactive;

    @Column(name = "UNIX", length = 8)
    private String unix;

    @Column(name = "ELEVEL")
    private Integer elevel;

    @Column(name = "EXTRDT")
    private LocalDate extrdt;

    @Column(name = "PRIMARY_ROID", length = 1)
    private String primaryRoid;

    @Column(name = "PODCD", length = 3)
    private String podcd;

    @Column(name = "ORG", length = 2)
    private String org;

    @Column(name = "LASTLOGIN")
    private LocalDate lastlogin;

    @Column(name = "GS9CNT")
    private Integer gs9cnt;

    @Column(name = "GS11CNT")
    private Integer gs11cnt;

    @Column(name = "GS12CNT")
    private Integer gs12cnt;

    @Column(name = "GS13CNT")
    private Integer gs13cnt;

    @Column(name = "LOGOFF")
    private LocalDate logoff;

    @Column(name = "IP_ADDR", length = 39)
    private String ipAddr;

    // =========================================================================
    // Helper Methods
    // =========================================================================

    /**
     * Get Tour of Duty Type as display string
     * Matches legacy: decode(tour,1,'REG',2,'5/4/9',3,'4/10',4,'PT',5,'MAXI','-')
     */
    public String getTourOfDutyType() {
        if (tour == null) return "-";
        return switch (tour) {
            case 1 -> "REG";
            case 2 -> "5/4/9";
            case 3 -> "4/10";
            case 4 -> "PT";
            case 5 -> "MAXI";
            default -> "-";
        };
    }

    /**
     * Check if employee is active
     */
    public boolean isActive() {
        return "A".equals(eactive) || "Y".equals(eactive);
    }

    /**
     * Check if employee type is valid for WTV
     * Valid types: C, M, P, R, T, H
     */
    public boolean isValidType() {
        return type != null && ("C".equals(type) || "M".equals(type) || 
               "P".equals(type) || "R".equals(type) || "T".equals(type) || "H".equals(type));
    }

    /**
     * Check if position type is valid for WTV
     * Invalid types: B, V
     */
    public boolean isValidPosType() {
        return postype == null || (!"B".equals(postype) && !"V".equals(postype));
    }
}
