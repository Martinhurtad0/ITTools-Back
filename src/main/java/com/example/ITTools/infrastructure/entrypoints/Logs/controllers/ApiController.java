package com.example.ITTools.infrastructure.entrypoints.Logs.controllers;

import com.example.ITTools.infrastructure.entrypoints.Logs.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/logs")
public class ApiController {

    @Autowired
    private ApiService apiService;

    @GetMapping("/{agentId}")
    public String getLogs(@PathVariable int agentId) {
        return apiService.getLogs(agentId);
    }

    @PostMapping("/zip/{agentId}")
    public ResponseEntity<byte[]> zipLogFile(@PathVariable int agentId, @RequestBody List<String> filenames) {
        return apiService.zipLogFiles(agentId, filenames);
    }

    @GetMapping("/filter/{agentId}")
    public String filterLogsByDate(@PathVariable int agentId, @RequestParam("date") String date) {
        return apiService.filterLogsByDate(agentId, date);
    }

    @GetMapping("/transaction/{agentId}")
    public String getLogsByTransactionId(@PathVariable int agentId,
                                         @RequestParam("transactionId") String transactionId,
                                         @RequestParam("date") String date) {
        return apiService.getLogsByTransactionId(agentId, transactionId, date);
    }
    @PostMapping("/search_selected/{agentId}")
    public ResponseEntity<String> searchLogsInSelectedFiles(
            @PathVariable int agentId,
            @RequestBody Map<String, Object> requestBody) {

        String idTransaction = (String) requestBody.get("idTransaction");
        List<String> selectedFiles = (List<String>) requestBody.get("selectedFiles");

        if (idTransaction == null || selectedFiles == null || selectedFiles.isEmpty()) {
            return ResponseEntity.badRequest().body("idTransaction and selectedFiles are required.");
        }

        return apiService.searchLogsInSelectedFiles(agentId, idTransaction, selectedFiles);
    }



}
