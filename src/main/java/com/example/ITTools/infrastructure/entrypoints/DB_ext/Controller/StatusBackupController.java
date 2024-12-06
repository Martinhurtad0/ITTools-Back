package com.example.ITTools.infrastructure.entrypoints.DB_ext.Controller;


import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.StatusBackupDatabase;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Service.StatusBackupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/statusBackup")
public class StatusBackupController {

    @Autowired
    private StatusBackupService statusBackupService;

    @GetMapping("/status")
    @ResponseStatus(HttpStatus.OK)
    public List<StatusBackupDatabase> getStatusBackup() {
        return  statusBackupService.statusBackupAll();
    }


}
