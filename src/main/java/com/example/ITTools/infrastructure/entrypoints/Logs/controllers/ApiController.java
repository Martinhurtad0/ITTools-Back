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
    public String getLogs(@PathVariable int agentId, @RequestParam String region) {
        return apiService.getLogs(agentId, region);
    }

    @PostMapping("/zip/single/{agentId}")
    public ResponseEntity<byte[]> zipSingleLogFile(
            @PathVariable int agentId,
            @RequestParam String filename,
            @RequestParam String region) {
        return apiService.zipSingleLogFile(agentId, filename, region);
    }

    @PostMapping("/zip/{agentId}")
    public ResponseEntity<byte[]> zipLogFile(@PathVariable int agentId, @RequestBody List<String> filenames, @RequestParam String region) {
        return apiService.zipLogFiles(agentId, filenames, region);
    }

    @GetMapping("/filter/{agentId}")
    public String filterLogsByDate(@PathVariable int agentId, @RequestParam("date") String date, @RequestParam String region) {
        return apiService.filterLogsByDate(agentId, date, region);
    }

    @GetMapping("/filter_archive/{agentId}")
    public String filterLogsArchiveByDate(@PathVariable int agentId, @RequestParam("date") String date, @RequestParam String region ) {
        return apiService.filterLogsArchiveByDate(agentId, date, region);
    }


    @GetMapping("/transaction/{agentId}")
    public String getLogsByTransactionId(@PathVariable int agentId,
                                         @RequestParam("transactionId") String transactionId,
                                         @RequestParam("date") String date, @RequestParam String region ) {
        return apiService.getLogsByTransactionId(agentId, transactionId, date, region);
    }
    @PostMapping("/search_selected/{agentId}")
    public ResponseEntity<String> searchLogsInSelectedFiles(
            @PathVariable int agentId,
            @RequestParam String region, // Añadir el parámetro de región
            @RequestBody Map<String, Object> requestBody) {

        String idTransaction = (String) requestBody.get("idTransaction");
        List<String> selectedFiles = (List<String>) requestBody.get("selectedFiles");

        if (idTransaction == null || selectedFiles == null || selectedFiles.isEmpty()) {
            return ResponseEntity.badRequest().body("idTransaction and selectedFiles are required.");
        }

        // Pasar la región al servicio
        return apiService.searchLogsInSelectedFiles(agentId, idTransaction, selectedFiles, region);
    }


}
