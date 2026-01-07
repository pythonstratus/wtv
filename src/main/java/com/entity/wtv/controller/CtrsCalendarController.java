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

    @GetMapping("/fiscal-years/{year}")
    @Operation(summary = "Get fiscal year details",
               description = "Returns complete fiscal year with all 12 months (October-September)")
    public ResponseEntity<FiscalYearDTO> getFiscalYear(
            @Parameter(description = "Fiscal year (e.g., 2026)")
            @PathVariable Integer year) {
        log.info("GET /api/ctrs/fiscal-years/{}", year);
        return ResponseEntity.ok(ctrsCalendarService.getFiscalYear(year));
    }

    @PostMapping("/fiscal-years")
    @Operation(summary = "Create new fiscal year",
               description = "Creates a new fiscal year with auto-generated 12 months")
    public ResponseEntity<FiscalYearDTO> createFiscalYear(
            @Valid @RequestBody CreateFiscalYearRequest request) {
        log.info("POST /api/ctrs/fiscal-years - Creating year: {}", request.getFiscalYear());
        FiscalYearDTO created = ctrsCalendarService.createFiscalYear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
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
               description = "Returns details for a specific month including week cycles")
    public ResponseEntity<FiscalMonthDTO> getFiscalMonth(
            @Parameter(description = "Month identifier (e.g., OCT2026, NOV2026)")
            @PathVariable String rptMonth) {
        log.info("GET /api/ctrs/months/{}", rptMonth);
        return ResponseEntity.ok(ctrsCalendarService.getFiscalMonth(rptMonth));
    }

    @PutMapping("/months/{rptMonth}")
    @Operation(summary = "Update fiscal month",
               description = "Updates dates, weeks, workdays for a specific month")
    public ResponseEntity<FiscalMonthDTO> updateFiscalMonth(
            @Parameter(description = "Month identifier (e.g., OCT2026)")
            @PathVariable String rptMonth,
            @Valid @RequestBody UpdateFiscalMonthRequest request) {
        log.info("PUT /api/ctrs/months/{}", rptMonth);
        return ResponseEntity.ok(ctrsCalendarService.updateFiscalMonth(rptMonth, request));
    }

    // =========================================================================
    // Bulk Operations
    // =========================================================================

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

    // =========================================================================
    // Health Check
    // =========================================================================

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns service health status")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("CTRS Calendar Service is healthy");
    }
}
