package com.entity.wtv.repository;

import com.entity.wtv.entity.Timenon;
import com.entity.wtv.entity.TimenonId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for TIMENON table (Non-Case Time Entries)
 * 
 * Contains queries matching legacy Pro*C logic from entity_common.pc
 */
@Repository
public interface TimenonRepository extends JpaRepository<Timenon, TimenonId> {

    List<Timenon> findByRoidAndRptdtBetween(Long roid, LocalDate startDate, LocalDate endDate);

    @Query("""
        SELECT COALESCE(SUM(t.hours), 0) FROM Timenon t 
        WHERE t.roid = :roid AND t.rptdt BETWEEN :startDate AND :endDate
        AND t.timecode IN (SELECT e.code FROM Entcode e WHERE e.active IN ('Y','C') AND e.timedef IN ('M','U','C','G','N','R','O','E'))
        """)
    BigDecimal sumTourDutyHours(@Param("roid") Long roid, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT COALESCE(SUM(t.hours), 0) FROM Timenon t 
        WHERE t.roid = :roid AND t.rptdt BETWEEN :startDate AND :endDate
        AND t.timecode IN (SELECT e.code FROM Entcode e WHERE e.active IN ('Y','C') AND e.timedef = 'A')
        """)
    BigDecimal sumAdjustmentHours(@Param("roid") Long roid, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT COALESCE(SUM(t.hours), 0) FROM Timenon t 
        WHERE t.roid = :roid AND t.rptdt BETWEEN :startDate AND :endDate
        AND t.timecode IN (SELECT e.code FROM Entcode e WHERE e.active IN ('Y','C') AND e.timedef = 'S')
        """)
    BigDecimal sumScheduleHours(@Param("roid") Long roid, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT COALESCE(SUM(t.hours), 0) FROM Timenon t 
        WHERE t.roid = :roid AND t.rptdt BETWEEN :startDate AND :endDate
        AND t.timecode IN (SELECT e.code FROM Entcode e WHERE e.active IN ('Y','C') AND e.timedef IN ('G','M','C','U','N','E'))
        """)
    BigDecimal sumCodeDirectHours(@Param("roid") Long roid, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT COALESCE(SUM(t.hours), 0) FROM Timenon t 
        WHERE t.roid = :roid AND t.rptdt BETWEEN :startDate AND :endDate
        AND t.timecode IN (SELECT e.code FROM Entcode e WHERE e.active IN ('Y','C') AND e.timedef IN ('O','R'))
        """)
    BigDecimal sumOverheadHours(@Param("roid") Long roid, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Count distinct report days, excluding holidays (750) and non-work days (760)
     * These codes are not counted in Employee work hours per business rules.
     */
    @Query("SELECT COUNT(DISTINCT t.rptdt) FROM Timenon t WHERE t.roid = :roid AND t.rptdt BETWEEN :startDate AND :endDate AND t.timecode NOT IN ('750', '760')")
    Integer countDistinctReportDays(@Param("roid") Long roid, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT MAX(t.rptdt) FROM Timenon t WHERE t.roid = :roid AND t.rptdt BETWEEN :startDate AND :endDate")
    LocalDate findMaxReportDate(@Param("roid") Long roid, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT DISTINCT t.timecode FROM Timenon t WHERE t.roid = :roid AND t.rptdt BETWEEN :startDate AND :endDate ORDER BY t.timecode")
    List<String> findDistinctTimecodes(@Param("roid") Long roid, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(t.hours), 0) FROM Timenon t WHERE t.roid = :roid AND t.rptdt = :rptdt AND t.timecode = :timecode")
    BigDecimal sumHoursForDateAndCode(@Param("roid") Long roid, @Param("rptdt") LocalDate rptdt, @Param("timecode") String timecode);
}