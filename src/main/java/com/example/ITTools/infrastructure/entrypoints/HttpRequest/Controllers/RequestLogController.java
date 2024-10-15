package com.example.ITTools.infrastructure.entrypoints.HttpRequest.Controllers;

import com.example.ITTools.infrastructure.entrypoints.HttpRequest.Repositories.RequestLogRepository;
import com.example.ITTools.infrastructure.entrypoints.HttpRequest.RequestLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/request")
public class RequestLogController {

    @Autowired
    private RequestLogRepository requestLogRepository;

    @GetMapping
    public ResponseEntity<List<RequestLog>> getAllLogs() {
        List<RequestLog> logs = requestLogRepository.findAll();
        return ResponseEntity.ok(logs);
    }

}
