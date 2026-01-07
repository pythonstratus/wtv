package com.entity.wtv.repository;

import com.entity.wtv.entity.Entcode;
import com.entity.wtv.entity.EntcodeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ENTCODE table (Time Code Reference)
 * 
 * TIMEDEF categories used in WTV calculations:
 * - Tour/Duty: M, U, C, G, N, R, O, E
 * - Code Direct: G, M, C, U, N, E
 * - Overhead: O, R
 * - Adjustment: A (negated), S (schedule)
 * - Info: I
 */
@Repository
public interface EntcodeRepository extends JpaRepository<Entcode, EntcodeId> {

    /**
     * Find time code by CODE and TYPE
     */
    @Query("SELECT e FROM Entcode e WHERE e.code = :code AND e.type = :type")
    Optional<Entcode> findByCodeAndType(@Param("code") String code, @Param("type") String type);

    /**
     * Find all active time codes
     * Active: ACTIVE in ('Y', 'C')
     */
    @Query("""
        SELECT e FROM Entcode e 
        WHERE e.active IN ('Y', 'C') 
        AND e.type = 'T'
        ORDER BY e.code
        """)
    List<Entcode> findAllActiveTimeCodes();

    /**
     * Find active time codes by TIMEDEF category
     */
    @Query("""
        SELECT e FROM Entcode e 
        WHERE e.active IN ('Y', 'C') 
        AND e.type = 'T'
        AND e.timedef = :timedef
        ORDER BY e.code
        """)
    List<Entcode> findByTimedef(@Param("timedef") String timedef);

    /**
     * Find Tour/Duty time codes (TIMEDEF in M,U,C,G,N,R,O,E)
     */
    @Query("""
        SELECT e FROM Entcode e 
        WHERE e.active IN ('Y', 'C') 
        AND e.type = 'T'
        AND e.timedef IN ('M', 'U', 'C', 'G', 'N', 'R', 'O', 'E')
        ORDER BY e.code
        """)
    List<Entcode> findTourDutyCodes();

    /**
     * Find Code Direct time codes (TIMEDEF in G,M,C,U,N,E)
     */
    @Query("""
        SELECT e FROM Entcode e 
        WHERE e.active IN ('Y', 'C') 
        AND e.type = 'T'
        AND e.timedef IN ('G', 'M', 'C', 'U', 'N', 'E')
        ORDER BY e.code
        """)
    List<Entcode> findCodeDirectCodes();

    /**
     * Find Overhead time codes (TIMEDEF in O,R)
     */
    @Query("""
        SELECT e FROM Entcode e 
        WHERE e.active IN ('Y', 'C') 
        AND e.type = 'T'
        AND e.timedef IN ('O', 'R')
        ORDER BY e.code
        """)
    List<Entcode> findOverheadCodes();

    /**
     * Find Adjustment time codes (TIMEDEF = 'A')
     */
    @Query("""
        SELECT e FROM Entcode e 
        WHERE e.active IN ('Y', 'C') 
        AND e.type = 'T'
        AND e.timedef = 'A'
        ORDER BY e.code
        """)
    List<Entcode> findAdjustmentCodes();

    /**
     * Find Schedule time codes (TIMEDEF = 'S')
     */
    @Query("""
        SELECT e FROM Entcode e 
        WHERE e.active IN ('Y', 'C') 
        AND e.type = 'T'
        AND e.timedef = 'S'
        ORDER BY e.code
        """)
    List<Entcode> findScheduleCodes();

    /**
     * Get code name (CDNAME) by code
     */
    @Query("""
        SELECT e.cdname FROM Entcode e 
        WHERE e.code = :code 
        AND e.type = 'T'
        AND e.active IN ('Y', 'C')
        """)
    Optional<String> findCodeNameByCode(@Param("code") String code);

    /**
     * Get all code values for a specific TIMEDEF category (for subqueries)
     */
    @Query("""
        SELECT e.code FROM Entcode e 
        WHERE e.active IN ('Y', 'C') 
        AND e.type = 'T'
        AND e.timedef = :timedef
        """)
    List<String> findCodesByTimedef(@Param("timedef") String timedef);

    /**
     * Get all code values for multiple TIMEDEF categories
     */
    @Query("""
        SELECT e.code FROM Entcode e 
        WHERE e.active IN ('Y', 'C') 
        AND e.type = 'T'
        AND e.timedef IN :timedefs
        """)
    List<String> findCodesByTimedefs(@Param("timedefs") List<String> timedefs);
}
