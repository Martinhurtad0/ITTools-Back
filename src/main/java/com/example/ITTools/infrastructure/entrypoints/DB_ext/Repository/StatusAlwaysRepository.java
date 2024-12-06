package com.example.ITTools.infrastructure.entrypoints.DB_ext.Repository;

import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.StatusAlways;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface StatusAlwaysRepository extends JpaRepository<StatusAlways, Integer> {
}
