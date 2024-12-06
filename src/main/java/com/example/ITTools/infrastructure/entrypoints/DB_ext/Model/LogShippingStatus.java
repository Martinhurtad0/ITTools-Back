package com.example.ITTools.infrastructure.entrypoints.DB_ext.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "R_Log_Shipping_Status")
public class  LogShippingStatus{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter @Getter
    private Integer id;

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
    private Timestamp lastBackupDate;
    @Getter @Setter
    private Timestamp lastCopiedDate;
    @Getter @Setter
    private Timestamp lastRestoredDate;
    @Getter @Setter
    private String status;


}
