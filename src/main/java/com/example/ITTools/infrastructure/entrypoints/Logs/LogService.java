package com.example.ITTools.infrastructure.entrypoints.Logs;

import com.example.ITTools.infrastructure.entrypoints.Server.Models.AgentModel;
import com.example.ITTools.infrastructure.entrypoints.Server.Repositories.AgentRepository;


import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.util.UriComponentsBuilder;


import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.*;


@Service
public class LogService {

    @Autowired
    private AgentRepository agentRepository;

    private final RestTemplate restTemplate;

    public LogService() {
        this.restTemplate = new RestTemplate();
    }

    // Método para obtener los logs del agente
    public List<String> fetchLogsFromAgent(int agentId, String token) {
        Optional<AgentModel> agentOptional = agentRepository.findById(agentId);
        if (agentOptional.isEmpty()) {
            throw new RuntimeException("Agent not found with id " + agentId);
        }

        AgentModel agent = agentOptional.get();
        String apiUrl = agent.getWebServiceUrl(); // URL del servicio web del agente
        String logPath = agent.getPathArchive();  // Ruta donde están los logs

        if (logPath == null || logPath.isEmpty()) {
            throw new RuntimeException("Log path not configured for agent: " + agent.getAgentName());
        }

        // Resto de la lógica para obtener los logs
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<LogResponse> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, LogResponse.class);
            int statusCode = response.getStatusCodeValue();

            if (statusCode == 200) {
                LogResponse logResponse = response.getBody();
                if (logResponse != null && logResponse.getLogs() != null) {
                    return logResponse.getLogs();
                } else {
                    throw new RuntimeException("No logs found or response body is null for agent: " + agent.getAgentName());
                }
            } else {
                throw new RuntimeException("Received HTTP status code " + statusCode + " while fetching logs from agent: " + agent.getAgentName());
            }
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("HTTP error while fetching logs from agent: " + agent.getAgentName() + " - " + e.getStatusCode() + ": " + e.getResponseBodyAsString(), e);
        } catch (ResourceAccessException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SocketTimeoutException) {
                throw new RuntimeException("Connection timed out while fetching logs from agent: " + agent.getAgentName(), e);
            } else if (cause instanceof ConnectException) {
                throw new RuntimeException("Failed to connect to the server while fetching logs from agent: " + agent.getAgentName(), e);
            } else {
                throw new RuntimeException("Failed to fetch logs from agent: " + agent.getAgentName(), e);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch logs from agent: " + agent.getAgentName(), e);
        }
    }

    public byte[] downloadLogsAsZip(int agentId, String token, List<String> filenames) {
        Optional<AgentModel> agentOptional = agentRepository.findById(agentId);
        if (agentOptional.isEmpty()) {
            throw new RuntimeException("Agent not found with id " + agentId);
        }

        AgentModel agent = agentOptional.get();
        String apiUrl = UriComponentsBuilder.fromHttpUrl(agent.getWebServiceUrl() + "/download_logs")
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Collections.singletonMap("filenames", filenames);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, byte[].class);
            int statusCode = response.getStatusCodeValue();

            if (statusCode == 200) {
                byte[] zipData = response.getBody();
                if (zipData != null) {
                    // Guardar el archivo ZIP en el disco local
                    String filePath = "C:/selected_logs.zip"; // Cambia la ruta según sea necesario
                    try (FileOutputStream fos = new FileOutputStream(filePath)) {
                        fos.write(zipData);
                        fos.flush();
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to save the ZIP file to disk: " + e.getMessage(), e);
                    }
                    return zipData; // Retorna los datos también si es necesario
                } else {
                    throw new RuntimeException("No data received from agent: " + agent.getAgentName());
                }
            } else {
                throw new RuntimeException("Received HTTP status code " + statusCode + " while downloading logs from agent: " + agent.getAgentName());
            }
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("HTTP error while downloading logs from agent: " + agent.getAgentName() + " - " + e.getStatusCode() + ": " + e.getResponseBodyAsString(), e);
        } catch (ResourceAccessException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SocketTimeoutException) {
                throw new RuntimeException("Connection timed out while downloading logs from agent: " + agent.getAgentName(), e);
            } else if (cause instanceof ConnectException) {
                throw new RuntimeException("Failed to connect to the server while downloading logs from agent: " + agent.getAgentName(), e);
            } else {
                throw new RuntimeException("Failed to download logs from agent: " + agent.getAgentName(), e);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to download logs from agent: " + agent.getAgentName(), e);
        }
    }
    // Método para cortar un log y descargarlo
    public byte[] cutLog(int agentId, String token, String logName) {
        Optional<AgentModel> agentOptional = agentRepository.findById(agentId);
        if (agentOptional.isEmpty()) {
            throw new RuntimeException("Agent not found with id " + agentId);
        }

        AgentModel agent = agentOptional.get();
        String apiUrl = agent.getWebServiceUrl() + "/cut_log?logName=" + logName; // URL del servicio web del agente

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, byte[].class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("HTTP error while cutting log from agent: " + agent.getAgentName() + " - " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            handleResourceAccessException(e, agent);
        }
        return null; // This line will never be reached
    }

    private void handleResourceAccessException(ResourceAccessException e, AgentModel agent) {
        Throwable cause = e.getCause();
        if (cause instanceof SocketTimeoutException) {
            throw new RuntimeException("Connection timed out while accessing agent: " + agent.getAgentName(), e);
        } else if (cause instanceof ConnectException) {
            throw new RuntimeException("Failed to connect to the server while accessing agent: " + agent.getAgentName(), e);
        } else {
            throw new RuntimeException("Failed to access logs from agent: " + agent.getAgentName(), e);
        }
    }

    // Método para transmitir el contenido de un log en tiempo real
    public ResponseEntity<String> streamLog(int agentId, String token, String logName) {
        Optional<AgentModel> agentOptional = agentRepository.findById(agentId);
        if (agentOptional.isEmpty()) {
            throw new RuntimeException("Agent not found with id " + agentId);
        }

        AgentModel agent = agentOptional.get();
        String apiUrl = agent.getWebServiceUrl() + "/stream_log?logName=" + logName; // URL del servicio web del agente

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("HTTP error while streaming log from agent: " + agent.getAgentName() + " - " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            handleResourceAccessException(e, agent);
        }
        return null; // This line will never be reached
    }
    //Metdo para ver un log en especifico
    public String fetchLogContent(int agentId, String token, String logFileName) {
        Optional<AgentModel> agentOptional = agentRepository.findById(agentId);
        if (agentOptional.isEmpty()) {
            throw new RuntimeException("Agent not found with id " + agentId);
        }

        AgentModel agent = agentOptional.get();
        String apiUrl = UriComponentsBuilder.fromHttpUrl(agent.getWebServiceUrl() + "/view_log")
                .queryParam("file", logFileName)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to fetch log content from agent: " + agent.getAgentName());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while fetching log content from agent: " + agent.getAgentName(), e);
        }
    }

}
