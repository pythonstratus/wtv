package com.entity.wtv.service;

import com.entity.wtv.dto.*;
import com.entity.wtv.entity.Entmonth;
import com.entity.wtv.exception.ResourceNotFoundException;
import com.entity.wtv.repository.EntmonthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for CTRS Calendar functionality
 * 
 * Manages fiscal years (October - September) and their monthly periods
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CtrsCalendarService {

    private final EntmonthRepository entmonthRepository;

    // Fiscal year month order (October to September)
    private static final String[] FISCAL_MONTH_ORDER = {
        "OCT", "NOV", "DEC", "JAN", "FEB", "MAR", 
        "APR", "MAY", "JUN", "JUL", "AUG", "SEP"
    };

    private static final LocalDate DEFAULT_RPTNATIONAL = LocalDate.of(1900, 1, 1);

    // =========================================================================
    // GET Operations
    // =========================================================================

    /**
     * Get list of all available fiscal years
     */
    public List<Integer> getAllFiscalYears() {
        log.info("Getting all fiscal years");
        return entmonthRepository.findDistinctFiscalYears();
    }

    /**
     * Get complete fiscal year with all months
     */
    public FiscalYearDTO getFiscalYear(Integer year) {
        log.info("Getting fiscal year: {}", year);
        
        String yearStr = String.valueOf(year);
        List<Entmonth> months = entmonthRepository.findByFiscalYearOrdered(yearStr);
        
        if (months.isEmpty()) {
            throw new ResourceNotFoundException("Fiscal year " + year + " not found");
        }

        List<FiscalMonthDTO> monthDTOs = months.stream()
            .map(this::toFiscalMonthDTO)
            .collect(Collectors.toList());

        int totalWeeks = months.stream()
            .mapToInt(m -> m.getWeeks() != null ? m.getWeeks() : 0)
            .sum();

        int totalWorkdays = months.stream()
            .mapToInt(m -> m.getWorkdays() != null ? m.getWorkdays() : 0)
            .sum();

        return FiscalYearDTO.builder()
            .fiscalYear(year)
            .displayLabel("FY " + year)
            .totalWeeks(totalWeeks)
            .totalWorkdays(totalWorkdays)
            .months(monthDTOs)
            .active(true)
            .build();
    }

    /**
     * Get a single fiscal month
     */
    public FiscalMonthDTO getFiscalMonth(String rptMonth) {
        log.info("Getting fiscal month: {}", rptMonth);
        
        Entmonth month = entmonthRepository.findByRptmonth(rptMonth.toUpperCase())
            .orElseThrow(() -> new ResourceNotFoundException("Month " + rptMonth + " not found"));
        
        return toFiscalMonthDTO(month);
    }

    // =========================================================================
    // CREATE Operations
    // =========================================================================

    /**
     * Create a new fiscal year with all 12 months
     * Auto-generates dates based on fiscal year calendar logic
     */
    @Transactional
    public FiscalYearDTO createFiscalYear(CreateFiscalYearRequest request) {
        log.info("Creating fiscal year: {}", request.getFiscalYear());
        
        Integer year = request.getFiscalYear();
        String yearStr = String.valueOf(year);
        
        // Check if already exists
        if (entmonthRepository.existsByFiscalYear(yearStr)) {
            throw new IllegalArgumentException("Fiscal year " + year + " already exists");
        }

        // Determine start date (first Sunday on or before Oct 1)
        LocalDate startDate = request.getStartDate();
        if (startDate == null) {
            // Calculate: First day of October in the previous calendar year
            // FY2026 starts in October 2025
            LocalDate oct1 = LocalDate.of(year - 1, 10, 1);
            // Find the Sunday on or before Oct 1
            startDate = oct1.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        }

        List<Entmonth> generatedMonths = new ArrayList<>();
        LocalDate currentStart = startDate;
        int cycleNumber = year * 100 + 1; // e.g., 202601

        for (String monthAbbrev : FISCAL_MONTH_ORDER) {
            // Determine calendar year for this month
            int calendarYear = monthAbbrev.matches("OCT|NOV|DEC") ? year - 1 : year;
            String rptMonth = monthAbbrev + calendarYear;

            // Determine weeks (typically 4, some months have 5)
            int weeks = calculateWeeksForMonth(monthAbbrev, calendarYear);
            
            // Calculate end date (weeks * 7 days - 1)
            LocalDate endDate = currentStart.plusDays((weeks * 7L) - 1);
            
            // Calculate workdays (weeks * 5, roughly)
            int workdays = weeks * 5;

            Entmonth month = Entmonth.builder()
                .rptmonth(rptMonth)
                .startdt(currentStart)
                .enddt(endDate)
                .weeks(weeks)
                .startcyc(cycleNumber)
                .endcyc(cycleNumber + weeks - 1)
                .workdays(workdays)
                .rptnational(DEFAULT_RPTNATIONAL)
                .build();

            generatedMonths.add(month);
            
            // Move to next month
            currentStart = endDate.plusDays(1);
            cycleNumber += weeks;
        }

        // Save all months
        entmonthRepository.saveAll(generatedMonths);
        log.info("Created {} months for fiscal year {}", generatedMonths.size(), year);

        return getFiscalYear(year);
    }

    // =========================================================================
    // UPDATE Operations
    // =========================================================================

    /**
     * Update a single fiscal month
     */
    @Transactional
    public FiscalMonthDTO updateFiscalMonth(String rptMonth, UpdateFiscalMonthRequest request) {
        log.info("Updating fiscal month: {}", rptMonth);
        
        Entmonth month = entmonthRepository.findByRptmonth(rptMonth.toUpperCase())
            .orElseThrow(() -> new ResourceNotFoundException("Month " + rptMonth + " not found"));

        // Update fields if provided
        if (request.getStartDate() != null) {
            month.setStartdt(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            month.setEnddt(request.getEndDate());
        }
        if (request.getWeeks() != null) {
            month.setWeeks(request.getWeeks());
        }
        if (request.getStartCycle() != null) {
            month.setStartcyc(request.getStartCycle());
        }
        if (request.getEndCycle() != null) {
            month.setEndcyc(request.getEndCycle());
        }
        if (request.getWorkdays() != null) {
            month.setWorkdays(request.getWorkdays());
        }

        entmonthRepository.save(month);
        log.info("Updated month: {}", rptMonth);

        return toFiscalMonthDTO(month);
    }

    /**
     * Bulk update all months in a fiscal year
     */
    @Transactional
    public FiscalYearDTO updateFiscalYear(Integer year, List<UpdateFiscalMonthRequest> monthUpdates) {
        log.info("Bulk updating fiscal year: {}", year);
        
        // Implementation would iterate through monthUpdates and apply each
        // For now, return the current state
        return getFiscalYear(year);
    }

    // =========================================================================
    // DELETE Operations
    // =========================================================================

    /**
     * Delete an entire fiscal year
     */
    @Transactional
    public void deleteFiscalYear(Integer year) {
        log.info("Deleting fiscal year: {}", year);
        
        String yearStr = String.valueOf(year);
        
        if (!entmonthRepository.existsByFiscalYear(yearStr)) {
            throw new ResourceNotFoundException("Fiscal year " + year + " not found");
        }

        // TODO: Add check for existing time entries before deleting
        
        int deleted = entmonthRepository.deleteByFiscalYear(yearStr);
        log.info("Deleted {} months for fiscal year {}", deleted, year);
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    /**
     * Convert Entmonth entity to FiscalMonthDTO
     */
    private FiscalMonthDTO toFiscalMonthDTO(Entmonth entity) {
        List<FiscalMonthDTO.WeekCycleDTO> weeks = generateWeekCycles(entity);
        
        String dateRange = formatDateRange(entity.getStartdt(), entity.getEnddt());

        return FiscalMonthDTO.builder()
            .rptMonth(entity.getRptmonth())
            .monthName(entity.getMonthName())
            .postingCycles(entity.getWeeks())
            .startDate(entity.getStartdt())
            .endDate(entity.getEnddt())
            .dateRange(dateRange)
            .workdays(entity.getWorkdays())
            .holidays(0) // Placeholder - no separate holidays table
            .hours(entity.getWorkdays()) // Hours = Workdays in UI
            .startCycle(entity.getStartcyc())
            .endCycle(entity.getEndcyc())
            .weeks(weeks)
            .rptNational(entity.getRptnational())
            .build();
    }

    /**
     * Generate individual week cycles for a month
     */
    private List<FiscalMonthDTO.WeekCycleDTO> generateWeekCycles(Entmonth month) {
        List<FiscalMonthDTO.WeekCycleDTO> weeks = new ArrayList<>();
        
        if (month.getStartdt() == null || month.getStartcyc() == null || month.getWeeks() == null) {
            return weeks;
        }

        LocalDate weekStart = month.getStartdt();
        int cycleNum = month.getStartcyc();

        for (int i = 0; i < month.getWeeks(); i++) {
            LocalDate weekEnd = weekStart.plusDays(6);
            
            weeks.add(FiscalMonthDTO.WeekCycleDTO.builder()
                .cycleNumber(cycleNum)
                .startDate(weekStart)
                .endDate(weekEnd)
                .dateRange(formatWeekDateRange(weekStart, weekEnd))
                .workdays(5)
                .build());

            weekStart = weekEnd.plusDays(1);
            cycleNum++;
        }

        return weeks;
    }

    /**
     * Format date range for display (e.g., "Sep 29 - Oct 26")
     */
    private String formatDateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) return "";
        
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d");
        return start.format(fmt) + " - " + end.format(fmt);
    }

    /**
     * Format week date range for display (e.g., "September 29 - October 3")
     */
    private String formatWeekDateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) return "";
        
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM d");
        return start.format(fmt) + " - " + end.format(fmt);
    }

    /**
     * Calculate number of weeks for a fiscal month
     * Most months have 4 weeks, some have 5 to balance the year
     */
    private int calculateWeeksForMonth(String monthAbbrev, int calendarYear) {
        // Simplified logic - in reality this would be more complex
        // Typically: March, June, September, December might have 5 weeks
        // to ensure 52 weeks total
        return switch (monthAbbrev) {
            case "MAR", "JUN", "SEP", "DEC" -> 5;
            default -> 4;
        };
    }
}
