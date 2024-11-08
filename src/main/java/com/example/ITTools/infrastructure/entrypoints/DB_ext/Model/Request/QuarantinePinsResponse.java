package com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.Request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class QuarantinePinsResponse {

    @Getter @Setter
    private List<String> quarantinePins;
    @Getter  @Setter
    private List<String> failedPins;
    @Getter @Setter
    private  List<String> errorMessages;
    @Getter @Setter
    private List<String>satisfyingMessages;

    public QuarantinePinsResponse(List<String> quarantinePins, List<String> failedPins, List<String> errorMessages, List<String>satisfyingMessages){
        this.quarantinePins = quarantinePins;
        this.failedPins = failedPins;
        this.errorMessages = errorMessages;
        this.satisfyingMessages = satisfyingMessages;

    }
}
