package com.example.ITTools.infrastructure.entrypoints.Region.Repositories;

import com.example.ITTools.infrastructure.entrypoints.Region.Models.RegionModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface RegionRepository extends JpaRepository<RegionModel, Long> {
    Optional<RegionModel> findByNameRegion(String nameRegion);
    List<RegionModel> findByStatus(int status);
    boolean existsByNameRegion(String nameRegion);
    Optional<RegionModel> findByIdRegionAndStatus(Integer idRegion, int status);
}
