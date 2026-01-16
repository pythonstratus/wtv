Let me check the `getWeeksForMonth` implementation to see why workdays, holidays, and hours aren't being populated:I found the issue! The `generateWeeksForMonth` method is **not setting** `workdays`, `holidays`, or `hours` in the WeekDTO builder. It's only setting `weekNumber`, `postingCycle`, `startDate`, `endDate`, and `displayLabel`.

---

## The Problem

Current code:
```java
weeks.add(ReportingMonthDTO.WeekDTO.builder()
        .weekNumber(i)
        .postingCycle(postingCycle++)
        .startDate(weekStart)
        .endDate(weekEnd)
        .displayLabel(formatDateRange(weekStart, weekEnd))
        .build());  // ❌ Missing: workdays, holidays, hours
```

---

## The Fix

Replace the `generateWeeksForMonth` method in `WtvService.java` with this:

```java
private List<ReportingMonthDTO.WeekDTO> generateWeeksForMonth(Entmonth month) {
    List<ReportingMonthDTO.WeekDTO> weeks = new ArrayList<>();
    
    if (month.getStartdt() == null || month.getWeeks() == null) {
        return weeks;
    }

    // First, try to parse WEEK_DATA if it exists
    if (month.getWeekData() != null && !month.getWeekData().isBlank()) {
        weeks = parseWeekDataJson(month.getWeekData());
        if (!weeks.isEmpty()) {
            return weeks;
        }
    }

    // Fallback: Calculate weeks from month data
    LocalDate weekStart = month.getStartdt();
    int numWeeks = month.getWeeks();
    int postingCycle = month.getStartcyc() != null ? month.getStartcyc() : 1;
    
    // Distribute workdays across weeks
    int totalWorkdays = month.getWorkdays() != null ? month.getWorkdays() : numWeeks * 5;
    int totalHolidays = month.getHolidays() != null ? month.getHolidays() : 0;
    int workdaysRemaining = totalWorkdays;

    for (int i = 1; i <= numWeeks; i++) {
        LocalDate weekEnd = weekStart.plusDays(6);
        
        // Calculate workdays for this week (last week gets remainder)
        int weekWorkdays;
        if (i == numWeeks) {
            weekWorkdays = workdaysRemaining;
        } else {
            weekWorkdays = Math.min(5, workdaysRemaining);
        }
        workdaysRemaining -= weekWorkdays;
        
        // Calculate holidays (5 possible workdays - actual workdays)
        int weekHolidays = Math.max(0, 5 - weekWorkdays);
        
        // Calculate hours (8 hours per workday)
        int weekHours = weekWorkdays * 8;
        
        weeks.add(ReportingMonthDTO.WeekDTO.builder()
                .weekNumber(i)
                .postingCycle(postingCycle++)
                .startDate(weekStart)
                .endDate(weekEnd)
                .displayLabel(formatDateRange(weekStart, weekEnd))
                .workdays(weekWorkdays)
                .holidays(weekHolidays)
                .hours(weekHours)
                .build());
        
        weekStart = weekStart.plusDays(7);
    }

    return weeks;
}

/**
 * Parse WEEK_DATA JSON column if populated
 */
private List<ReportingMonthDTO.WeekDTO> parseWeekDataJson(String weekDataJson) {
    List<ReportingMonthDTO.WeekDTO> weeks = new ArrayList<>();
    
    try {
        // Simple JSON parsing without external library
        // Expected format: [{"cycle":202544,"weekNumber":1,"startDate":"2025-10-26",...},...]
        if (weekDataJson == null || weekDataJson.isBlank()) {
            return weeks;
        }
        
        // Remove outer brackets
        String content = weekDataJson.trim();
        if (content.startsWith("[")) content = content.substring(1);
        if (content.endsWith("]")) content = content.substring(0, content.length() - 1);
        
        // Split by "},{"
        String[] weekObjects = content.split("\\},\\s*\\{");
        
        for (String weekObj : weekObjects) {
            // Clean up the object string
            String obj = weekObj.replace("{", "").replace("}", "");
            
            // Parse fields
            Integer cycle = extractIntValue(obj, "cycle");
            Integer weekNumber = extractIntValue(obj, "weekNumber");
            String startDateStr = extractStringValue(obj, "startDate");
            String endDateStr = extractStringValue(obj, "endDate");
            Integer workdays = extractIntValue(obj, "workdays");
            Integer holidays = extractIntValue(obj, "holidays");
            Integer hours = extractIntValue(obj, "hours");
            
            LocalDate startDate = startDateStr != null ? LocalDate.parse(startDateStr) : null;
            LocalDate endDate = endDateStr != null ? LocalDate.parse(endDateStr) : null;
            
            weeks.add(ReportingMonthDTO.WeekDTO.builder()
                    .weekNumber(weekNumber)
                    .postingCycle(cycle)
                    .startDate(startDate)
                    .endDate(endDate)
                    .displayLabel(formatDateRange(startDate, endDate))
                    .workdays(workdays)
                    .holidays(holidays)
                    .hours(hours)
                    .build());
        }
    } catch (Exception e) {
        log.warn("Failed to parse WEEK_DATA JSON: {}", e.getMessage());
    }
    
    return weeks;
}

private Integer extractIntValue(String json, String key) {
    try {
        String pattern = "\"" + key + "\"\\s*:\\s*(\\d+)";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(json);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
    } catch (Exception e) {
        // Ignore
    }
    return null;
}

private String extractStringValue(String json, String key) {
    try {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(json);
        if (m.find()) {
            return m.group(1);
        }
    } catch (Exception e) {
        // Ignore
    }
    return null;
}
```

---

## Summary

| Field | Before | After |
|-------|--------|-------|
| `workdays` | ❌ Not set (null) | ✅ Calculated or from WEEK_DATA |
| `holidays` | ❌ Not set (null) | ✅ Calculated or from WEEK_DATA |
| `hours` | ❌ Not set (null) | ✅ Calculated or from WEEK_DATA |

---

## How It Works Now

1. **If WEEK_DATA exists** → Parse JSON and use stored values
2. **If WEEK_DATA is empty** → Calculate from ENTMONTH.WORKDAYS and distribute across weeks

This ensures you get the correct values whether or not you've run the WEEK_DATA population script!
