package com.example.ITTools.infrastructure.entrypoints.HttpRequest.Repositories;

import com.example.ITTools.infrastructure.entrypoints.HttpRequest.RequestLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestLogRepository extends JpaRepository<RequestLog, Long> {
}
