package com.example.ITTools.infrastructure.entrypoints.DB_ext.Controller;

import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.BackupInfo;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Service.TestDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/database")
public class TestDatabaseController {
    @Autowired
    private TestDatabaseService testDatabaseService;

    @GetMapping("/allBackupInfo")
    public List<BackupInfo> getAllBackupInfo() {
        return testDatabaseService.getAllBackupInfo();
    }
}
