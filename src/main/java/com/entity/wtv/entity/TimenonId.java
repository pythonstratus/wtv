package com.entity.wtv.entity;

import lombok.*;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Composite Primary Key for TIMENON table
 * 
 * The TIMENON table has a composite key of RPTDT + ROID + TIMECODE
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TimenonId implements Serializable {
    
    private LocalDate rptdt;
    private Long roid;
    private String timecode;
}
