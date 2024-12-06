package com.example.ITTools.infrastructure.entrypoints.DB_ext.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
public class StatusDisk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter @Getter
    private Integer id;
    @Setter @Getter
    private String ip;
    @Setter @Getter
    private String serverName;
    @Setter @Getter
    private String  disk;
    @Setter @Getter
    private Long totalSpace;
    @Setter @Getter
    private Long freeSpace;
    @Setter @Getter
    private Double percentAvailable;
    @Setter @Getter
    private String region;
    @Setter @Getter
    private String status;
}
