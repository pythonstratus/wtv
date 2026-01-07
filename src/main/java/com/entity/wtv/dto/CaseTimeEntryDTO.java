package com.entity.wtv.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * DTO for Employee Detail Case TIN Table (Table 2)
 * 
 * Displays hours worked on each case/TIN by day of week
 * 
 * Legacy source: getTimeVerifyTinData() in entity_common.pc
 * 
 * Columns: Case TIN, Name, Sun, Mon, Tue, Wed, Thu, Fri, Sat, Total Hours
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaseTimeEntryDTO {

    /**
     * Formatted TIN (e.g., "111-22-3333" or "44-5555555")
     * From: ENT.TIN formatted by TINTT
     */
    private String caseTin;

    /**
     * Taxpayer name
     * From: ENT.TP or ENT.TPCTRL
     */
    private String taxpayerName;

    /**
     * Assignment ROID
     */
    private Long roid;

    /**
     * Time Session ID (links to ENT.TINSID)
     */
    private Long timesid;

    /**
     * Hours by day of week
     * Keys: SUN, MON, TUE, WED, THU, FRI, SAT
     */
    @Builder.Default
    private Map<String, BigDecimal> hoursByDay = new LinkedHashMap<>();

    /**
     * Total hours for this case
     */
    @Builder.Default
    private BigDecimal totalHours = BigDecimal.ZERO;

    // =========================================================================
    // Static Factory Methods
    // =========================================================================

    public static CaseTimeEntryDTO create(String tin, String name, Long roid, Long timesid) {
        return CaseTimeEntryDTO.builder()
                .caseTin(tin)
                .taxpayerName(name)
                .roid(roid)
                .timesid(timesid)
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
}
