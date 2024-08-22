package com.example.ITTools.infrastructure.entrypoints.Service.Repositories;

import com.example.ITTools.infrastructure.entrypoints.Service.Models.ServiceModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceModel, Integer> {
    List<ServiceModel> findByServer_IdAgent(int idAgent);

}
