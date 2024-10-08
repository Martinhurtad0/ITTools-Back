package com.example.ITTools.infrastructure.entrypoints.Logs;

import com.example.ITTools.infrastructure.entrypoints.Server.Services.AgentService;
import com.example.ITTools.infrastructure.entrypoints.Server.DTO.AgentDTO;
import com.example.ITTools.infrastructure.entrypoints.Audit.Service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApiService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AgentService agentService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private HttpServletRequest request;

    // Método para obtener el token JWT del servidor Flask
    private String getJwtToken(String webServiceUrl) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(webServiceUrl + "/get_token", Map.class);
            Map<String, String> responseBody = response.getBody();
            return responseBody != null ? responseBody.get("access_token") : null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get JWT token: " + e.getMessage());
        }
    }

    // Método auxiliar para obtener la URL del agente
    private String getWebServiceUrl(int agentId) {
        AgentDTO agent = agentService.getServerById(agentId);
        if (agent == null || agent.getWebServiceUrl() == null || agent.getWebServiceUrl().isEmpty()) {
            throw new RuntimeException("Agent or WebService URL not found for agent ID: " + agentId);
        }
        return agent.getWebServiceUrl();
    }

    public String getLogs(int agentId) {
        String action = "Get Logs for Agent ID: " + agentId;
        try {
            String webServiceUrl = getWebServiceUrl(agentId);
            String token = getJwtToken(webServiceUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);  // Añadir el token JWT a los encabezados

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    webServiceUrl + "/logs",
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            auditService.audit(action, request);  // Audit success
            return response.getBody();
        } catch (HttpClientErrorException e) {
            return "Error: " + e.getStatusCode() + " " + e.getResponseBodyAsString();
        }
    }

    public ResponseEntity<byte[]> zipLogFiles(int agentId, List<String> filenames) {
        String action = "Zip Log Files for Agent ID: " + agentId;
        try {
            String webServiceUrl = getWebServiceUrl(agentId);
            String token = getJwtToken(webServiceUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);  // Añadir el token JWT a los encabezados

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("files", filenames);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    webServiceUrl + "/logs/zip",
                    HttpMethod.POST,
                    entity,
                    byte[].class
            );

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            responseHeaders.setContentDispositionFormData("attachment", "logs.zip");

            auditService.audit(action , request);
            return new ResponseEntity<>(response.getBody(), responseHeaders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(("Error: " + e.getMessage()).getBytes(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public String filterLogsArchiveByDate(int agentId, String date) {
        String action = "Filter Logs by Date for Agent ID: " + agentId + ", Date: " + date;
        try {
            String webServiceUrl = getWebServiceUrl(agentId);
            String token = getJwtToken(webServiceUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);  // Añadir el token JWT a los encabezados

            HttpEntity<String> entity = new HttpEntity<>(headers);

            AgentDTO agent = agentService.getServerById(agentId);
            String logPath = agent.getPathArchive();

            if (logPath == null || logPath.isEmpty()) {
                throw new RuntimeException("Log path not found for agent ID: " + agentId);
            }

            ResponseEntity<String> response = restTemplate.exchange(
                    webServiceUrl + "/logs/filter?date=" + date + "&logPath=" + logPath,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            auditService.audit(action , request);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            return "Error: " + e.getStatusCode() + " " + e.getResponseBodyAsString();
        }
    }

    public String filterLogsByDate(int agentId, String date) {
        String action = "Filter Logs by Date for Agent ID: " + agentId + ", Date: " + date;
        try {
            String webServiceUrl = getWebServiceUrl(agentId);
            String token = getJwtToken(webServiceUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);  // Añadir el token JWT a los encabezados

            HttpEntity<String> entity = new HttpEntity<>(headers);

            AgentDTO agent = agentService.getServerById(agentId);
            String logPath = agent.getPathLog();

            if (logPath == null || logPath.isEmpty()) {
                throw new RuntimeException("Log path not found for agent ID: " + agentId);
            }

            ResponseEntity<String> response = restTemplate.exchange(
                    webServiceUrl + "/logs/filter?date=" + date + "&logPath=" + logPath,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            auditService.audit(action , request);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            return "Error: " + e.getStatusCode() + " " + e.getResponseBodyAsString();
        }
    }





    public String getLogsByTransactionId(int agentId, String transactionId, String date) {
        String action = "Get Logs by Transaction ID for Agent ID: " + agentId + ", Transaction ID: " + transactionId;
        try {
            String webServiceUrl = getWebServiceUrl(agentId);
            String token = getJwtToken(webServiceUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);  // Añadir el token JWT a los encabezados

            HttpEntity<String> entity = new HttpEntity<>(headers);

            AgentDTO agent = agentService.getServerById(agentId);
            String logPath = agent.getPathLog();

            if (transactionId.length() < 4) {
                throw new IllegalArgumentException("Transaction ID must be greater than 4 digits.");
            }

            String fullUrl = webServiceUrl + "/logs/search?idTransaction=" + transactionId + "&date=" + date + "&logPath=" + logPath;

            ResponseEntity<String> response = restTemplate.exchange(
                    fullUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            auditService.audit(action, request);
            return response.getBody();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public ResponseEntity<String> searchLogsInSelectedFiles(int agentId, String idTransaction, List<String> selectedFiles) {
        String action = "Search Logs in Selected Files for Agent ID: " + agentId + ", Transaction ID: " + idTransaction;

        try {
            String webServiceUrl = getWebServiceUrl(agentId);
            String token = getJwtToken(webServiceUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);  // Añadir el token JWT a los encabezados

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("idTransaction", idTransaction);
            requestBody.put("selectedFiles", selectedFiles);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    webServiceUrl + "/logs/search_selected",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            auditService.audit(action , request);
            return ResponseEntity.ok(response.getBody());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body("Error: " + e.getResponseBodyAsString());
        }
    }
}
