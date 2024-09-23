package com.example.ITTools.infrastructure.entrypoints.Logs.controllers;

import com.example.ITTools.infrastructure.entrypoints.Logs.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/logs")
public class ApiController {

    @Autowired
    private ApiService apiService;

    @GetMapping("/{agentId}")
    public String getLogs(@PathVariable int agentId) {
        return apiService.getLogs(agentId);
    }

    @GetMapping("/{agentId}/{filename}")
    public String getLogFile(@PathVariable int agentId, @PathVariable String filename) {
        return apiService.getLogFile(agentId, filename);
    }

    @GetMapping("/zip/{agentId}/{filename}")
    public ResponseEntity<byte[]> zipLogFile(@PathVariable int agentId, @PathVariable String filename) {
        return apiService.zipLogFile(agentId, filename);
    }

    @GetMapping("/filter/{agentId}")
    public String filterLogsByDate(@PathVariable int agentId, @RequestParam("date") String date) {
        return apiService.filterLogsByDate(agentId, date);
    }

}
