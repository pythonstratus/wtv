package com.entity.wtv.controller;

import com.entity.wtv.dto.*;
import com.entity.wtv.service.WtvService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Weekly Time Verification
 * 
 * Base path: /api/wtv
 */
@RestController
@RequestMapping("/api/wtv")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Weekly Time Verification", description = "APIs for WTV group view and employee detail")
@CrossOrigin(origins = "*")
public class WtvController {

    private final WtvService wtvService;

    // =========================================================================
    // Reporting Month / Week Selection APIs
    // =========================================================================

    @GetMapping("/reporting-months")
    @Operation(summary = "Get all reporting months", 
               description = "Returns list of reporting months with nested weeks for dropdown population")
    public ResponseEntity<List<ReportingMonthDTO>> getReportingMonths() {
        log.info("GET /api/wtv/reporting-months");
        return ResponseEntity.ok(wtvService.getReportingMonths());
    }

    @GetMapping("/weeks")
    @Operation(summary = "Get weeks for a reporting month",
               description = "Returns list of weeks for the specified reporting month")
    public ResponseEntity<List<ReportingMonthDTO.WeekDTO>> getWeeksForMonth(
            @Parameter(description = "Reporting month code (e.g., '2023-11')")
            @RequestParam String month) {
        log.info("GET /api/wtv/weeks?month={}", month);
        return ResponseEntity.ok(wtvService.getWeeksForMonth(month));
    }

    @GetMapping("/pay-period")
    @Operation(summary = "Get pay period by date",
               description = "Returns pay period information for the specified date")
    public ResponseEntity<PayPeriodDTO> getPayPeriodByDate(
            @Parameter(description = "Date to find pay period for (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("GET /api/wtv/pay-period?date={}", date);
        return ResponseEntity.ok(wtvService.getPayPeriodByDate(date));
    }

    @GetMapping("/pay-period/previous")
    @Operation(summary = "Navigate to previous week",
               description = "Returns pay period for the week before the specified start date")
    public ResponseEntity<PayPeriodDTO> getPreviousWeek(
            @Parameter(description = "Current week start date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate currentStartDate) {
        log.info("GET /api/wtv/pay-period/previous?currentStartDate={}", currentStartDate);
        return ResponseEntity.ok(wtvService.getPreviousWeek(currentStartDate));
    }

    @GetMapping("/pay-period/next")
    @Operation(summary = "Navigate to next week",
               description = "Returns pay period for the week after the specified start date")
    public ResponseEntity<PayPeriodDTO> getNextWeek(
            @Parameter(description = "Current week start date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate currentStartDate) {
        log.info("GET /api/wtv/pay-period/next?currentStartDate={}", currentStartDate);
        return ResponseEntity.ok(wtvService.getNextWeek(currentStartDate));
    }

    // =========================================================================
    // Group Weekly Summary API (Main View)
    // =========================================================================

    @GetMapping("/summaries")
    @Operation(summary = "Get group weekly summaries",
               description = "Returns weekly time summaries for all employees (main table view)")
    public ResponseEntity<List<WeeklyTimeSummaryDTO>> getGroupWeeklySummaries(
            @Parameter(description = "Week start date - Sunday (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "Week end date - Saturday (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            
            @Parameter(description = "Optional assignment number filter (prefix match)")
            @RequestParam(required = false) String assignmentNumber) {
        
        log.info("GET /api/wtv/summaries?startDate={}&endDate={}&assignmentNumber={}", 
                startDate, endDate, assignmentNumber);
        
        return ResponseEntity.ok(wtvService.getGroupWeeklySummaries(startDate, endDate, assignmentNumber));
    }

    // =========================================================================
    // Employee Timesheet Detail API (Drill-Down)
    // =========================================================================

    @GetMapping("/employees/{roid}/timesheet")
    @Operation(summary = "Get employee timesheet detail",
               description = "Returns complete timesheet with daily summary, case time, and non-case time")
    public ResponseEntity<EmployeeTimesheetDTO> getEmployeeTimesheet(
            @Parameter(description = "Employee assignment number (ROID)")
            @PathVariable Long roid,
            
            @Parameter(description = "Week start date - Sunday (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "Week end date - Saturday (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("GET /api/wtv/employees/{}/timesheet?startDate={}&endDate={}", 
                roid, startDate, endDate);
        
        return ResponseEntity.ok(wtvService.getEmployeeTimesheet(roid, startDate, endDate));
    }

    // =========================================================================
    // CSV Export API
    // =========================================================================

    @GetMapping("/summaries/export")
    @Operation(summary = "Export group summaries to CSV",
               description = "Returns weekly time summaries as CSV download")
    public ResponseEntity<String> exportSummariesToCsv(
            @Parameter(description = "Week start date - Sunday (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "Week end date - Saturday (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            
            @Parameter(description = "Optional assignment number filter (prefix match)")
            @RequestParam(required = false) String assignmentNumber) {
        
        log.info("GET /api/wtv/summaries/export?startDate={}&endDate={}&assignmentNumber={}", 
                startDate, endDate, assignmentNumber);
        
        String csv = wtvService.exportSummariesToCsv(startDate, endDate, assignmentNumber);
        
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=wtv_export_" + startDate + ".csv")
                .body(csv);
    }

    // =========================================================================
    // Health Check
    // =========================================================================

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns service health status")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("WTV Service is healthy");
    }
}
