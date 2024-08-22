package com.example.ITTools.infrastructure.entrypoints.Service.Models;


import com.example.ITTools.infrastructure.entrypoints.Server.Models.AgentModel;
import com.example.ITTools.infrastructure.entrypoints.Service.DTO.ServiceDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "services")
public class ServiceModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    private int idService;
    @Getter @Setter
    private String serviceName;
    @Getter @Setter
    private String description;
    @Getter @Setter
    private String command;
    @Getter @Setter
    private String pathCommand;
    @Getter @Setter
    private String logFile;
    @Getter @Setter
    private int type;
    @Getter @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdAgent", nullable = false)
    private AgentModel server;

    public ServiceDTO toDTO() {
        ServiceDTO dto = new ServiceDTO();
        dto.setIdService(this.idService);
        dto.setServiceName(this.serviceName);
        dto.setDescription(this.description);
        dto.setCommand(this.command);
        dto.setPathCommand(this.pathCommand);
        dto.setLogFile(this.logFile);
        dto.setType(this.type); // Asegúrate de que 'type' esté definido en tu modelo
        dto.setIdAgent(this.server != null ? this.server.getIdAgent() : null);
        // Asumiendo que el ID del servidor es un int
        return dto;
    }
}




