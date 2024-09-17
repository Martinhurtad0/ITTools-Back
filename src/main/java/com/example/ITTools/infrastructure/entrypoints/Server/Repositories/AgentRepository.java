package com.example.ITTools.infrastructure.entrypoints.Server.Repositories;


import com.example.ITTools.infrastructure.entrypoints.Server.Models.AgentModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentRepository extends JpaRepository<AgentModel, Integer> {
    List<AgentModel> findByRegion_IdRegion(Long idRegion);
    boolean existsByRegion_IdRegion(Long idRegion);
    boolean existsByAgentName(String agentName);
    boolean existsByIPAgent(String IPAgent);
    boolean existsByWebServiceUrl(String webServiceUrl);


}
