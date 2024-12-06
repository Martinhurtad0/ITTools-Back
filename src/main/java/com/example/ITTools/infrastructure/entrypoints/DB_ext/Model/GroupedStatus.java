package com.example.ITTools.infrastructure.entrypoints.DB_ext.Model;

import java.util.List;

public class GroupedStatus {
    private String agName;
    private List<StatusAlways> statuses;

    public GroupedStatus(String agName, List<StatusAlways> statuses) {
        this.agName = agName;
        this.statuses = statuses;
    }

    public String getAgName() {
        return agName;
    }

    public List<StatusAlways> getStatuses() {
        return statuses;
    }
}
