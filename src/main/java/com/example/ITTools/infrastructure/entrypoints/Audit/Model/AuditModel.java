package com.example.ITTools.infrastructure.entrypoints.Audit.Model;

import com.example.ITTools.infrastructure.entrypoints.Audit.DTO.AuditDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity

@Table(name="audit")

public class AuditModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    private int idAudit;

    @Getter @Setter
    private String userName;
    @Getter @Setter
    private String userAction;
    @Getter @Setter
    private LocalDateTime  dateTime;
    @Getter @Setter
    private String userIP;


    public AuditDTO toDTO(){
        AuditDTO dto = new AuditDTO();
        dto.setIdAudit(this.idAudit);
        dto.setUserName(this.userName);
        dto.setUserAction(this.userAction);
        dto.setDateTime(this.dateTime);
        dto.setUserIP(this.userIP);
        return dto;
    }




 }
