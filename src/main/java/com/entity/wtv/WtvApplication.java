package com.entity.wtv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Weekly Time Verification (WTV) Service
 * 
 * Modernized from legacy Pro*C code (entity_common.pc, ent_timeverify.pc)
 * 
 * This service provides APIs for:
 * - Group Weekly Hours Verification (main view)
 * - Employee Timesheet Detail (drill-down with 3 tables)
 * - Pay Period / Reporting Month navigation
 * - CSV Export functionality
 * 
 * @version 2.0.0
 */
@SpringBootApplication
public class WtvApplication {

    public static void main(String[] args) {
        SpringApplication.run(WtvApplication.class, args);
    }
}
