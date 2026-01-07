package com.entity.wtv.repository;

import com.entity.wtv.entity.Entmonth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ENTMONTH table
 * Used by both WTV and CTRS Calendar services
 * 
 * Primary Key: RPTMONTH (e.g., "OCT2026")
 */
@Repository
public interface EntmonthRepository extends JpaRepository<Entmonth, String> {

    // =========================================================================
    // WTV Queries
    // =========================================================================

    /**
     * Find all reporting months ordered by start date descending
     */
    @Query("SELECT e FROM Entmonth e ORDER BY e.startdt DESC")
    List<Entmonth> findAllOrderByStartDateDesc();

    /**
     * Find pay period containing a specific date
     */
    @Query("SELECT e FROM Entmonth e WHERE e.startdt <= :date AND e.enddt >= :date")
    Optional<Entmonth> findByDateWithin(@Param("date") LocalDate date);

    /**
     * Find reporting month by date - gets the most recent period starting on or before date
     */
    @Query("SELECT e FROM Entmonth e WHERE e.startdt <= :viewDate ORDER BY e.startdt DESC LIMIT 1")
    Optional<Entmonth> findByDateContaining(@Param("viewDate") LocalDate viewDate);

    /**
     * Find pay period by start date
     */
    Optional<Entmonth> findByStartdt(LocalDate startdt);

    /**
     * Find by RPTMONTH value (e.g., "OCT2026")
     */
    Optional<Entmonth> findByRptmonth(String rptmonth);

    /**
     * Find previous reporting month
     */
    @Query("SELECT e FROM Entmonth e WHERE e.startdt < :currentStartDate ORDER BY e.startdt DESC LIMIT 1")
    Optional<Entmonth> findPreviousMonth(@Param("currentStartDate") LocalDate currentStartDate);

    /**
     * Find next reporting month
     */
    @Query("SELECT e FROM Entmonth e WHERE e.startdt > :currentStartDate ORDER BY e.startdt ASC LIMIT 1")
    Optional<Entmonth> findNextMonth(@Param("currentStartDate") LocalDate currentStartDate);

    /**
     * Get reporting month label for a date
     */
    @Query("SELECT e.rptmonth FROM Entmonth e WHERE e.startdt <= :viewDate ORDER BY e.startdt DESC LIMIT 1")
    Optional<String> findRptmonthByDate(@Param("viewDate") LocalDate viewDate);

    // =========================================================================
    // CTRS Calendar Queries
    // =========================================================================

    /**
     * Get distinct fiscal years from RPTMONTH
     * Extracts year portion from RPTMONTH (e.g., "OCT2026" -> 2026)
     */
    @Query("SELECT DISTINCT CAST(SUBSTRING(e.rptmonth, 4, 4) AS integer) FROM Entmonth e ORDER BY 1 DESC")
    List<Integer> findDistinctFiscalYears();

    /**
     * Find all months for a fiscal year by RPTMONTH pattern
     * Matches months where RPTMONTH ends with the year (e.g., "%2026")
     */
    @Query("SELECT e FROM Entmonth e WHERE e.rptmonth LIKE %:year ORDER BY e.startdt")
    List<Entmonth> findByFiscalYearPattern(@Param("year") String year);

    /**
     * Check if fiscal year exists
     */
    @Query("SELECT COUNT(e) > 0 FROM Entmonth e WHERE e.rptmonth LIKE %:year")
    boolean existsByFiscalYear(@Param("year") String year);

    /**
     * Delete all months for a fiscal year
     */
    @Modifying
    @Query("DELETE FROM Entmonth e WHERE e.rptmonth LIKE %:year")
    int deleteByFiscalYear(@Param("year") String year);

    /**
     * Find months by start date range
     */
    @Query("SELECT e FROM Entmonth e WHERE e.startdt BETWEEN :startDate AND :endDate ORDER BY e.startdt")
    List<Entmonth> findByStartdtBetween(@Param("startDate") LocalDate startDate, 
                                         @Param("endDate") LocalDate endDate);

    /**
     * Get the latest end cycle number for a fiscal year
     */
    @Query("SELECT MAX(e.endcyc) FROM Entmonth e WHERE e.rptmonth LIKE %:year")
    Optional<Integer> findMaxEndCycleByFiscalYear(@Param("year") String year);

    /**
     * Get count of months for a fiscal year
     */
    @Query("SELECT COUNT(e) FROM Entmonth e WHERE e.rptmonth LIKE %:year")
    long countByFiscalYear(@Param("year") String year);

    /**
     * Find fiscal year months ordered properly (Oct-Sep)
     * Uses CASE to order months in fiscal year order
     */
    @Query("""
        SELECT e FROM Entmonth e 
        WHERE e.rptmonth LIKE %:year 
        ORDER BY 
            CASE SUBSTRING(e.rptmonth, 1, 3)
                WHEN 'OCT' THEN 1
                WHEN 'NOV' THEN 2
                WHEN 'DEC' THEN 3
                WHEN 'JAN' THEN 4
                WHEN 'FEB' THEN 5
                WHEN 'MAR' THEN 6
                WHEN 'APR' THEN 7
                WHEN 'MAY' THEN 8
                WHEN 'JUN' THEN 9
                WHEN 'JUL' THEN 10
                WHEN 'AUG' THEN 11
                WHEN 'SEP' THEN 12
                ELSE 13
            END
        """)
    List<Entmonth> findByFiscalYearOrdered(@Param("year") String year);
}
