package com.entity.wtv.repository;

import com.entity.wtv.entity.Timetin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for TIMETIN table (Case/TIN Time Entries)
 * 
 * Contains queries matching legacy Pro*C logic from entity_common.pc:
 * - getTimeVerifyData() - Hours Worked, Case Direct Time
 * - getTimeVerifyTinData() - Employee detail case time by TIN
 */
@Repository
public interface TimetinRepository extends JpaRepository<Timetin, Long> {

    List<Timetin> findByRoidAndRptdtBetween(Long roid, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(t.hours), 0) FROM Timetin t WHERE t.roid = :roid AND t.rptdt BETWEEN :startDate AND :endDate")
    BigDecimal sumHoursWorked(@Param("roid") Long roid, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(DISTINCT t.rptdt) FROM Timetin t WHERE t.roid = :roid AND t.rptdt BETWEEN :startDate AND :endDate")
    Integer countDistinctReportDays(@Param("roid") Long roid, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT MAX(t.rptdt) FROM Timetin t WHERE t.roid = :roid AND t.rptdt BETWEEN :startDate AND :endDate")
    LocalDate findMaxReportDate(@Param("roid") Long roid, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT DISTINCT t.timesid FROM Timetin t WHERE t.roid = :roid AND t.rptdt BETWEEN :startDate AND :endDate")
    List<Long> findDistinctTimesids(@Param("roid") Long roid, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(t.hours), 0) FROM Timetin t WHERE t.roid = :roid AND t.timesid = :timesid AND t.rptdt = :rptdt")
    BigDecimal sumHoursForTimesidAndDate(@Param("roid") Long roid, @Param("timesid") Long timesid, @Param("rptdt") LocalDate rptdt);

    // Count days in TIMETIN that don't exist in TIMENON (for report days calculation)
    @Query("""
        SELECT COUNT(DISTINCT t.rptdt) FROM Timetin t 
        WHERE t.roid = :roid AND t.rptdt BETWEEN :startDate AND :endDate
        AND NOT EXISTS (SELECT 1 FROM Timenon n WHERE n.roid = t.roid AND n.rptdt = t.rptdt)
        """)
    Integer countUniqueTimetinDays(@Param("roid") Long roid, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
