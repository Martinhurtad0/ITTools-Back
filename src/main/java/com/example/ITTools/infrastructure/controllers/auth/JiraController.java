package com.example.ITTools.infrastructure.controllers.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/jira")
public class JiraController {

    @PostMapping("/token")
    public ResponseEntity<String> authenticateWithJira(@RequestBody Map<String, String> requestBody) {
        try {
            String code = requestBody.get("code");

            String clientId = "WqIezgIRXTVMWie5sQpl0PZh1WcLxR9R";
            String redirectUri = "http://localhost:5173/uikit/Support";

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("code", code);
            body.add("redirect_uri", redirectUri);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            String jiraUrl = "https://auth.atlassian.com/oauth/token";
            ResponseEntity<String> response = restTemplate.exchange(jiraUrl, HttpMethod.POST, request, String.class);

            // Deserializar respuesta usando Jackson
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> jsonResponse = objectMapper.readValue(response.getBody(), Map.class);

            // Extraer y mostrar el access_token
            if (jsonResponse.containsKey("access_token")) {
                String accessToken = (String) jsonResponse.get("access_token");
                System.out.println("Access Token: " + accessToken);
            } else {
                System.out.println("Access Token not found in response.");
            }

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error fetching Jira access token: " + e.getMessage());
        }
    }

    @PostMapping("/createIssue")
    public ResponseEntity<String> createIssue(
            @RequestBody Map<String, Object> requestBody) {
        try {
            // Obtener el token de autenticación desde el cuerpo de la solicitud
            String authorizationToken = (String) requestBody.get("authorizationToken");

            // Verificar que el token esté en el formato correcto (Bearer <token>)
            if (authorizationToken == null || !authorizationToken.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Authorization token must be provided and start with 'Bearer '");
            }

            // Obtener los parámetros del issue desde el cuerpo de la solicitud
            String projectKey = (String) requestBody.get("projectKey");
            String summary = (String) requestBody.get("summary");
            String descriptionText = (String) requestBody.get("description");
            String issueType = (String) requestBody.get("issueType"); // Tipo de issue proporcionado en la solicitud

            // Verificar que issueType no sea nulo o vacío
            if (issueType == null || issueType.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Issue type must be provided.");
            }

            // Cuerpo de la solicitud para Jira
            Map<String, Object> fields = new HashMap<>();
            fields.put("project", Map.of("key", projectKey));
            fields.put("summary", summary);
            fields.put("description", Map.of(
                    "type", "doc",
                    "version", 1,
                    "content", List.of(
                            Map.of(
                                    "type", "paragraph",
                                    "content", List.of(
                                            Map.of(
                                                    "text", descriptionText,
                                                    "type", "text"
                                            )
                                    )
                            )
                    )
            ));
            fields.put("issuetype", Map.of("name", issueType)); // Usar el tipo de issue proporcionado
            fields.put("customfield_10301", Map.of("value", "ALL"));


            Map<String, Object> jiraRequestBody = Map.of("fields", fields);

            // Crear solicitud HTTP
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", authorizationToken); // Usar el Bearer token proporcionado en el cuerpo

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(jiraRequestBody, headers);

            // Endpoint de Jira
            String jiraUrl = "https://api.atlassian.com/ex/jira/c5ade6fb-da0c-4bf5-b93c-157f03787da4/rest/api/3/issue";

            // Enviar la solicitud y obtener la respuesta
            ResponseEntity<String> response = restTemplate.exchange(jiraUrl, HttpMethod.POST, request, String.class);

            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error creating Jira issue: " + e.getMessage());
        }
    }



}
