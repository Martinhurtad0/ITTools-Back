package com.example.ITTools.infrastructure.entrypoints.Server.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgentDTO {
    private int idAgent;
    private String agentName;
    private String IPAgent;
    private String webServiceUrl;
    private String pathLog;
    private String pathArchive;
    private int status;
    private Long regionId;

}