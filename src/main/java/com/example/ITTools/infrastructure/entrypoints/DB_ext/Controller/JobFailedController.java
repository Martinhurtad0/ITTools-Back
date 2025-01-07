package com.example.ITTools.infrastructure.entrypoints.DB_ext.Controller;

import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.ErrorLog;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.JobFailed;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Service.JobFailedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jobsFailed")
public class JobFailedController {


    @Autowired
    private JobFailedService jobFailedService;

    @GetMapping("/status")
    @ResponseStatus(HttpStatus.OK)
    public List<JobFailed> getJobFailed() {
        return jobFailedService.JobFailedAll();
    }

    @GetMapping("/{sp}")
    public ResponseEntity<List<ErrorLog>> getErrorsBySp(@PathVariable String sp) {
        List<ErrorLog> errors =  jobFailedService.findErrorsBySp(sp);
        return ResponseEntity.ok(errors);
    }

}
