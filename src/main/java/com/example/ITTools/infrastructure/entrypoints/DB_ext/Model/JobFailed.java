package com.example.ITTools.infrastructure.entrypoints.DB_ext.Model;

import com.google.api.client.util.DateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name="JobFailed")
public class JobFailed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    private Integer id;
    @Getter @Setter
    private String ip;
    @Getter @Setter
    private String jobName;
    @Getter @Setter
    private String serverName;
    @Getter @Setter
    private String stepID;
    @Getter @Setter
    private String stepName;
    @Getter @Setter
    private String message;
    @Getter @Setter
    private Timestamp runDate;
    @Getter @Setter
    private String runStatus;
    @Getter @Setter
    private String rowNum;


}
