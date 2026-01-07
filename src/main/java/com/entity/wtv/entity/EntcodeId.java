package com.entity.wtv.entity;

import lombok.*;
import java.io.Serializable;

/**
 * Composite Primary Key for ENTCODE table
 * 
 * The ENTCODE table has a composite key of CODE + TYPE
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class EntcodeId implements Serializable {
    
    private String code;
    private String type;
}
