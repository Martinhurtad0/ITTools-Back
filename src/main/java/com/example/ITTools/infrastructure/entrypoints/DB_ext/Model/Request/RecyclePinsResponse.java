package com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.Request;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class RecyclePinsResponse {

    @Getter @Setter
    private List<String> recycledPins;
    @Getter @Setter
    private List<String> failedPins;
    @Getter @Setter
    private List<String> errorMessages;
    @Getter @Setter
    private List<String> satisfyingMessages;


    public RecyclePinsResponse(List<String> recycledPins, List<String> failedPins, List<String> errorMessages, List<String> satisfyingMessages) {
        this.recycledPins = recycledPins;
        this.failedPins = failedPins;
        this.errorMessages = errorMessages;
        this.satisfyingMessages = satisfyingMessages;
    }


}
