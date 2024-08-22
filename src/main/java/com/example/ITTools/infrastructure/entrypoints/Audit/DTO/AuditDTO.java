package com.example.ITTools.infrastructure.entrypoints.Audit.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public class AuditDTO {
    @Getter @Setter
    private int idAudit;
    @Getter @Setter
    private String  userName;
    @Getter @Setter
    private String userAction;
    @Getter @Setter
    private LocalDateTime dateTime;
    @Getter @Setter
    private String userIP;

}
