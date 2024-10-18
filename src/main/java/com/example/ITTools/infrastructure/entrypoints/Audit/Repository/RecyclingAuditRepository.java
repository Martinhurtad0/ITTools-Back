package com.example.ITTools.infrastructure.entrypoints.Audit.Repository;

import com.example.ITTools.infrastructure.entrypoints.Audit.Model.RecyclingAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecyclingAuditRepository extends JpaRepository <RecyclingAudit, Integer> {
}
