package com.entity.wtv.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * DTO for Employee Detail Daily Summary Table (Table 1)
 * 
 * Displays Tour, Holiday, Credit, and Worked hours by day of week
 * 
 * Note: This will be populated from a separate data entry screen
 * that Santosh will provide later. For now, calculated from TIMENON.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailySummaryDTO {

    /**
     * Row type: TOUR, HOLIDAY, CREDIT, WORKED
     */
    private String rowType;

    /**
     * Hours by day of week
     * Keys: SUN, MON, TUE, WED, THU, FRI, SAT
     */
    @Builder.Default
    private Map<String, BigDecimal> hoursByDay = new LinkedHashMap<>();

    /**
     * Total hours for the row
     */
    @Builder.Default
    private BigDecimal totalHours = BigDecimal.ZERO;

    // =========================================================================
    // Static Factory Methods
    // =========================================================================

    public static DailySummaryDTO createTourRow() {
        return DailySummaryDTO.builder()
                .rowType("Tour")
                .hoursByDay(createEmptyDayMap())
                .build();
    }

    public static DailySummaryDTO createHolidayRow() {
        return DailySummaryDTO.builder()
                .rowType("Holiday")
                .hoursByDay(createEmptyDayMap())
                .build();
    }

    public static DailySummaryDTO createCreditRow() {
        return DailySummaryDTO.builder()
                .rowType("Credit")
                .hoursByDay(createEmptyDayMap())
                .build();
    }

    public static DailySummaryDTO createWorkedRow() {
        return DailySummaryDTO.builder()
                .rowType("Worked")
                .hoursByDay(createEmptyDayMap())
                .build();
    }

    private static Map<String, BigDecimal> createEmptyDayMap() {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        map.put("SUN", BigDecimal.ZERO);
        map.put("MON", BigDecimal.ZERO);
        map.put("TUE", BigDecimal.ZERO);
        map.put("WED", BigDecimal.ZERO);
        map.put("THU", BigDecimal.ZERO);
        map.put("FRI", BigDecimal.ZERO);
        map.put("SAT", BigDecimal.ZERO);
        return map;
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    public void setHoursForDay(String day, BigDecimal hours) {
        if (hoursByDay == null) {
            hoursByDay = createEmptyDayMap();
        }
        hoursByDay.put(day.toUpperCase(), hours);
        recalculateTotal();
    }

    public BigDecimal getHoursForDay(String day) {
        if (hoursByDay == null) return BigDecimal.ZERO;
        return hoursByDay.getOrDefault(day.toUpperCase(), BigDecimal.ZERO);
    }

    public void recalculateTotal() {
        if (hoursByDay == null) {
            totalHours = BigDecimal.ZERO;
            return;
        }
        totalHours = hoursByDay.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
