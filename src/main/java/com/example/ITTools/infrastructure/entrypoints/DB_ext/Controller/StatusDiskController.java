package com.example.ITTools.infrastructure.entrypoints.DB_ext.Controller;

import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.ErrorLog;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.JobFailed;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.StatusDisk;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Repository.ErrorLogRepository;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Service.StatusDiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/statusDisk")
public class StatusDiskController {

    @Autowired
    private StatusDiskService statusDiskService;

    @GetMapping("/status")
    @ResponseStatus(HttpStatus.OK)
    public List<StatusDisk> getStatusDisk() {
        return statusDiskService.statusDiskAll();
    }


    @GetMapping("/{sp}")
    public ResponseEntity<List<ErrorLog>> getErrorsBySp(@PathVariable String sp) {
        List<ErrorLog> errors = statusDiskService.findErrorsBySp(sp);
        return ResponseEntity.ok(errors);
    }
}
