package com.example.ITTools.infrastructure.entrypoints.Server.Services;

import com.example.ITTools.infrastructure.entrypoints.Audit.Service.AuditService;
import com.example.ITTools.infrastructure.entrypoints.Region.Models.RegionModel;
import com.example.ITTools.infrastructure.entrypoints.Region.Services.RegionService;
import com.example.ITTools.infrastructure.entrypoints.Server.DTO.ServerBD_DTO;
import com.example.ITTools.infrastructure.entrypoints.Server.Models.ServerBD_Model;
import com.example.ITTools.infrastructure.entrypoints.Server.Repositories.ServerBD_Repository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ServerBD_Service {

    @Autowired
    private ServerBD_Repository serverRepository;

    @Autowired
    private RegionService regionService;
    @Autowired
    private AuditService auditService;

    public List<ServerBD_DTO> getAllServers() {
        List<ServerBD_Model> servers = serverRepository.findAll();
        return servers.stream().map(ServerBD_Model::toDTO).collect(Collectors.toList());
    }

    public ServerBD_DTO getServerById(int id) {
        ServerBD_Model server = serverRepository.findById(id).orElseThrow(() -> new RuntimeException("Server not found"));
        return server.toDTO();
    }

    public ServerBD_DTO createServer(ServerBD_DTO serverDTO, HttpServletRequest request) {
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

        if (serverDTO.getIpServer() == null || serverDTO.getIpServer().isEmpty()) {
            throw new IllegalArgumentException("Server IP cannot be empty");
        }

        ServerBD_Model serverModel = new ServerBD_Model();
        serverModel.setServerName(serverDTO.getServerName());
        serverModel.setDescription(serverDTO.getDescription());
        serverModel.setIpServer(serverDTO.getIpServer());
        serverModel.setPortServer(serverDTO.getPortServer());
        serverModel.setInstance(serverDTO.getInstance());
        serverModel.setServerDB(serverDTO.getServerDB());
        serverModel.setUserLogin(serverDTO.getUserLogin());
        serverModel.setPassword(serverDTO.getPassword());
        serverModel.setDbFR(serverDTO.getDbFR());
        serverModel.setStatus(1);
        serverModel.setRegion(region);

        ServerBD_Model savedServer = serverRepository.save(serverModel);
        auditService.audit("Create ServerDB: "+ savedServer.getServerName() + ", id "+savedServer.getIdServer(),request);
        return savedServer.toDTO();
    }

    public ServerBD_DTO updateServer(int id, ServerBD_DTO serverDTO, HttpServletRequest request) {
        ServerBD_Model server = serverRepository.findById(id).orElseThrow(() -> new RuntimeException("Server not found"));

        server.setServerName(serverDTO.getServerName());
        server.setIpServer(serverDTO.getIpServer());
        server.setInstance(serverDTO.getInstance());
        server.setPassword(serverDTO.getPassword());
        server.setPortServer(serverDTO.getPortServer());
        server.setServerDB(serverDTO.getServerDB());
        server.setDbFR(serverDTO.getDbFR());

        if (serverDTO.getRegionId() != null) {
            RegionModel region = regionService.getRegionById(serverDTO.getRegionId())
                    .orElseThrow(() -> new RuntimeException("Region not found"));
            server.setRegion(region);
        }

        ServerBD_Model updatedServer = serverRepository.save(server);
        auditService.audit("Update ServerDB: "+ updatedServer.getServerName() + ", id "+updatedServer.getIdServer(), request);
        return updatedServer.toDTO();
    }

    public void updateServerStatus(int id, HttpServletRequest request) {
        // Recuperar el servidor por ID
        ServerBD_Model server = serverRepository.findById(id).orElseThrow(() -> new RuntimeException("Server not found"));

        // Cambiar el estado del servidor (si está en 1, cambiar a 0 y viceversa)
        server.setStatus(server.getStatus() == 1 ? 0 : 1);

        // Guardar el servidor actualizado
        auditService.audit("Update ServerDbStatus: " + server.getServerName()+ ", id "+id, request);
        serverRepository.save(server);
    }

    public ServerBD_DTO deleteServerDB (Integer id, HttpServletRequest request){
        Optional<ServerBD_Model> optionalServerBDModel = serverRepository.findById(id);
        ServerBD_Model deleteServerDB = optionalServerBDModel.get();
        serverRepository.deleteById(id);
        auditService.audit("Delete ServerDB: "+deleteServerDB.getServerName()+ ", id "+ deleteServerDB.getIdServer(),request);
        return  deleteServerDB.toDTO();
    }

}



