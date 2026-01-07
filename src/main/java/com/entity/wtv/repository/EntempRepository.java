package com.entity.wtv.repository;

import com.entity.wtv.entity.Entemp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ENTEMP table with CFF (security filter) support
 * 
 * CFF View Logic (from ENTITYDEV.CFF):
 * - TYPE in ('M','R','C','P','T') with POSTYPE not in ('B','V'), OR TYPE = 'H'
 * - EACTIVE in ('A','Y')
 * - ROID between 21000000 and 36999999
 * - ROID structure validation (positions 1-2: 21-36, 3-4: 01-16, 5-6: 10-58)
 */
@Repository
public interface EntempRepository extends JpaRepository<Entemp, Long> {

    /**
     * Find employee by ROID
     */
    Optional<Entemp> findByRoid(Long roid);

    /**
     * Find employee by SEID
     */
    Optional<Entemp> findBySeid(String seid);

    /**
     * Find all active employees matching CFF criteria
     * This replicates the CFF view logic in JPQL
     */
    @Query("""
        SELECT e FROM Entemp e 
        WHERE e.eactive IN ('A', 'Y')
        AND (
            (e.type IN ('M', 'R', 'C', 'P', 'T') AND (e.postype IS NULL OR e.postype NOT IN ('B', 'V')))
            OR e.type = 'H'
        )
        AND e.roid BETWEEN 21000000 AND 36999999
        ORDER BY e.roid
        """)
    List<Entemp> findAllValidForWtv();

    /**
     * Find employees by ROID range with CFF filtering
     */
    @Query("""
        SELECT e FROM Entemp e 
        WHERE e.eactive IN ('A', 'Y')
        AND (
            (e.type IN ('M', 'R', 'C', 'P', 'T') AND (e.postype IS NULL OR e.postype NOT IN ('B', 'V')))
            OR e.type = 'H'
        )
        AND e.roid BETWEEN :startRoid AND :endRoid
        ORDER BY e.roid, e.tour
        """)
    List<Entemp> findByRoidRangeWithCffFilter(
            @Param("startRoid") Long startRoid, 
            @Param("endRoid") Long endRoid);

    /**
     * Find employees by assignment number prefix (for search)
     */
    @Query("""
        SELECT e FROM Entemp e 
        WHERE e.eactive IN ('A', 'Y')
        AND (
            (e.type IN ('M', 'R', 'C', 'P', 'T') AND (e.postype IS NULL OR e.postype NOT IN ('B', 'V')))
            OR e.type = 'H'
        )
        AND CAST(e.roid AS string) LIKE :assignmentPrefix
        ORDER BY e.roid
        """)
    List<Entemp> findByAssignmentNumberPrefix(@Param("assignmentPrefix") String assignmentPrefix);

    /**
     * Check if ROID is valid according to CFF criteria
     */
    @Query("""
        SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END 
        FROM Entemp e 
        WHERE e.roid = :roid
        AND e.eactive IN ('A', 'Y')
        AND (
            (e.type IN ('M', 'R', 'C', 'P', 'T') AND (e.postype IS NULL OR e.postype NOT IN ('B', 'V')))
            OR e.type = 'H'
        )
        AND e.roid BETWEEN 21000000 AND 36999999
        """)
    boolean isValidRoidForWtv(@Param("roid") Long roid);

    /**
     * Get employee name by ROID
     */
    @Query("SELECT e.name FROM Entemp e WHERE e.roid = :roid")
    Optional<String> findNameByRoid(@Param("roid") Long roid);

    /**
     * Get tour of duty type by ROID
     */
    @Query("SELECT e.tour FROM Entemp e WHERE e.roid = :roid")
    Optional<Integer> findTourByRoid(@Param("roid") Long roid);
}
