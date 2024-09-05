package com.example.ITTools.infrastructure.entrypoints.Server.Repositories;

import com.example.ITTools.infrastructure.entrypoints.Server.Models.ServerBD_Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface ServerBD_Repository extends JpaRepository<ServerBD_Model, Integer> {
    boolean existsByRegion_IdRegion(Long idRegion);
    boolean existsByServerName(String serverName);
    boolean existsByIpServer(String ipServer);
}
