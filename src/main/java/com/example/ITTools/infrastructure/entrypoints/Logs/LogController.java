package com.example.ITTools.infrastructure.entrypoints.Logs;


import com.example.ITTools.infrastructure.entrypoints.Server.Models.AgentModel;
import com.example.ITTools.infrastructure.entrypoints.Server.Repositories.AgentRepository;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


@RestController
@RequestMapping("/logs")

public class LogController {

    private final LogService logService;
    private final AgentRepository agentRepository;
    private final RestTemplate restTemplate;

    public LogController(LogService logService, AgentRepository agentRepository, RestTemplate
                          restTemplate) {
        this.logService = logService;
        this.agentRepository =agentRepository;
        this.restTemplate = restTemplate;
    }

    @CrossOrigin(origins = "http://192.168.2.133:5173", allowCredentials = "true")
    @GetMapping("/{agentId}")
    public List<String> getLogs(@PathVariable int agentId, @RequestHeader("Authorization") String authorizationHeader) {
        // Extraer el token del encabezado "Authorization"
        String token = authorizationHeader.replace("Bearer ", "");

        // Llamar al servicio para obtener los logs usando el token
        return logService.fetchLogsFromAgent(agentId, token);
    }

    // Nuevo endpoint para descargar los logs en formato ZIP
    @PostMapping("/download")
    public ResponseEntity<byte[]> downloadLogs(
            @RequestParam int agentId,
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, List<String>> requestBody) {

        List<String> selectedLogs = requestBody.get("filenames");
        if (selectedLogs == null || selectedLogs.isEmpty()) {
            return ResponseEntity.badRequest().body("No log files selected".getBytes());
        }

        try {
            byte[] zipData = downloadLogsAsZip(agentId, token, selectedLogs);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("logs.zip")
                    .build());
            return new ResponseEntity<>(zipData, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage().getBytes());
        }
    }

    private byte[] downloadLogsAsZip(int agentId, String token, List<String> selectedLogs) {
        AgentModel agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found with id " + agentId));

        String apiUrl = UriComponentsBuilder.fromHttpUrl(agent.getWebServiceUrl() + agent.getPathArchive())
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Collections.singletonMap("filenames", selectedLogs);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, byte[].class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to download logs from agent");
        }
    }


    // Endpoint para cortar un log
    @GetMapping("/{agentId}/cut_log")
    public ResponseEntity<byte[]> cutLog(@PathVariable int agentId, @RequestHeader("Authorization") String token, @RequestParam String logName) {
        try {
            byte[] cutLog = logService.cutLog(agentId, token.replace("Bearer ", ""), logName);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + logName);
            return ResponseEntity.ok().headers(headers).body(cutLog);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    // Endpoint para transmitir el contenido de un log en tiempo real
    @GetMapping("/{agentId}/stream")
    public ResponseEntity<String> streamLog(@PathVariable int agentId, @RequestHeader("Authorization") String token, @RequestParam String logName) {
        try {
            return logService.streamLog(agentId, token.replace("Bearer ", ""), logName);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Endpoint para ver el contenido de un log específico
    @GetMapping("/{agentId}/view")
    public ResponseEntity<String> viewLog(@PathVariable int agentId, @RequestHeader("Authorization") String token, @RequestParam String logFileName) {
        try {
            String logContent = logService.fetchLogContent(agentId, token.replace("Bearer ", ""), logFileName);
            return ResponseEntity.ok(logContent);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}

