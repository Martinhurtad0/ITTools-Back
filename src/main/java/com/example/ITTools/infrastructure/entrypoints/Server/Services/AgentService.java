package com.example.ITTools.infrastructure.entrypoints.Server.Services;

import com.example.ITTools.infrastructure.entrypoints.Audit.Service.AuditService;
import com.example.ITTools.infrastructure.entrypoints.Region.Models.RegionModel;
import com.example.ITTools.infrastructure.entrypoints.Region.Services.RegionService;
import com.example.ITTools.infrastructure.entrypoints.Server.DTO.AgentDTO;
import com.example.ITTools.infrastructure.entrypoints.Server.Models.AgentModel;
import com.example.ITTools.infrastructure.entrypoints.Server.Repositories.AgentRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AgentService {
    @Autowired
    private AgentRepository serverRepository;

    @Autowired
    private RegionService regionService;
    @Autowired
    private AuditService auditService;




    public List<AgentDTO> getAllServers(){
        List<AgentModel> servers = serverRepository.findAll();
        return servers.stream().map(AgentModel::toDTO).collect(Collectors.toList());

    }
    //METODO PARA TRAER EL AGENTE POR EL ID
    public AgentDTO getServerById(int id) {
        AgentModel server = serverRepository.findById(id).orElseThrow(() -> new RuntimeException("Server not found"));
        return server.toDTO();
    }

    //METODO PARA CREAR UN AGENTE
    public AgentDTO createServer(AgentDTO serverDTO, HttpServletRequest request) {
        // Validaciones iniciales
        if (serverDTO.getRegionId() == null) {
            throw new IllegalArgumentException("Region ID must not be null");
        }

        Optional<RegionModel> optionalRegion = regionService.getRegionById(serverDTO.getRegionId());
        if (optionalRegion.isEmpty()) {
            throw new RuntimeException("Region not found with id " + serverDTO.getRegionId());
        }
        RegionModel region = optionalRegion.get();
        if (region.getStatus() != 1) {
            throw new RuntimeException("Region is not active.");
        }

        if (serverDTO.getIPAgent() == null || serverDTO.getIPAgent().isEmpty()) {
            throw new IllegalArgumentException("Server IP cannot be empty");
        }

        // Verificaciones adicionales con mensajes específicos
        if (serverRepository.existsByAgentName(serverDTO.getAgentName())) {
            throw new IllegalArgumentException("The agent " + serverDTO.getAgentName() + " already exists");
        }
        if (serverRepository.existsByIPAgent(serverDTO.getIPAgent())) {
            throw new IllegalArgumentException("Agent with IP " + serverDTO.getIPAgent() + " already exists");
        }
        if (serverRepository.existsByWebServiceUrl(serverDTO.getWebServiceUrl())) {
            throw new IllegalArgumentException("Agent with Web Service URL " + serverDTO.getWebServiceUrl() + " already exists");
        }

        AgentModel serverModel = new AgentModel();
        serverModel.setAgentName(serverDTO.getAgentName());
        serverModel.setIPAgent(serverDTO.getIPAgent());
        serverModel.setWebServiceUrl(serverDTO.getWebServiceUrl());
        serverModel.setPathArchive(serverDTO.getPathArchive());
        serverModel.setStatus(1);
        serverModel.setRegion(region);

        AgentModel savedServer = serverRepository.save(serverModel);
        auditService.audit("Create Agent: " + savedServer.getAgentName() + ", id " + savedServer.getIdAgent(), request);

        return savedServer.toDTO();
    }



    //metodo para traer los AGENTES SEGUN LA REGION

    public List<AgentDTO> getServersByRegion(Long idRegion) {
        List<AgentModel> agents = serverRepository.findByRegion_IdRegion(idRegion);
        return agents.stream()
                .map(AgentModel::toDTO) // Suponiendo que tienes un método toDTO en AgentModel
                .collect(Collectors.toList());
    }


//metodo para editar el Agente
public AgentDTO updateServer(int id, AgentDTO serverDTO, HttpServletRequest request) {
    // Recuperar el agente existente
    AgentModel existingServer = serverRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Server not found"));

    // Verificar si el nuevo nombre ya está en uso por otro agente
    if (!existingServer.getAgentName().equals(serverDTO.getAgentName()) &&
            serverRepository.existsByAgentName(serverDTO.getAgentName())) {
        throw new IllegalArgumentException("The agent " + serverDTO.getAgentName() + " already exists");
    }

    // Verificar si la nueva IP ya está en uso por otro agente
    if (!existingServer.getIPAgent().equals(serverDTO.getIPAgent()) &&
            serverRepository.existsByIPAgent(serverDTO.getIPAgent())) {
        throw new IllegalArgumentException("Agent with IP " + serverDTO.getIPAgent() + " already exists");
    }

    // Verificar si la nueva URL del Web Service ya está en uso por otro agente
    if (!existingServer.getWebServiceUrl().equals(serverDTO.getWebServiceUrl()) &&
            serverRepository.existsByWebServiceUrl(serverDTO.getWebServiceUrl())) {
        throw new IllegalArgumentException("Agent with Web Service URL " + serverDTO.getWebServiceUrl() + " already exists");
    }

    // Actualizar los campos del agente
    existingServer.setAgentName(serverDTO.getAgentName());
    existingServer.setIPAgent(serverDTO.getIPAgent());
    existingServer.setWebServiceUrl(serverDTO.getWebServiceUrl());
    existingServer.setPathArchive(serverDTO.getPathArchive());

    // Actualizar la región si es necesario
    if (serverDTO.getRegionId() != null) {
        RegionModel region = regionService.getRegionById(serverDTO.getRegionId())
                .orElseThrow(() -> new RuntimeException("Region not found"));
        existingServer.setRegion(region);
    }

    // Guardar el agente actualizado
    AgentModel updatedServer = serverRepository.save(existingServer);

    // Auditoría de la acción
    auditService.audit("Update Agent: " + updatedServer.getAgentName() + ", id " + updatedServer.getIdAgent(), request);

    return updatedServer.toDTO();
}



    public void updateServerStatus(int id, HttpServletRequest request) {
        // Recuperar el servidor por ID
        AgentModel server = serverRepository.findById(id).orElseThrow(() -> new RuntimeException("Server not found"));

        // Cambiar el estado del servidor (si está en 1, cambiar a 0 y viceversa)
        server.setStatus(server.getStatus() == 1 ? 0 : 1);

        // Guardar el servidor actualizado
        auditService.audit("Update Agent Status: "+ server.getAgentName() + ", id "+ id , request);
        serverRepository.save(server);
    }

    public AgentDTO deleteAgent(int idAgent, HttpServletRequest request) {
        Optional<AgentModel> agentModelOptional = serverRepository.findById(idAgent);
        AgentModel agent = agentModelOptional.orElseThrow(() -> new RuntimeException("Agent not found with id " + idAgent));

        serverRepository.deleteById(idAgent);

        auditService.audit("Delete Agent: " + agent.getAgentName()+ ", id "+ agent.getIdAgent(), request);

        // Convertir el modelo a DTO antes de devolver
        return agent.toDTO();
    }


}
