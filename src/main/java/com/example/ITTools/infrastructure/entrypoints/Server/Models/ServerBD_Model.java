package com.example.ITTools.infrastructure.entrypoints.Server.Models;

import com.example.ITTools.infrastructure.entrypoints.Region.Models.RegionModel;
import com.example.ITTools.infrastructure.entrypoints.Server.DTO.ServerBD_DTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "serversBD")
public class ServerBD_Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    private int idServer;

    @Getter @Setter
    private String serverName;

    @Getter @Setter
    private String description;

    @Getter @Setter
    private String ipServer;

    @Getter @Setter
    private String ipServerSecondary;

    @Getter @Setter
    private String portServer;

    @Getter @Setter
    private String instance;

    @Getter @Setter
    private String serverDB;

    @Getter @Setter
    private String userLogin;

    @Getter @Setter
    private String password;

    @Getter @Setter
    @Column(name = "RecyclingDB")
    private String recyclingDB;

    @Getter @Setter
    private int status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    @Getter @Setter
    private RegionModel region;

    // Convertir a DTO
    public ServerBD_DTO toDTO() {
        ServerBD_DTO dto = new ServerBD_DTO();

        dto.setIdServer(this.idServer);
        dto.setServerName(this.serverName);
        dto.setDescription(this.description);
        dto.setIpServer(this.ipServer);
        dto.setIpServerSecondary(this.ipServerSecondary);
        dto.setPortServer(this.portServer);
        dto.setInstance(this.instance);
        dto.setServerDB(this.serverDB);
        dto.setUserLogin(this.userLogin);
        dto.setPassword(this.password);
        dto.setRecyclingDB(this.recyclingDB);
        dto.setStatus(this.status);

        dto.setRegionId(this.region != null ? this.region.getIdRegion() : null);
        return dto;
    }
}
