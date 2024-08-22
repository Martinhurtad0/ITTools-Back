package com.example.ITTools.infrastructure.entrypoints.Region.DTO;

import com.example.ITTools.infrastructure.entrypoints.Server.DTO.ServerBD_DTO;
import com.example.ITTools.infrastructure.entrypoints.Server.DTO.AgentDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

public class RegionDTO {

    @Getter @Setter
    private Long idRegion;

    @Getter @Setter
    private String nameRegion;

    @Getter @Setter
    private String description;

    @Getter @Setter
    private int status;

    @Getter @Setter
    private Set<AgentDTO> servers;  // Para ServerDTO

    @Getter @Setter
    private Set<ServerBD_DTO> serversBD;  // Para ServerBD_DTO

}
