package com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.Request;


import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.LogTransactionServers;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.Pins;

import java.util.List;



public class RecyclingRequest {
    private List<Pins> selectedPins;
    private LogTransactionServers logTransactionServers;
    private String auditTicket;
    private String authorization;

    // Getters y Setters
    public List<Pins> getSelectedPins() {
        return selectedPins;
    }

    public void setSelectedPins(List<Pins> selectedPins) {
        this.selectedPins = selectedPins;
    }

    public LogTransactionServers getLogTransactionServers() {
        return logTransactionServers;
    }

    public void setLogTransactionServers(LogTransactionServers logTransactionServers) {
        this.logTransactionServers = logTransactionServers;
    }

    public String getAuditTicket() {
        return auditTicket;
    }

    public void setAuditTicket(String auditTicket) {
        this.auditTicket = auditTicket;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }
}