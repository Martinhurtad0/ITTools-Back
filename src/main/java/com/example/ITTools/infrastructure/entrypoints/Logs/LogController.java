package com.example.ITTools.infrastructure.entrypoints.Logs;



import com.example.ITTools.infrastructure.entrypoints.Server.Repositories.AgentRepository;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;


import java.util.List;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;



@RestController
@RequestMapping("/logs")

public class LogController {

    private final LogService logService;


    public LogController(LogService logService, AgentRepository agentRepository, RestTemplate
                          restTemplate) {
        this.logService = logService;

    }

    @CrossOrigin(origins = "http://192.168.2.133:5173", allowCredentials = "true")
    @GetMapping("/{agentId}")
    public List<String> getLogs(@PathVariable int agentId, @RequestHeader("Authorization") String authorizationHeader) {
        // Extraer el token del encabezado "Authorization"
        String token = authorizationHeader.replace("Bearer ", "");

        // Llamar al servicio para obtener los logs usando el token
        return logService.fetchLogsFromAgent(agentId);
    }

    // Nuevo endpoint para descargar los logs en formato ZIP
    // Endpoint para descargar logs como ZIP
    @PostMapping("/{agentId}/download")
    public ResponseEntity<byte[]> downloadLogsAsZip(@PathVariable int agentId, @RequestBody List<String> filenames) {
        byte[] zipData = logService.downloadLogsAsZip(agentId, filenames);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=selected_logs.zip")
                .body(zipData);
    }

    // Endpoint para cortar un log y descargarlo
    @GetMapping("/{agentId}/cut")
    public ResponseEntity<byte[]> cutLog(@PathVariable int agentId, @RequestParam String logName) {
        byte[] logData = logService.cutLog(agentId, logName);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + logName + "_cut.log")
                .body(logData);
    }

    // Endpoint para transmitir el contenido de un log en tiempo real
    @GetMapping("/{agentId}/stream")
    public ResponseEntity<String> streamLog(@PathVariable int agentId, @RequestParam String logName) {
        return logService.streamLog(agentId, logName);
    }

    // Endpoint para ver el contenido de un log específico
    @GetMapping("/{agentId}/view")
    public ResponseEntity<String> viewLog(@PathVariable int agentId, @RequestParam String fileName) {
        String content = logService.fetchLogContent(agentId, fileName);
        return ResponseEntity.ok(content);
    }
}

