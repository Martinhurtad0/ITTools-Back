package com.example.ITTools.infrastructure.entrypoints.DB_ext.Repository;

import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErrorLogRepository  extends JpaRepository<ErrorLog, Integer> {
}
