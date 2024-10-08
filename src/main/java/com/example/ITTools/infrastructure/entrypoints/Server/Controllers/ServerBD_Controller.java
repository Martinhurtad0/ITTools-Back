package com.example.ITTools.infrastructure.entrypoints.Server.Controllers;


import com.example.ITTools.infrastructure.entrypoints.Server.DTO.ServerBD_DTO;
import com.example.ITTools.infrastructure.entrypoints.Server.Services.ServerBD_Service;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/serversdb")
public class ServerBD_Controller {

    @Autowired
    private ServerBD_Service serverService;

    @GetMapping
    public List<ServerBD_DTO> getAllServers() {
        return serverService.getAllServers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServerBD_DTO> getServerById(@PathVariable int id) {
        ServerBD_DTO serverDTO = serverService.getServerById(id);
        return ResponseEntity.ok(serverDTO);
    }

    @PostMapping("/register")
    public ResponseEntity<String> createServer(@RequestBody ServerBD_DTO serverDTO, HttpServletRequest request) {
        try {
            ServerBD_DTO createdServer = serverService.createServer(serverDTO, request);
            return ResponseEntity.ok("Server created successfully");
        } catch (IllegalArgumentException e) {
            // Manejo de excepciones específicas para errores de validación
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RuntimeException e) {
            // Manejo de excepciones generales
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateServer(@PathVariable int id, @RequestBody ServerBD_DTO serverDTO, HttpServletRequest request) {
        try {
            ServerBD_DTO updatedServer = serverService.updateServer(id, serverDTO, request);
            return ResponseEntity.ok("Server updated successfully");
        } catch (IllegalArgumentException e) {
            // Manejo de excepciones específicas para errores de validación
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RuntimeException e) {
            // Manejo de excepciones generales
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PatchMapping("/status/{id}")
    public ResponseEntity<Void> updateServerStatus(@PathVariable("id") int id, HttpServletRequest request) {
        try {
            serverService.updateServerStatus(id, request);
            return ResponseEntity.noContent().build(); // Estado 204 No Content
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build(); // Estado 404 Not Found
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ServerBD_DTO> deleteServerDB( @PathVariable("id") int id, HttpServletRequest request){
        ServerBD_DTO deleteServerDB = serverService.deleteServerDB(id, request);
        return  ResponseEntity.ok(deleteServerDB);
    }
}
