package com.example.ITTools.infrastructure.entrypoints.Service.Controllers;



import com.example.ITTools.infrastructure.entrypoints.Service.DTO.ServiceDTO;


import com.example.ITTools.infrastructure.entrypoints.Service.Services.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/services")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

    // Crear un nuevo servicio
    @PostMapping("register")
    public ResponseEntity<ServiceDTO> createService(@RequestBody ServiceDTO serviceDTO) {
        ServiceDTO createdService = serviceService.createService(serviceDTO);
        return ResponseEntity.ok(createdService);
    }

    // Obtener todos los servicios
    @GetMapping
    public ResponseEntity<List<ServiceDTO>> getAllServices() {
        List<ServiceDTO> services = serviceService.getAllServices();
        return ResponseEntity.ok(services);
    }

    // Obtener un servicio por ID
    @GetMapping("/{id}")
    public ResponseEntity<ServiceDTO> getServiceById(@PathVariable("id") Integer id) {
        ServiceDTO service = serviceService.getServiceById(id);
        return ResponseEntity.ok(service);
    }

    // Actualizar un servicio existente
    @PutMapping("/{id}")
    public ResponseEntity<ServiceDTO> updateService(@PathVariable("id") Integer id, @RequestBody ServiceDTO serviceDTO) {
        ServiceDTO updatedService = serviceService.updateService(id, serviceDTO);
        return ResponseEntity.ok(updatedService);
    }


}

