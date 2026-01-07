package com.entity.wtv.repository;

import com.entity.wtv.entity.Ent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ENT table (Case/TIN Master)
 * 
 * Used for TIN display information in employee detail view.
 * Join: TIMETIN.TIMESID = ENT.TINSID
 */
@Repository
public interface EntRepository extends JpaRepository<Ent, Long> {

    Optional<Ent> findByTinsid(Long tinsid);

    @Query("SELECT e FROM Ent e WHERE e.tinsid IN :tinsids")
    List<Ent> findByTinsidIn(@Param("tinsids") List<Long> tinsids);

    @Query("SELECT e.tin FROM Ent e WHERE e.tinsid = :tinsid")
    Optional<Long> findTinByTinsid(@Param("tinsid") Long tinsid);

    @Query("SELECT e.tp FROM Ent e WHERE e.tinsid = :tinsid")
    Optional<String> findTaxpayerNameByTinsid(@Param("tinsid") Long tinsid);
}
