package com.entity.wtv.service;

import com.entity.wtv.dto.*;
import com.entity.wtv.entity.*;
import com.entity.wtv.exception.ResourceNotFoundException;
import com.entity.wtv.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Weekly Time Verification Service
 * 
 * Implements business logic matching legacy Pro*C code from:
 * - entity_common.pc: getTimeVerifyData, getTimeVerifyTinData, getTimeVerifyNonData, getTimeVerifyAdjData
 * - ent_timeverify.pc: Screen display and navigation logic
 * 
 * @version 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WtvService {

    private final EntempRepository entempRepository;
    private final EntmonthRepository entmonthRepository;
    private final EntcodeRepository entcodeRepository;
    private final TimenonRepository timenonRepository;
    private final TimetinRepository timetinRepository;
    private final EntRepository entRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    // =========================================================================
    // Reporting Month / Week Selection APIs
    // =========================================================================

    /**
     * Get all available reporting months with nested weeks for dropdowns
     */
    public List<ReportingMonthDTO> getReportingMonths() {
        log.debug("Fetching all reporting months");
        
        List<Entmonth> months = entmonthRepository.findAllOrderByStartDateDesc();
        
        return months.stream()
                .map(this::convertToReportingMonthDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get weeks for a specific reporting month
     */
    public List<ReportingMonthDTO.WeekDTO> getWeeksForMonth(String rptmonth) {
        log.debug("Fetching weeks for month: {}", rptmonth);
        
        Entmonth month = entmonthRepository.findByRptmonth(rptmonth)
                .orElseThrow(() -> new ResourceNotFoundException("Reporting month not found: " + rptmonth));
        
        return generateWeeksForMonth(month);
    }

    /**
     * Get pay period details by date
     */
    public PayPeriodDTO getPayPeriodByDate(LocalDate date) {
        log.debug("Fetching pay period for date: {}", date);
        
        // Calculate week boundaries (Sunday to Saturday)
        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        
        // Get reporting month
        String rptmonth = entmonthRepository.findRptmonthByDate(weekStart)
                .orElse(formatMonthYear(weekStart));
        
        return PayPeriodDTO.builder()
                .startDate(weekStart)
                .endDate(weekEnd)
                .reportingMonth(rptmonth)
                .displayLabel(formatDateRange(weekStart, weekEnd))
                .hasPrevious(true)
                .hasNext(true)
                .build();
    }

    /**
     * Navigate to previous week
     * Legacy: F3 key - dateString(viewDate, "- 7")
     */
    public PayPeriodDTO getPreviousWeek(LocalDate currentStartDate) {
        log.debug("Navigating to previous week from: {}", currentStartDate);
        return getPayPeriodByDate(currentStartDate.minusDays(7));
    }

    /**
     * Navigate to next week
     * Legacy: F4 key - dateString(viewDate, "+ 7")
     */
    public PayPeriodDTO getNextWeek(LocalDate currentStartDate) {
        log.debug("Navigating to next week from: {}", currentStartDate);
        return getPayPeriodByDate(currentStartDate.plusDays(7));
    }

    // =========================================================================
    // Group Weekly Summary API (Main View)
    // =========================================================================

    /**
     * Get group weekly summaries for the main table
     * 
     * Legacy source: getTimeVerifyData() in entity_common.pc
     * 
     * @param startDate Week start date (Sunday)
     * @param endDate Week end date (Saturday)
     * @param assignmentNumberFilter Optional filter by assignment number prefix
     * @return List of weekly summaries for all matching employees
     */
    public List<WeeklyTimeSummaryDTO> getGroupWeeklySummaries(
            LocalDate startDate, 
            LocalDate endDate,
            String assignmentNumberFilter) {
        
        log.debug("Fetching group summaries for {} to {}, filter: {}", 
                startDate, endDate, assignmentNumberFilter);

        // Get eligible employees (filtered by CFF criteria)
        List<Entemp> employees;
        if (assignmentNumberFilter != null && !assignmentNumberFilter.isBlank()) {
            employees = entempRepository.findByAssignmentNumberPrefix(assignmentNumberFilter + "%");
        } else {
            employees = entempRepository.findAllValidForWtv();
        }

        log.debug("Found {} eligible employees", employees.size());

        // Calculate summary for each employee
        return employees.stream()
                .map(emp -> calculateWeeklySummary(emp, startDate, endDate))
                .sorted(Comparator.comparing(WeeklyTimeSummaryDTO::getAssignmentNumber)
                        .thenComparing(WeeklyTimeSummaryDTO::getTour, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    /**
     * Calculate weekly summary for a single employee
     * 
     * Implements the complex SQL logic from getTimeVerifyData()
     */
    private WeeklyTimeSummaryDTO calculateWeeklySummary(Entemp employee, LocalDate startDate, LocalDate endDate) {
        Long roid = employee.getRoid();

        // Get hours from TIMENON
        BigDecimal tourDutyHours = timenonRepository.sumTourDutyHours(roid, startDate, endDate);
        BigDecimal adjustmentHours = timenonRepository.sumAdjustmentHours(roid, startDate, endDate);
        BigDecimal scheduleHours = timenonRepository.sumScheduleHours(roid, startDate, endDate);
        BigDecimal codeDirectHours = timenonRepository.sumCodeDirectHours(roid, startDate, endDate);
        BigDecimal overheadHours = timenonRepository.sumOverheadHours(roid, startDate, endDate);

        // Get hours from TIMETIN
        BigDecimal timetinHours = timetinRepository.sumHoursWorked(roid, startDate, endDate);

        // Calculate Tour of Duty Hours
        // Legacy: tourDutyHours + timetinHours - adjustmentHours - scheduleHours
        BigDecimal tourOfDuty = tourDutyHours
                .add(timetinHours)
                .subtract(adjustmentHours)
                .subtract(scheduleHours);

        // Calculate Adjusted Tour
        // Legacy: adjustmentHours - scheduleHours
        BigDecimal adjustedTour = adjustmentHours.subtract(scheduleHours);

        // Calculate Report Days
        // Legacy: count distinct rptdt from timenon (excluding 750) + count from timetin where not exists in timenon
        Integer timenonDays = timenonRepository.countDistinctReportDays(roid, startDate, endDate);
        Integer uniqueTimetinDays = timetinRepository.countUniqueTimetinDays(roid, startDate, endDate);
        int reportDays = (timenonDays != null ? timenonDays : 0) + (uniqueTimetinDays != null ? uniqueTimetinDays : 0);

        // Get Last Date EOD (max of both tables)
        LocalDate timenonMaxDate = timenonRepository.findMaxReportDate(roid, startDate, endDate);
        LocalDate timetinMaxDate = timetinRepository.findMaxReportDate(roid, startDate, endDate);
        LocalDate lastDateEod = null;
        if (timenonMaxDate != null && timetinMaxDate != null) {
            lastDateEod = timenonMaxDate.isAfter(timetinMaxDate) ? timenonMaxDate : timetinMaxDate;
        } else if (timenonMaxDate != null) {
            lastDateEod = timenonMaxDate;
        } else {
            lastDateEod = timetinMaxDate;
        }

        return WeeklyTimeSummaryDTO.builder()
                .assignmentNumber(roid)
                .employeeName(employee.getName())
                .tourOfDutyHours(tourOfDuty)
                .adjustedTour(adjustedTour)
                .hoursWorked(timetinHours)
                .caseDirectTime(timetinHours)
                .codeDirectTime(codeDirectHours)
                .overheadTime(overheadHours)
                .reportDays(reportDays)
                .tourOfDutyType(employee.getTourOfDutyType())
                .tour(employee.getTour())
                .lastDateEod(lastDateEod != null ? lastDateEod.format(DATE_FORMATTER) : "")
                .build();
    }

    // =========================================================================
    // Employee Timesheet Detail API (Drill-Down)
    // =========================================================================

    /**
     * Get complete employee timesheet for drill-down view
     * 
     * Returns all three tables:
     * 1. Daily Summary (Tour, Holiday, Credit, Worked)
     * 2. Case TIN entries
     * 3. Non-Case Time entries
     * 
     * @param roid Employee assignment number
     * @param startDate Week start date (Sunday)
     * @param endDate Week end date (Saturday)
     */
    public EmployeeTimesheetDTO getEmployeeTimesheet(Long roid, LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching timesheet for ROID {} from {} to {}", roid, startDate, endDate);

        // Validate employee exists and is valid for WTV
        Entemp employee = entempRepository.findByRoid(roid)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + roid));

        if (!entempRepository.isValidRoidForWtv(roid)) {
            throw new ResourceNotFoundException("Employee not eligible for WTV: " + roid);
        }

        // Get reporting month
        String rptmonth = entmonthRepository.findRptmonthByDate(startDate)
                .orElse(formatMonthYear(startDate));

        // Build response
        EmployeeTimesheetDTO timesheet = EmployeeTimesheetDTO.builder()
                .assignmentNumber(roid)
                .employeeName(employee.getName())
                .weekStartDate(startDate)
                .weekEndDate(endDate)
                .reportingMonth(rptmonth)
                .build();

        // Initialize day labels
        timesheet.initializeDayLabels();

        // Populate the three tables
        timesheet.setDailySummary(getDailySummary(roid, startDate, endDate));
        timesheet.setCaseTimeEntries(getCaseTimeEntries(roid, startDate, endDate));
        timesheet.setNonCaseTimeEntries(getNonCaseTimeEntries(roid, startDate, endDate));

        // Calculate totals
        timesheet.calculateTotalDirectCaseTime();
        timesheet.calculateTotalNonCreditDirectCaseTime();

        return timesheet;
    }

    /**
     * Get daily summary rows (Tour, Holiday, Credit, Worked)
     * 
     * Note: This is a stub implementation. Santosh mentioned this will be
     * populated from a separate data entry screen to be provided later.
     */
    private List<DailySummaryDTO> getDailySummary(Long roid, LocalDate startDate, LocalDate endDate) {
        List<DailySummaryDTO> summary = new ArrayList<>();
        
        // TODO: Implement when daily entry screen is provided
        // For now, create placeholder rows
        summary.add(DailySummaryDTO.createTourRow());
        summary.add(DailySummaryDTO.createHolidayRow());
        summary.add(DailySummaryDTO.createCreditRow());
        summary.add(DailySummaryDTO.createWorkedRow());

        // Populate Worked row from actual TIMETIN data
        DailySummaryDTO workedRow = summary.get(3);
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            BigDecimal dayHours = timetinRepository.sumHoursWorked(roid, date, date);
            String dayKey = getDayKey(date.getDayOfWeek());
            workedRow.setHoursForDay(dayKey, dayHours);
        }
        workedRow.recalculateTotal();

        return summary;
    }

    /**
     * Get case time entries by TIN
     * 
     * Legacy source: getTimeVerifyTinData() in entity_common.pc
     */
    private List<CaseTimeEntryDTO> getCaseTimeEntries(Long roid, LocalDate startDate, LocalDate endDate) {
        // Get distinct TINSIDs for this employee in date range
        List<Long> timesids = timetinRepository.findDistinctTimesids(roid, startDate, endDate);
        
        if (timesids.isEmpty()) {
            return Collections.emptyList();
        }

        // Get TIN info from ENT table
        List<Ent> entRecords = entRepository.findByTinsidIn(timesids);
        Map<Long, Ent> entMap = entRecords.stream()
                .collect(Collectors.toMap(Ent::getTinsid, e -> e));

        // Build case time entries
        List<CaseTimeEntryDTO> entries = new ArrayList<>();
        
        for (Long timesid : timesids) {
            Ent ent = entMap.get(timesid);
            String tin = ent != null ? ent.getFormattedTin() : String.valueOf(timesid);
            String name = ent != null ? ent.getTaxpayerName() : "Unknown";

            CaseTimeEntryDTO entry = CaseTimeEntryDTO.create(tin, name, roid, timesid);

            // Get hours for each day
            for (int i = 0; i < 7; i++) {
                LocalDate date = startDate.plusDays(i);
                BigDecimal dayHours = timetinRepository.sumHoursForTimesidAndDate(roid, timesid, date);
                String dayKey = getDayKey(date.getDayOfWeek());
                entry.setHoursForDay(dayKey, dayHours);
            }
            entry.recalculateTotal();

            if (entry.getTotalHours().compareTo(BigDecimal.ZERO) > 0) {
                entries.add(entry);
            }
        }

        // Sort by TIN
        entries.sort(Comparator.comparing(CaseTimeEntryDTO::getCaseTin));
        
        return entries;
    }

    /**
     * Get non-case time entries by time code
     * 
     * Legacy source: getTimeVerifyNonData() in entity_common.pc
     */
    private List<NonCaseTimeEntryDTO> getNonCaseTimeEntries(Long roid, LocalDate startDate, LocalDate endDate) {
        // Get distinct timecodes used
        List<String> timecodes = timenonRepository.findDistinctTimecodes(roid, startDate, endDate);
        
        if (timecodes.isEmpty()) {
            return Collections.emptyList();
        }

        List<NonCaseTimeEntryDTO> entries = new ArrayList<>();

        for (String timecode : timecodes) {
            // Get code description
            String codeName = entcodeRepository.findCodeNameByCode(timecode)
                    .orElse(timecode);
            
            // Determine category type
            Entcode entcode = entcodeRepository.findByCodeAndType(timecode, "T").orElse(null);
            String categoryType = "T";
            if (entcode != null) {
                if ("A".equals(entcode.getTimedef()) || "S".equals(entcode.getTimedef())) {
                    categoryType = "A";
                } else if ("I".equals(entcode.getTimedef())) {
                    categoryType = "I";
                }
            }

            NonCaseTimeEntryDTO entry = NonCaseTimeEntryDTO.create(
                    codeName.length() > 12 ? codeName.substring(0, 12) : codeName,
                    timecode,
                    categoryType,
                    roid);

            // Get hours for each day
            for (int i = 0; i < 7; i++) {
                LocalDate date = startDate.plusDays(i);
                BigDecimal dayHours = timenonRepository.sumHoursForDateAndCode(roid, date, timecode);
                
                // TODO: Revisit - Timecode 760 special handling
                // Legacy uses decode(hours,0,1,0) for this code
                if ("760".equals(timecode)) {
                    dayHours = (dayHours == null || dayHours.compareTo(BigDecimal.ZERO) == 0) 
                            ? BigDecimal.ONE : BigDecimal.ZERO;
                }
                
                // For adjustment codes, hours may need to be negated in display
                if ("A".equals(categoryType) && entcode != null && "A".equals(entcode.getTimedef())) {
                    dayHours = dayHours.negate();
                }

                String dayKey = getDayKey(date.getDayOfWeek());
                entry.setHoursForDay(dayKey, dayHours);
            }
            entry.recalculateTotal();

            if (entry.getTotalHours().compareTo(BigDecimal.ZERO) != 0) {
                entries.add(entry);
            }
        }

        // Sort by description
        entries.sort(Comparator.comparing(NonCaseTimeEntryDTO::getTimeDescription));
        
        return entries;
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private ReportingMonthDTO convertToReportingMonthDTO(Entmonth month) {
        ReportingMonthDTO dto = ReportingMonthDTO.builder()
                .id(month.getRptmonth())
                .rptmonth(month.getRptmonth())
                .displayLabel(formatMonthDisplay(month))
                .startDate(month.getStartdt())
                .endDate(month.getEnddt())
                .weekCount(month.getWeeks())
                .build();

        dto.setWeeks(generateWeeksForMonth(month));
        return dto;
    }

    private List<ReportingMonthDTO.WeekDTO> generateWeeksForMonth(Entmonth month) {
        List<ReportingMonthDTO.WeekDTO> weeks = new ArrayList<>();
        
        if (month.getStartdt() == null || month.getWeeks() == null) {
            return weeks;
        }

        LocalDate weekStart = month.getStartdt();
        int numWeeks = month.getWeeks() != null ? month.getWeeks() : 4;
        int postingCycle = month.getStartcyc() != null ? month.getStartcyc() : 1;

        for (int i = 1; i <= numWeeks; i++) {
            LocalDate weekEnd = weekStart.plusDays(6);
            
            weeks.add(ReportingMonthDTO.WeekDTO.builder()
                    .weekNumber(i)
                    .postingCycle(postingCycle++)
                    .startDate(weekStart)
                    .endDate(weekEnd)
                    .displayLabel(formatDateRange(weekStart, weekEnd))
                    .build());
            
            weekStart = weekStart.plusDays(7);
        }

        return weeks;
    }

    private String formatMonthDisplay(Entmonth month) {
        if (month.getStartdt() == null) return month.getRptmonth();
        return month.getStartdt().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
    }

    private String formatMonthYear(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
    }

    private String formatDateRange(LocalDate start, LocalDate end) {
        return String.format("%s - %s", 
                start.format(DATE_FORMATTER), 
                end.format(DATE_FORMATTER));
    }

    private String getDayKey(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case SUNDAY -> "SUN";
            case MONDAY -> "MON";
            case TUESDAY -> "TUE";
            case WEDNESDAY -> "WED";
            case THURSDAY -> "THU";
            case FRIDAY -> "FRI";
            case SATURDAY -> "SAT";
        };
    }

    // =========================================================================
    // CSV Export
    // =========================================================================

    /**
     * Export group summaries to CSV format
     */
    public String exportSummariesToCsv(LocalDate startDate, LocalDate endDate, String assignmentNumberFilter) {
        List<WeeklyTimeSummaryDTO> summaries = getGroupWeeklySummaries(startDate, endDate, assignmentNumberFilter);
        
        StringBuilder csv = new StringBuilder();
        
        // Header row
        csv.append("Assignment #,Employee Name,Tour of Duty Hours,Adjusted Tour,Hours Worked,");
        csv.append("Case Direct Time,Code Direct Time,Overhead Time,Report Days,Tour of Duty Type,Last Date EOD\n");
        
        // Data rows
        for (WeeklyTimeSummaryDTO summary : summaries) {
            csv.append(summary.getAssignmentNumber()).append(",");
            csv.append("\"").append(summary.getEmployeeName() != null ? summary.getEmployeeName() : "").append("\",");
            csv.append(summary.getTourOfDutyHours()).append(",");
            csv.append(summary.getAdjustedTour()).append(",");
            csv.append(summary.getHoursWorked()).append(",");
            csv.append(summary.getCaseDirectTime()).append(",");
            csv.append(summary.getCodeDirectTime()).append(",");
            csv.append(summary.getOverheadTime()).append(",");
            csv.append(summary.getReportDays()).append(",");
            csv.append(summary.getTourOfDutyType()).append(",");
            csv.append(summary.getLastDateEod() != null ? summary.getLastDateEod() : "").append("\n");
        }
        
        return csv.toString();
    }
}
