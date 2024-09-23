package com.example.ITTools.infrastructure.entrypoints.Logs;

import com.example.ITTools.infrastructure.entrypoints.Server.Services.AgentService;
import com.example.ITTools.infrastructure.entrypoints.Server.DTO.AgentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AgentService agentService;

    // Método para obtener la URL del WebService de un agente
    private String getWebServiceUrl(int agentId) {
        AgentDTO agent = agentService.getServerById(agentId);
        if (agent == null || agent.getWebServiceUrl() == null || agent.getWebServiceUrl().isEmpty()) {
            throw new RuntimeException("Agent or WebService URL not found for agent ID: " + agentId);
        }
        return agent.getWebServiceUrl();
    }

    public String getLogs(int agentId) {
        try {
            String webServiceUrl = getWebServiceUrl(agentId);
            return restTemplate.getForObject(webServiceUrl + "/logs", String.class);
        } catch (HttpClientErrorException e) {
            return "Error: " + e.getStatusCode() + " " + e.getResponseBodyAsString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String getLogFile(int agentId, String filename) {
        try {
            String webServiceUrl = getWebServiceUrl(agentId);
            return restTemplate.getForObject(webServiceUrl + "/logs/" + filename, String.class);
        } catch (HttpClientErrorException e) {
            return "Error: " + e.getStatusCode() + " " + e.getResponseBodyAsString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public ResponseEntity<byte[]> zipLogFile(int agentId, String filename) {
        try {
            String webServiceUrl = getWebServiceUrl(agentId);
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    webServiceUrl + "/logs/zip/" + filename,
                    HttpMethod.GET,
                    null,
                    byte[].class
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename + ".zip");

            return new ResponseEntity<>(response.getBody(), headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(("Error: " + e.getMessage()).getBytes(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String filterLogsByDate(int agentId, String date) {
        try {
            String webServiceUrl = getWebServiceUrl(agentId);
            return restTemplate.getForObject(webServiceUrl + "/logs/filter?date=" + date, String.class);
        } catch (HttpClientErrorException e) {
            return "Error: " + e.getStatusCode() + " " + e.getResponseBodyAsString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
