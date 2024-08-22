package com.example.ITTools.infrastructure.entrypoints.Service.Services;

import com.example.ITTools.infrastructure.entrypoints.Server.Repositories.AgentRepository;
import com.example.ITTools.infrastructure.entrypoints.Service.DTO.ServiceDTO;
import com.example.ITTools.infrastructure.entrypoints.Service.Models.ServiceModel;
import com.example.ITTools.infrastructure.entrypoints.Service.Repositories.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceService {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private AgentRepository serverRepository;

    // Método para crear un nuevo servicio
    public ServiceDTO createService(ServiceDTO serviceDTO) {
        // Verificar que se ha proporcionado un ID de servidor
        if (serviceDTO.getIdAgent() == null) {
            throw new IllegalArgumentException("El servidor es obligatorio");
        }

        // Verificar que el servidor existe
        ServiceModel service = new ServiceModel();
        service.setServiceName(serviceDTO.getServiceName());
        service.setDescription(serviceDTO.getDescription());
        service.setCommand(serviceDTO.getCommand());
        service.setPathCommand(serviceDTO.getPathCommand());
        service.setLogFile(serviceDTO.getLogFile());
        service.setType(serviceDTO.getType());

        service.setServer(serverRepository.findById(serviceDTO.getIdAgent())
                .orElseThrow(() -> new RuntimeException("Servidor no encontrado")));

        ServiceModel savedService = serviceRepository.save(service);
        return savedService.toDTO();
    }

    // Método para obtener todos los servicios
    public List<ServiceDTO> getAllServices() {
        List<ServiceModel> services = serviceRepository.findAll();
        return services.stream().map(ServiceModel::toDTO).toList();
    }

    // Método para obtener un servicio por su ID
    public ServiceDTO getServiceById(int idService) {
        ServiceModel service = serviceRepository.findById(idService)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        return service.toDTO();
    }

    // Método para actualizar un servicio existente
    public ServiceDTO updateService(int idService, ServiceDTO serviceDTO) {
        ServiceModel service = serviceRepository.findById(idService)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        service.setServiceName(serviceDTO.getServiceName());
        service.setDescription(serviceDTO.getDescription());
        service.setCommand(serviceDTO.getCommand());
        service.setPathCommand(serviceDTO.getPathCommand());
        service.setLogFile(serviceDTO.getLogFile());
        service.setType(serviceDTO.getType());

        // Actualizar el servidor asociado, si es necesario
        if (serviceDTO.getIdAgent() != null) {
            service.setServer(serverRepository.findById(serviceDTO.getIdAgent())
                    .orElseThrow(() -> new RuntimeException("Servidor no encontrado")));
        }

        ServiceModel updatedService = serviceRepository.save(service);
        return updatedService.toDTO();
    }

    // Método para obtener los servicios por servidor
    public List<ServiceDTO> getServicesByServerId(int idAgent) {
        List<ServiceModel> services = serviceRepository.findByServer_IdAgent(idAgent);
        return services.stream().map(ServiceModel::toDTO).toList();
    }
}
