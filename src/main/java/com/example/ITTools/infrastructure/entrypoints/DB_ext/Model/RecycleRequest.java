package com.example.ITTools.infrastructure.entrypoints.DB_ext.Model;



import java.util.List;

class RecycleRequest {
    private List<Pins> selectedPins;
    private int serverId;
    private String auditTicket;

    // Getters y Setters
    public List<Pins> getSelectedPins() {
        return selectedPins;
    }

    public void setSelectedPins(List<Pins> selectedPins) {
        this.selectedPins = selectedPins;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getAuditTicket() {
        return auditTicket;
    }

    public void setAuditTicket(String auditTicket) {
        this.auditTicket = auditTicket;
    }



}