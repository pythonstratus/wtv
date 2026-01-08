package com.entity.wtv.controller;

import com.entity.wtv.dto.*;
import com.entity.wtv.service.CtrsCalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for CTRS Calendar Management
 * 
 * Manages fiscal years (October - September) and their monthly periods.
 * Supports:
 * - Creating fiscal years (auto-generated, empty, or copied from previous)
 * - Updating months and individual weeks/posting cycles
 * - Marking fiscal years as active/inactive
 * 
 * Base path: /api/ctrs
 */
@RestController
@RequestMapping("/api/ctrs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "CTRS Calendar", description = "APIs for managing fiscal years and calendar periods")
@CrossOrigin(origins = "*")
public class CtrsCalendarController {

    private final CtrsCalendarService ctrsCalendarService;

    // =========================================================================
    // Fiscal Year Endpoints
    // =========================================================================

    @GetMapping("/fiscal-years")
    @Operation(summary = "Get all fiscal years", 
               description = "Returns list of all available fiscal years for dropdown")
    public ResponseEntity<List<Integer>> getAllFiscalYears() {
        log.info("GET /api/ctrs/fiscal-years");
        return ResponseEntity.ok(ctrsCalendarService.getAllFiscalYears());
    }

    @GetMapping("/fiscal-years/active")
    @Operation(summary = "Get active fiscal years only",
               description = "Returns list of fiscal years that are currently active")
    public ResponseEntity<List<Integer>> getActiveFiscalYears() {
        log.info("GET /api/ctrs/fiscal-years/active");
        return ResponseEntity.ok(ctrsCalendarService.getActiveFiscalYears());
    }

    @GetMapping("/fiscal-years/{year}")
    @Operation(summary = "Get fiscal year details",
               description = "Returns complete fiscal year with all 12 months (October-September) and week details")
    public ResponseEntity<FiscalYearDTO> getFiscalYear(
            @Parameter(description = "Fiscal year (e.g., 2026)")
            @PathVariable Integer year) {
        log.info("GET /api/ctrs/fiscal-years/{}", year);
        return ResponseEntity.ok(ctrsCalendarService.getFiscalYear(year));
    }

    @PostMapping("/fiscal-years")
    @Operation(summary = "Create new fiscal year",
               description = "Creates a new fiscal year. Options: auto-generate dates, create empty, or copy from previous year")
    public ResponseEntity<FiscalYearDTO> createFiscalYear(
            @Valid @RequestBody CreateFiscalYearRequest request) {
        log.info("POST /api/ctrs/fiscal-years - Creating year: {}, empty: {}", 
                request.getFiscalYear(), request.getEmpty());
        FiscalYearDTO created = ctrsCalendarService.createFiscalYear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/fiscal-years/{year}")
    @Operation(summary = "Bulk update fiscal year",
               description = "Updates multiple months in a fiscal year at once")
    public ResponseEntity<FiscalYearDTO> updateFiscalYear(
            @Parameter(description = "Fiscal year (e.g., 2026)")
            @PathVariable Integer year,
            @Valid @RequestBody List<UpdateFiscalMonthRequest> monthUpdates) {
        log.info("PUT /api/ctrs/fiscal-years/{} - Updating {} months", year, monthUpdates.size());
        return ResponseEntity.ok(ctrsCalendarService.updateFiscalYear(year, monthUpdates));
    }

    @PutMapping("/fiscal-years/{year}/activate")
    @Operation(summary = "Mark fiscal year as active",
               description = "Sets all months in the fiscal year to active status")
    public ResponseEntity<FiscalYearDTO> activateFiscalYear(
            @Parameter(description = "Fiscal year to activate (e.g., 2026)")
            @PathVariable Integer year) {
        log.info("PUT /api/ctrs/fiscal-years/{}/activate", year);
        return ResponseEntity.ok(ctrsCalendarService.markFiscalYearActive(year));
    }

    @PutMapping("/fiscal-years/{year}/deactivate")
    @Operation(summary = "Mark fiscal year as inactive",
               description = "Sets all months in the fiscal year to inactive status")
    public ResponseEntity<FiscalYearDTO> deactivateFiscalYear(
            @Parameter(description = "Fiscal year to deactivate (e.g., 2026)")
            @PathVariable Integer year) {
        log.info("PUT /api/ctrs/fiscal-years/{}/deactivate", year);
        return ResponseEntity.ok(ctrsCalendarService.markFiscalYearInactive(year));
    }

    @DeleteMapping("/fiscal-years/{year}")
    @Operation(summary = "Delete fiscal year",
               description = "Deletes all months for a fiscal year. Cannot delete if time entries exist.")
    public ResponseEntity<Map<String, String>> deleteFiscalYear(
            @Parameter(description = "Fiscal year to delete (e.g., 2026)")
            @PathVariable Integer year) {
        log.info("DELETE /api/ctrs/fiscal-years/{}", year);
        ctrsCalendarService.deleteFiscalYear(year);
        return ResponseEntity.ok(Map.of(
            "message", "Fiscal year " + year + " deleted successfully",
            "year", String.valueOf(year)
        ));
    }

    // =========================================================================
    // Fiscal Month Endpoints
    // =========================================================================

    @GetMapping("/months/{rptMonth}")
    @Operation(summary = "Get fiscal month details",
               description = "Returns details for a specific month including all week/posting cycles")
    public ResponseEntity<FiscalMonthDTO> getFiscalMonth(
            @Parameter(description = "Month identifier (e.g., OCT2025, NOV2025)")
            @PathVariable String rptMonth) {
        log.info("GET /api/ctrs/months/{}", rptMonth);
        return ResponseEntity.ok(ctrsCalendarService.getFiscalMonth(rptMonth));
    }

    @PutMapping("/months/{rptMonth}")
    @Operation(summary = "Update fiscal month",
               description = "Updates dates, weeks, workdays, holidays, hours for a specific month. Can also update individual weeks.")
    public ResponseEntity<FiscalMonthDTO> updateFiscalMonth(
            @Parameter(description = "Month identifier (e.g., OCT2025)")
            @PathVariable String rptMonth,
            @Valid @RequestBody UpdateFiscalMonthRequest request) {
        log.info("PUT /api/ctrs/months/{}", rptMonth);
        return ResponseEntity.ok(ctrsCalendarService.updateFiscalMonth(rptMonth, request));
    }

    // =========================================================================
    // Health Check
    // =========================================================================

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns service health status")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("CTRS Calendar Service is healthy");
    }
}
