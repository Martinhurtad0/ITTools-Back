package com.example.ITTools.infrastructure.entrypoints.Region.Services;

import com.example.ITTools.infrastructure.entrypoints.Audit.Service.AuditService;
import com.example.ITTools.infrastructure.entrypoints.Region.DTO.RegionDTO;
import com.example.ITTools.infrastructure.entrypoints.Region.Exeption.RegionYaExisteException;
import com.example.ITTools.infrastructure.entrypoints.Region.Models.RegionModel;
import com.example.ITTools.infrastructure.entrypoints.Region.Repositories.RegionRepository;
import com.example.ITTools.infrastructure.entrypoints.Server.Repositories.AgentRepository;
import com.example.ITTools.infrastructure.entrypoints.Server.Repositories.ServerBD_Repository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class RegionService {

    @Autowired
    private RegionRepository regionRepository;
    @Autowired
    private ServerBD_Repository serverBDRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private AuditService auditService; // Asegúrate de que auditService esté inyectado

    public List<RegionModel> getAllRegions() {
        return regionRepository.findAll();
    }

    public RegionModel createRegion(RegionModel region, HttpServletRequest request) {
        if (region.getNameRegion() == null || region.getDescription() == null) {
            throw new IllegalArgumentException("The region name and description cannot be null.");
        }

        // Verificar si la región ya existe
        if (regionRepository.findByNameRegion(region.getNameRegion()).isPresent()) {
            // Lanzar una RuntimeException con un mensaje claro
            throw new RuntimeException("The region " + region.getNameRegion() + " already exists");
        }

        region.setStatus(1); // Al crear, la región está activa
        RegionModel createdRegion = regionRepository.save(region);

        // Registrar la auditoría
        auditService.audit("Create Region: " + createdRegion.getNameRegion() + ", id " + createdRegion.getIdRegion(), request);

        return createdRegion;
    }


    public Optional<RegionModel> getRegionById(Long idRegion) {
        return regionRepository.findById(idRegion);
    }

    public RegionModel updateRegion(Long idRegion, RegionModel regionDetails, HttpServletRequest request) {
        RegionModel region = regionRepository.findById(idRegion)
                .orElseThrow(() -> new RuntimeException("Region not found with id " + idRegion));

        // Verificar si el nombre de la región ya existe en otro registro
        if (regionDetails.getNameRegion() != null && !region.getNameRegion().equals(regionDetails.getNameRegion())) {
            boolean nameExists = regionRepository.existsByNameRegion(regionDetails.getNameRegion());
            if (nameExists) {
                throw new RegionYaExisteException("The region " + regionDetails.getNameRegion() + " already exists");
            }
            region.setNameRegion(regionDetails.getNameRegion());
        }

        if (regionDetails.getDescription() != null) {
            region.setDescription(regionDetails.getDescription());
        }

        // No cambiar el estado al actualizar detalles
        RegionModel updatedRegion = regionRepository.save(region);

        // Registrar la auditoría
        auditService.audit("Update Region: " + updatedRegion.getNameRegion() + ", id " + updatedRegion.getIdRegion(), request);

        return updatedRegion;
    }

    public void updateRegionStatus(Long idRegion, int status, HttpServletRequest request) {
        RegionModel region = regionRepository.findById(idRegion)
                .orElseThrow(() -> new RuntimeException("Region not found with id " + idRegion));
        region.setStatus(status);
        regionRepository.save(region);

        // Registrar la auditoría
        auditService.audit("Update Region Status: " + idRegion + " Status: " + status, request);
    }

    public RegionDTO deleteRegion(Long idRegion, HttpServletRequest request) {
        // Verificar si la región existe
        RegionModel deleteRegion = regionRepository.findById(idRegion)
                .orElseThrow(() -> new RuntimeException("Region not found with id " + idRegion));

        // Verificar si la región tiene servidores asociados
        boolean hasServers = serverBDRepository.existsByRegion_IdRegion(idRegion);

        // Verificar si la región tiene agentes asociados
        boolean hasAgents = agentRepository.existsByRegion_IdRegion(idRegion);

        // Si la región tiene servidores o agentes, lanzar una excepción
        if (hasServers || hasAgents) {
            throw new IllegalStateException("Cannot delete region. The region has associated servers or agents.");
        }

        // Si no tiene servidores ni agentes asociados, proceder con la eliminación
        regionRepository.deleteById(idRegion);

        // Registrar la auditoría
        auditService.audit("Delete Region: " + deleteRegion.getNameRegion() + ", id " + deleteRegion.getIdRegion(), request);

        return deleteRegion.toDTO();
    }




}
