package com.example.ITTools.infrastructure.entrypoints.Logs;

import com.example.ITTools.infrastructure.entrypoints.Server.Services.AgentService;
import com.example.ITTools.infrastructure.entrypoints.Server.DTO.AgentDTO;
import com.example.ITTools.infrastructure.entrypoints.Audit.Service.AuditService;
import jakarta.servlet.http.HttpServletRequest; // Import HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ParseException;
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
    private AuditService auditService; // Injecting AuditService

    @Autowired
    private HttpServletRequest request; // To get the HTTP request context

    // Método para obtener la URL del WebService de un agente
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
            String logs = restTemplate.getForObject(webServiceUrl + "/logs", String.class);
            auditService.audit(action + " - Success", request); // Audit success
            return logs;
        } catch (HttpClientErrorException e) {
            auditService.audit(action + " - Error: " + e.getStatusCode(), request); // Audit error
            return "Error: " + e.getStatusCode() + " " + e.getResponseBodyAsString();
        } catch (Exception e) {
            auditService.audit(action + " - Error: " + e.getMessage(), request); // Audit error
            return "Error: " + e.getMessage();
        }
    }

    public String getLogFile(int agentId, String filename) {
        String action = "Get Log File for Agent ID: " + agentId + ", Filename: " + filename;
        try {
            String webServiceUrl = getWebServiceUrl(agentId);
            String logFile = restTemplate.getForObject(webServiceUrl + "/logs/" + filename, String.class);
            auditService.audit(action + " - Success", request); // Audit success
            return logFile;
        } catch (HttpClientErrorException e) {
            auditService.audit(action + " - Error: " + e.getStatusCode(), request); // Audit error
            return "Error: " + e.getStatusCode() + " " + e.getResponseBodyAsString();
        } catch (Exception e) {
            auditService.audit(action + " - Error: " + e.getMessage(), request); // Audit error
            return "Error: " + e.getMessage();
        }
    }

    public ResponseEntity<byte[]> zipLogFiles(int agentId, List<String> filenames) {
        String action = "Zip Log Files for Agent ID: " + agentId;
        try {
            String webServiceUrl = getWebServiceUrl(agentId);

            // Crear el cuerpo de la solicitud con la lista de archivos
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("files", filenames);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Realizar la llamada POST para zipear los archivos
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    webServiceUrl + "/logs/zip",
                    HttpMethod.POST,
                    entity,
                    byte[].class
            );

            // Preparar los encabezados para la respuesta
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            responseHeaders.setContentDispositionFormData("attachment", "logs.zip");

            auditService.audit(action + " - Success", request); // Auditoría exitosa
            return new ResponseEntity<>(response.getBody(), responseHeaders, HttpStatus.OK);
        } catch (Exception e) {
            auditService.audit(action + " - Error: " + e.getMessage(), request); // Auditoría de error
            return new ResponseEntity<>(("Error: " + e.getMessage()).getBytes(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String filterLogsByDate(int agentId, String date) {
        String action = "Filter Logs by Date for Agent ID: " + agentId + ", Date: " + date;
        try {
            String webServiceUrl = getWebServiceUrl(agentId);
            String filteredLogs = restTemplate.getForObject(webServiceUrl + "/logs/filter?date=" + date, String.class);
            auditService.audit(action + " - Success", request); // Audit success
            return filteredLogs;
        } catch (HttpClientErrorException e) {
            auditService.audit(action + " - Error: " + e.getStatusCode(), request); // Audit error
            return "Error: " + e.getStatusCode() + " " + e.getResponseBodyAsString();
        } catch (Exception e) {
            auditService.audit(action + " - Error: " + e.getMessage(), request); // Audit error
            return "Error: " + e.getMessage();
        }
    }

    public String getLogsByTransactionId(int agentId, String transactionId, String date) {
        String action = "Get Logs by Transaction ID for Agent ID: " + agentId + ", Transaction ID: " + transactionId;
        try {
            // Validar si el transactionId tiene más de 4 dígitos
            if (transactionId.length() < 4) {
                throw new IllegalArgumentException("Transaction ID must be greater than 4 digits.");
            }

            String webServiceUrl = getWebServiceUrl(agentId);
            String fullUrl = webServiceUrl + "/logs/search?idTransaction=" + transactionId + "&date=" + date;

            String logs = restTemplate.getForObject(fullUrl, String.class);
            auditService.audit(action + " - Success", request); // Audit success
            return logs;
        } catch (ParseException e) {
            auditService.audit(action + " - Error: Invalid date format", request); // Audit error
            return "Error: Invalid date format. Use DD-MM-YYYY.";
        } catch (HttpClientErrorException e) {
            auditService.audit(action + " - Error: " + e.getStatusCode(), request); // Audit error
            return "Error: " + e.getStatusCode() + " " + e.getResponseBodyAsString();
        } catch (Exception e) {
            auditService.audit(action + " - Error: " + e.getMessage(), request); // Audit error
            return "Error: " + e.getMessage();
        }
    }

    public ResponseEntity<String> searchLogsInSelectedFiles(int agentId, String idTransaction, List<String> selectedFiles) {
        String action = "Search Logs in Selected Files for Agent ID: " + agentId + ", Transaction ID: " + idTransaction;
        try {
            // Validar si el transactionId tiene más de 4 dígitos
            if (idTransaction.length() < 4) {
                throw new IllegalArgumentException("Transaction ID must be greater than 4 digits.");
            }

            String webServiceUrl = getWebServiceUrl(agentId);
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("idTransaction", idTransaction);
            requestBody.put("selectedFiles", selectedFiles);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    webServiceUrl + "/logs/search_selected",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            auditService.audit(action + " - Success", request); // Audit success
            return response;
        } catch (HttpClientErrorException e) {
            auditService.audit(action + " - Error: " + e.getStatusCode(), request); // Audit error
            return new ResponseEntity<>("Error: " + e.getStatusCode() + " " + e.getResponseBodyAsString(), e.getStatusCode());
        } catch (Exception e) {
            auditService.audit(action + " - Error: " + e.getMessage(), request); // Audit error
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
