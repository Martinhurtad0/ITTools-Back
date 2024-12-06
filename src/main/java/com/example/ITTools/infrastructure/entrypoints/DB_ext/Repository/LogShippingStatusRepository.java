package com.example.ITTools.infrastructure.entrypoints.DB_ext.Repository;

import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.LogShippingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LogShippingStatusRepository extends JpaRepository<LogShippingStatus, Integer> {

    Optional<LogShippingStatus> findByIpAndPrimaryDatabase(String ip, String primaryDatabase);
}
