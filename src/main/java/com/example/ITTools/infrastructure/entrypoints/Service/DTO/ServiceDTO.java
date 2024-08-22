package com.example.ITTools.infrastructure.entrypoints.Service.DTO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ServiceDTO {
    private int idService;
    private String serviceName;
    private String description;
    private String command;
    private String pathCommand;
    private String logFile;
    private int type;
    private Integer  idAgent;

}
