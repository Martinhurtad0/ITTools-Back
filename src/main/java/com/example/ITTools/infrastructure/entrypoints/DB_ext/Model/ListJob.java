package com.example.ITTools.infrastructure.entrypoints.DB_ext.Model;

import lombok.Getter;
import lombok.Setter;

public class ListJob {
    @Getter @Setter
    private String idJob;
    @Getter @Setter// ID del trabajo
    private String jobName;
    @Getter @Setter// Nombre del trabajo
    private String startDate;
    @Getter @Setter// Fecha de inicio de ejecución
    private String stepName;
    @Getter @Setter
    private String scheduledDate;
    @Getter @Setter// Fecha programada para la próxima ejecución// Nombre del paso actual
    private String stopDate;
    @Getter @Setter// Fecha de detención de ejecución
    private String executionTime; // Tiempo de ejecución en minutos

    // Constructor vacío
    public ListJob() {
    }

}