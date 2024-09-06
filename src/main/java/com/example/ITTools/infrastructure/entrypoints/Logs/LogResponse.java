package com.example.ITTools.infrastructure.entrypoints.Logs;

import java.util.List;

public class LogResponse {
    private List<String> logs;

    public List<String> getLogs() {
        return logs;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }
}
