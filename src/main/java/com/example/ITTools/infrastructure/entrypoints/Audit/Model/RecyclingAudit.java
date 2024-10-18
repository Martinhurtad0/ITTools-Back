package com.example.ITTools.infrastructure.entrypoints.Audit.Model;

import com.example.ITTools.infrastructure.entrypoints.Audit.DTO.RecyclingAuditDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Entity
@Table(name="RecyclingTable")
public class RecyclingAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Getter @Setter
    private int idRecyclingPins;
    @Getter @Setter
    private String filename;
    @Getter @Setter
    @NonNull
    private String pin;
    @Getter @Setter
    private String ticket;
    @Getter @Setter
    @NonNull
    private Long sKu;
    @Getter @Setter
    @NonNull
    private String controlNo;
    @Getter @Setter
    @NonNull
    private String dateRecycling;
    @Getter @Setter
    private String username;
    @NonNull
    @Getter @Setter
    private String authorizationFor;
    @Getter @Setter
    private  String statusPinBefore;
    @NonNull
    @Getter @Setter
    private String statusPinAfter;
    @NonNull
    @Getter @Setter
    private String descriptionError;

    public RecyclingAuditDTO toDTO(RecyclingAudit recyclingAudit) {
        RecyclingAuditDTO dto = new RecyclingAuditDTO();

        dto.setIdRecyclingPins(recyclingAudit.getIdRecyclingPins());
        dto.setFilename(recyclingAudit.getFilename());
        dto.setPin(recyclingAudit.getPin());
        dto.setTicket(recyclingAudit.getTicket());
        dto.setSKu(recyclingAudit.getSKu());
        dto.setControlNo(recyclingAudit.getControlNo());
        dto.setDateRecycling(recyclingAudit.getDateRecycling());
        dto.setUsername(recyclingAudit.getUsername());
        dto.setAuthorizationFor(recyclingAudit.getAuthorizationFor());
        dto.setStatusPinBefore(recyclingAudit.getStatusPinBefore());
        dto.setStatusPinAfter(recyclingAudit.getStatusPinAfter());
        dto.setDescriptionError(recyclingAudit.getDescriptionError());

        return dto;
    }

}
