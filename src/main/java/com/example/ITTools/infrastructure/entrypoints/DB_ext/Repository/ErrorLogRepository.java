package com.example.ITTools.infrastructure.entrypoints.DB_ext.Repository;

import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.ErrorLog;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ErrorLogRepository  extends JpaRepository<ErrorLog, Integer> {
    List<ErrorLog> findBySp(String sp);

    @Transactional
    @Modifying
    @Query("DELETE FROM ErrorLog e WHERE e.sp = :sp")
    void deleteBySp(@Param("sp") String sp);
}
