package com.example.ITTools.infrastructure.entrypoints.Server.Models;

import com.example.ITTools.infrastructure.entrypoints.Region.Models.RegionModel;
import com.example.ITTools.infrastructure.entrypoints.Server.DTO.AgentDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "agents")
public class AgentModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    private int idAgent;

    @Column(nullable = false)
    @Getter @Setter
    private String agentName;

    @Column(nullable = false)
    @Getter @Setter
    private String iPAgent;

    @Column(nullable = false)
    @Getter @Setter
    private String webServiceUrl;

    @Column(nullable = false)
    @Getter @Setter
    private String pathArchive;

    @Getter @Setter
    private int status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    @Getter @Setter
    private RegionModel region;

    // Convertir a DTO
    public AgentDTO toDTO() {
        AgentDTO dto = new AgentDTO();
        dto.setIdAgent(this.idAgent);
        dto.setAgentName(this.agentName);
        dto.setIpAgent(this.iPAgent);
        dto.setWebServiceUrl(this.webServiceUrl);
        dto.setPathArchive(this.pathArchive);
        dto.setStatus(this.status);
        dto.setRegionId(this.region != null ? this.region.getIdRegion() : null);
        return dto;
    }
}
