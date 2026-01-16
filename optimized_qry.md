Here's the optimized code to replace in your `WtvService.java`:

---

## 1. Add This Native Query to `EntempRepository.java`

```java
/**
 * Optimized single query for weekly summaries - replaces N+1 queries
 */
@Query(value = """
    SELECT 
        e.ROID AS assignmentNumber,
        e.NAME AS employeeName,
        e.TOUR AS tour,
        NVL(SUM(CASE WHEN c.TIMEDEF IN ('M','U','C','G','N','R','O','E') THEN n.HOURS ELSE 0 END), 0) AS tourDutyHours,
        NVL(SUM(CASE WHEN c.TIMEDEF = 'A' THEN n.HOURS ELSE 0 END), 0) AS adjustmentHours,
        NVL(SUM(CASE WHEN c.TIMEDEF = 'S' THEN n.HOURS ELSE 0 END), 0) AS scheduleHours,
        NVL(SUM(CASE WHEN c.TIMEDEF IN ('G','M','C','U','N','E') THEN n.HOURS ELSE 0 END), 0) AS codeDirectHours,
        NVL(SUM(CASE WHEN c.TIMEDEF IN ('O','R') THEN n.HOURS ELSE 0 END), 0) AS overheadHours,
        NVL(SUM(t.HOURS), 0) AS timetinHours,
        COUNT(DISTINCT CASE WHEN n.TIMECODE != '750' THEN n.RPTDT END) AS timenonDays,
        COUNT(DISTINCT CASE WHEN t.RPTDT IS NOT NULL AND n.RPTDT IS NULL THEN t.RPTDT END) AS uniqueTimetinDays,
        GREATEST(MAX(n.RPTDT), MAX(t.RPTDT)) AS lastDateEod
    FROM ENTEMP e
    LEFT JOIN TIMENON n ON e.ROID = n.ROID AND n.RPTDT BETWEEN :startDate AND :endDate
    LEFT JOIN ENTCODE c ON n.TIMECODE = c.CODE AND c.TYPE = 'T'
    LEFT JOIN TIMETIN t ON e.ROID = t.ROID AND t.RPTDT BETWEEN :startDate AND :endDate
    WHERE e.EACTIVE IN ('A', 'Y')
      AND (
          (e.TYPE IN ('M', 'R', 'C', 'P', 'T') AND (e.POSTYPE IS NULL OR e.POSTYPE NOT IN ('B', 'V')))
          OR e.TYPE = 'H'
      )
      AND e.ROID BETWEEN 21000000 AND 36999999
    GROUP BY e.ROID, e.NAME, e.TOUR
    ORDER BY e.ROID, e.TOUR
    """, nativeQuery = true)
List<Object[]> findWeeklySummariesOptimized(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

/**
 * Optimized query with assignment number filter
 */
@Query(value = """
    SELECT 
        e.ROID AS assignmentNumber,
        e.NAME AS employeeName,
        e.TOUR AS tour,
        NVL(SUM(CASE WHEN c.TIMEDEF IN ('M','U','C','G','N','R','O','E') THEN n.HOURS ELSE 0 END), 0) AS tourDutyHours,
        NVL(SUM(CASE WHEN c.TIMEDEF = 'A' THEN n.HOURS ELSE 0 END), 0) AS adjustmentHours,
        NVL(SUM(CASE WHEN c.TIMEDEF = 'S' THEN n.HOURS ELSE 0 END), 0) AS scheduleHours,
        NVL(SUM(CASE WHEN c.TIMEDEF IN ('G','M','C','U','N','E') THEN n.HOURS ELSE 0 END), 0) AS codeDirectHours,
        NVL(SUM(CASE WHEN c.TIMEDEF IN ('O','R') THEN n.HOURS ELSE 0 END), 0) AS overheadHours,
        NVL(SUM(t.HOURS), 0) AS timetinHours,
        COUNT(DISTINCT CASE WHEN n.TIMECODE != '750' THEN n.RPTDT END) AS timenonDays,
        COUNT(DISTINCT CASE WHEN t.RPTDT IS NOT NULL AND n.RPTDT IS NULL THEN t.RPTDT END) AS uniqueTimetinDays,
        GREATEST(MAX(n.RPTDT), MAX(t.RPTDT)) AS lastDateEod
    FROM ENTEMP e
    LEFT JOIN TIMENON n ON e.ROID = n.ROID AND n.RPTDT BETWEEN :startDate AND :endDate
    LEFT JOIN ENTCODE c ON n.TIMECODE = c.CODE AND c.TYPE = 'T'
    LEFT JOIN TIMETIN t ON e.ROID = t.ROID AND t.RPTDT BETWEEN :startDate AND :endDate
    WHERE e.EACTIVE IN ('A', 'Y')
      AND (
          (e.TYPE IN ('M', 'R', 'C', 'P', 'T') AND (e.POSTYPE IS NULL OR e.POSTYPE NOT IN ('B', 'V')))
          OR e.TYPE = 'H'
      )
      AND TO_CHAR(e.ROID) LIKE :assignmentPrefix
    GROUP BY e.ROID, e.NAME, e.TOUR
    ORDER BY e.ROID, e.TOUR
    """, nativeQuery = true)
List<Object[]> findWeeklySummariesByAssignmentOptimized(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("assignmentPrefix") String assignmentPrefix);
```

---

## 2. Replace `getGroupWeeklySummaries()` in `WtvService.java`

```java
/**
 * Optimized: Single query replaces N+1 queries
 */
public List<WeeklyTimeSummaryDTO> getGroupWeeklySummaries(
        LocalDate startDate, 
        LocalDate endDate,
        String assignmentNumberFilter) {
    
    log.debug("Fetching group summaries for {} to {}, filter: {}", 
            startDate, endDate, assignmentNumberFilter);

    List<Object[]> results;
    if (assignmentNumberFilter != null && !assignmentNumberFilter.isBlank()) {
        results = entempRepository.findWeeklySummariesByAssignmentOptimized(
                startDate, endDate, assignmentNumberFilter + "%");
    } else {
        results = entempRepository.findWeeklySummariesOptimized(startDate, endDate);
    }

    log.debug("Query returned {} rows", results.size());

    return results.stream()
            .map(this::mapToWeeklySummaryDTO)
            .collect(Collectors.toList());
}

/**
 * Map native query result to DTO
 */
private WeeklyTimeSummaryDTO mapToWeeklySummaryDTO(Object[] row) {
    BigDecimal tourDutyHours = toBigDecimal(row[3]);
    BigDecimal adjustmentHours = toBigDecimal(row[4]);
    BigDecimal scheduleHours = toBigDecimal(row[5]);
    BigDecimal codeDirectHours = toBigDecimal(row[6]);
    BigDecimal overheadHours = toBigDecimal(row[7]);
    BigDecimal timetinHours = toBigDecimal(row[8]);
    
    // Tour of Duty = tourDutyHours + timetinHours - adjustmentHours - scheduleHours
    BigDecimal tourOfDuty = tourDutyHours
            .add(timetinHours)
            .subtract(adjustmentHours)
            .subtract(scheduleHours);
    
    // Adjusted Tour = adjustmentHours - scheduleHours
    BigDecimal adjustedTour = adjustmentHours.subtract(scheduleHours);
    
    // Report Days
    int timenonDays = toInt(row[9]);
    int uniqueTimetinDays = toInt(row[10]);
    int reportDays = timenonDays + uniqueTimetinDays;
    
    // Last Date EOD
    LocalDate lastDateEod = row[11] != null ? ((java.sql.Date) row[11]).toLocalDate() : null;
    
    // Tour type
    Integer tour = toInt(row[2]);
    String tourType = decodeTourType(tour);

    return WeeklyTimeSummaryDTO.builder()
            .assignmentNumber(toLong(row[0]))
            .employeeName((String) row[1])
            .tourOfDutyHours(tourOfDuty)
            .adjustedTour(adjustedTour)
            .hoursWorked(timetinHours)
            .caseDirectTime(timetinHours)
            .codeDirectTime(codeDirectHours)
            .overheadTime(overheadHours)
            .reportDays(reportDays)
            .tourOfDutyType(tourType)
            .lastDateEod(lastDateEod != null ? lastDateEod.toString() : null)
            .tour(tour)
            .build();
}

private BigDecimal toBigDecimal(Object value) {
    if (value == null) return BigDecimal.ZERO;
    if (value instanceof BigDecimal) return (BigDecimal) value;
    if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
    return BigDecimal.ZERO;
}

private int toInt(Object value) {
    if (value == null) return 0;
    if (value instanceof Number) return ((Number) value).intValue();
    return 0;
}

private Long toLong(Object value) {
    if (value == null) return null;
    if (value instanceof Number) return ((Number) value).longValue();
    return null;
}

private String decodeTourType(Integer tour) {
    if (tour == null) return "-";
    return switch (tour) {
        case 1 -> "REG";
        case 2 -> "5/4/9";
        case 3 -> "4/10";
        case 4 -> "PT";
        case 5 -> "MAXI";
        default -> "-";
    };
}
```

---

## Performance Comparison

| Approach | Employees | Queries | Estimated Time |
|----------|-----------|---------|----------------|
| **Old (N+1)** | 1,000 | ~10,001 | 30-60 seconds |
| **New (Single)** | 1,000 | **1** | 1-3 seconds |

This should dramatically improve your response time!
