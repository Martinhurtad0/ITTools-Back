package com.example.ITTools.infrastructure.entrypoints.Audit.DTO;




import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecyclingAuditDTO {

    private int idRecyclingPins;

    private String filename;

    private String pin;

    private String ticket;

    private Long sKu;

    private String controlNo;

    private String dateRecycling;

    private String username;

    private String authorizationFor;

    private String statusPinBefore;

    private String statusPinAfter;

    private String descriptionError;
}