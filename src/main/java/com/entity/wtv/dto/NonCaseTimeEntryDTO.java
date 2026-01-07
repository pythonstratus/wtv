package com.entity.wtv.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * DTO for Employee Detail Non-Case Time Table (Table 3)
 * 
 * Displays non-case hours by time code and day of week
 * 
 * Legacy source: getTimeVerifyNonData() in entity_common.pc
 * 
 * Columns: Time Description, Time Code, Sun, Mon, Tue, Wed, Thu, Fri, Sat, Total Hours
 * 
 * Categories from legacy UNION query:
 * - Type 'T': Regular time codes (TIMEDEF in E,O,R,N,G,C,U,M)
 * - Type 'A': Adjustment codes (TIMEDEF in A,S) - Note: 'A' hours are negated
 * - Type 'I': Info codes (timecode != 760) 
 * - Type 'I': Timecode 760 with special binary handling
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NonCaseTimeEntryDTO {

    /**
     * Time description (from ENTCODE.CDNAME, truncated to 12 chars)
     * Examples: ADP SUPPORT, ADMINISTRATION, LEAVE, FLEXIPLACE, HOLIDAY
     */
    private String timeDescription;

    /**
     * Time code (from TIMENON.TIMECODE)
     */
    private String timeCode;

    /**
     * Category type from legacy query
     * 'T' = Regular time codes
     * 'A' = Adjustment codes
     * 'I' = Info/Other codes
     */
    private String categoryType;

    /**
     * Assignment ROID
     */
    private Long roid;

    /**
     * Hours by day of week
     * Keys: SUN, MON, TUE, WED, THU, FRI, SAT
     */
    @Builder.Default
    private Map<String, BigDecimal> hoursByDay = new LinkedHashMap<>();

    /**
     * Total hours for this time code
     */
    @Builder.Default
    private BigDecimal totalHours = BigDecimal.ZERO;

    // =========================================================================
    // Static Factory Methods
    // =========================================================================

    public static NonCaseTimeEntryDTO create(String description, String code, String type, Long roid) {
        return NonCaseTimeEntryDTO.builder()
                .timeDescription(description)
                .timeCode(code)
                .categoryType(type)
                .roid(roid)
                .hoursByDay(createEmptyDayMap())
                .totalHours(BigDecimal.ZERO)
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

    public void addHoursForDay(String day, BigDecimal hours) {
        if (hoursByDay == null) {
            hoursByDay = createEmptyDayMap();
        }
        String dayKey = day.toUpperCase();
        BigDecimal current = hoursByDay.getOrDefault(dayKey, BigDecimal.ZERO);
        hoursByDay.put(dayKey, current.add(hours));
        recalculateTotal();
    }

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

    /**
     * Check if this is an adjustment entry (hours should be negated in display)
     */
    public boolean isAdjustmentEntry() {
        return "A".equals(categoryType);
    }
}
