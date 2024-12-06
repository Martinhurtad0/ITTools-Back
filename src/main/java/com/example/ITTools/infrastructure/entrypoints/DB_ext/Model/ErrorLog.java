package com.example.ITTools.infrastructure.entrypoints.DB_ext.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "R_Error")
public class ErrorLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    private Integer id;
    @Getter @Setter
    private String ip;
    @Getter @Setter
    @Column(name = "server_name")
    private String serverName;

    @Getter @Setter
    private String sp;
    @Getter @Setter
    private String description;
    @Getter @Setter
    private LocalDateTime timestamp;


}
