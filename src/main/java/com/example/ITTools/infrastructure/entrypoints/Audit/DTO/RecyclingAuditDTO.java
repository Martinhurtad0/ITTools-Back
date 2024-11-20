package com.example.ITTools.infrastructure.entrypoints.Audit.DTO;




import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class RecyclingAuditDTO {

    private int idRecyclingPins;

    private String filename;

    private String pin;

    private String ticket;

    private String sKu;

    private String controlNo;

    private Date recycleDate;

    private String username;

    private String authorizationFor;

    private String statusPinBefore;

    private String statusPinAfter;

    private String descriptionError;
}