package com.example.ITTools.infrastructure.entrypoints.DB_ext.Model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public class BackupInfo {
    @Getter @Setter
    private String region;
    @Getter @Setter
    private String ip;
    @Getter @Setter
    private String primaryServer;
    @Getter @Setter
    private String secondaryServer;
    @Getter @Setter
    private String primaryDatabase;
    @Getter @Setter
    private LocalDateTime lastBackupDate;
    @Getter @Setter
    private LocalDateTime lastCopiedDate;
    @Getter @Setter
    private LocalDateTime lastRestoredDate;
    @Getter @Setter
    private String status;

    // Getters y setters
}
