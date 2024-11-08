package com.example.ITTools.infrastructure.entrypoints.Audit.Repository;

import com.example.ITTools.infrastructure.entrypoints.Audit.Model.RecyclingAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecyclingAuditRepository extends JpaRepository <RecyclingAudit, Integer> {
}
