package com.example.ITTools.infrastructure.entrypoints.DB_ext.Model;

import com.google.api.client.util.DateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name="R_AlwaysOn_Status")
public class StatusAlways {

    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    @Getter
    private Integer id;
    @Setter
    @Getter
    private String availabilityGroupName;
    @Setter
    @Getter
    private String availabilityReplicaServerName;
    @Setter
    @Getter
    private String availabilityDatabaseName;
    @Setter
    @Getter
    private String replicaRole;
    @Setter
    @Getter

    private String availabilityMode;
    @Setter
    @Getter
    private String synchronizationState;
    @Setter
    @Getter
    private Timestamp lastRedoneTime;
    @Setter
    @Getter
    private Timestamp lastSentTime;
    @Setter
    @Getter
    private Timestamp lastCommitTime;





}


