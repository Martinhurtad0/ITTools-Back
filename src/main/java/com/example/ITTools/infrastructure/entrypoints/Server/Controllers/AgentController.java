package com.example.ITTools.infrastructure.entrypoints.Server.Controllers;

import com.example.ITTools.infrastructure.entrypoints.Server.DTO.AgentDTO;
import com.example.ITTools.infrastructure.entrypoints.Server.Services.AgentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

    @Autowired
    private AgentService serverService;

    // Obtener todos los servidores
    @GetMapping
    public ResponseEntity<List<AgentDTO>> getAllServers() {
        List<AgentDTO> servers = serverService.getAllServers();
        return new ResponseEntity<>(servers, HttpStatus.OK);
    }

    // Obtener un servidor por ID
    @GetMapping("/{id}")
    public ResponseEntity<AgentDTO> getServerById(@PathVariable int id) {
        AgentDTO server = serverService.getServerById(id);
        return new ResponseEntity<>(server, HttpStatus.OK);
    }

    // Crear un nuevo servidor
    @PostMapping("/register")
    public ResponseEntity<?> createServer(@RequestBody AgentDTO serverDTO, HttpServletRequest request) {
        try {
            AgentDTO createdServer = serverService.createServer(serverDTO, request);
            return ResponseEntity.ok(createdServer);
        } catch (IllegalArgumentException e) {
            // Maneja el caso de nombre o IP duplicado
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            // Maneja otros errores
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    // Actualizar un servidor existente
    @PutMapping("/{id}")
    public ResponseEntity<AgentDTO> updateServer(@PathVariable int id, @RequestBody AgentDTO serverDTO, HttpServletRequest request) {
        AgentDTO updatedServer = serverService.updateServer(id, serverDTO, request);
        return new ResponseEntity<>(updatedServer, HttpStatus.OK);
    }


    @GetMapping("/region/{idRegion}")
    public ResponseEntity<List<AgentDTO>> getServersByRegion(@PathVariable Long idRegion) {
        try {
            List<AgentDTO> servers = serverService.getServersByRegion(idRegion);
            return servers.isEmpty() ? new ResponseEntity<>(HttpStatus.NO_CONTENT) : new ResponseEntity<>(servers, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @DeleteMapping("/delete/{idAgent}")
    public ResponseEntity<AgentDTO> deleteAgent(@PathVariable("idAgent") int idAgent, HttpServletRequest request) {
        try {
            AgentDTO deletedAgent = serverService.deleteAgent(idAgent, request);
            return ResponseEntity.ok(deletedAgent);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
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


}
