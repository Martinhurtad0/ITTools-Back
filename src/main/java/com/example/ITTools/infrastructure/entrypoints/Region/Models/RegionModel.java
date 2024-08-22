package com.example.ITTools.infrastructure.entrypoints.Region.Models;

import com.example.ITTools.infrastructure.entrypoints.Server.Models.ServerBD_Model;
import com.example.ITTools.infrastructure.entrypoints.Server.Models.AgentModel;
import com.example.ITTools.infrastructure.entrypoints.Region.DTO.RegionDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "regions")
public class RegionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    private Long idRegion;

    @Column(nullable = false)
    @Getter @Setter
    private String nameRegion;

    @Column(nullable = false)
    @Getter @Setter
    private String description;

    @Column(nullable = false)
    @Getter @Setter
    private int status;

    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ServerBD_Model> serversBD;

    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<AgentModel> servers;

    public RegionDTO toDTO() {
        RegionDTO dto = new RegionDTO();
        dto.setIdRegion(this.idRegion);
        dto.setNameRegion(this.nameRegion);
        dto.setDescription(this.description);
        dto.setStatus(this.status);
        dto.setServersBD(this.serversBD != null ? this.serversBD.stream()
                .map(ServerBD_Model::toDTO)
                .collect(Collectors.toSet()) : null);
        dto.setServers(this.servers != null ? this.servers.stream()
                .map(AgentModel::toDTO)
                .collect(Collectors.toSet()) : null);
        return dto;
    }
}
