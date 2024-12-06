package com.example.ITTools.infrastructure.entrypoints.DB_ext.Model;

import com.google.api.client.util.DateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
public class StatusBackupDatabase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter @Getter
    private Integer id;
    @Setter @Getter
    private String ip;
    @Setter @Getter
    private String databaseName;
    @Setter @Getter
    private String serverName;
    @Setter @Getter
    private String backupType;
    @Setter @Getter
    private Timestamp backupFinishDate;


    @Setter @Getter
    private String daysLastBackup;
    @Setter @Getter
    private String status;
}
