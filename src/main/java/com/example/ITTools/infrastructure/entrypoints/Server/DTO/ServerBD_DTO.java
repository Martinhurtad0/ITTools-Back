package com.example.ITTools.infrastructure.entrypoints.Server.DTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerBD_DTO {
    private int idServer;
    private String serverName;
    private String description;
    private String ipServer;
    private String portServer;
    private String instance;
    private String serverDB;
    private String userLogin;
    private String password;
    private String dbFR;
    private int status;
    private Long regionId;
}