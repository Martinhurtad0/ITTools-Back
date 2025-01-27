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
    private String recyclingDB;
    private int status;
    private int serverType;
    private int logShipping;
    private Long regionId;
}