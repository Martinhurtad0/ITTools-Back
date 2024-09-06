package com.example.ITTools.infrastructure.entrypoints.Logs;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.ITTools.infrastructure.entrypoints.Server.Models.AgentModel;
import com.example.ITTools.infrastructure.entrypoints.Server.Repositories.AgentRepository;

import org.springframework.web.client.ResourceAccessException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Optional;

@Service
public class LogService {

    @Autowired
    private AgentRepository agentRepository;

    private final RestTemplate restTemplate;

    public LogService() {
        this.restTemplate = new RestTemplate();
    }

    public List<String> fetchLogsFromAgent(int agentId, String token) {
        Optional<AgentModel> agentOptional = agentRepository.findById(agentId);
        if (agentOptional.isEmpty()) {
            throw new RuntimeException("Agent not found with id " + agentId);
        }

        AgentModel agent = agentOptional.get();
        String apiUrl = agent.getWebServiceUrl(); // Asegúrate de que la URL sea correcta

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<LogResponse> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, LogResponse.class);
            int statusCode = response.getStatusCodeValue(); // Captura el código de estado

            // Mensaje de éxito al recibir el token
            if (statusCode == 200) {
                System.out.println("Token recibido correctamente.");
                LogResponse logResponse = response.getBody();
                if (logResponse != null && logResponse.getLogs() != null) {
                    return logResponse.getLogs();
                } else {
                    throw new RuntimeException("No logs found or response body is null for agent: " + agent.getAgentName());
                }
            } else {
                // Maneja otros códigos de estado
                throw new RuntimeException("Received HTTP status code " + statusCode + " while fetching logs from agent: " + agent.getAgentName());
            }
        } catch (HttpClientErrorException e) {
            // Captura errores HTTP específicos
            System.out.println("Error al recibir el token: " + e.getResponseBodyAsString());
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
}