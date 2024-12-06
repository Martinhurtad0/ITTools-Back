package com.example.ITTools.infrastructure.entrypoints.DB_ext.Task;

import com.example.ITTools.infrastructure.entrypoints.DB_ext.Service.LogShippingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LogShippingTask {

    private final LogShippingService logShippingService;

    public LogShippingTask(LogShippingService logShippingService) {
        this.logShippingService = logShippingService;
    }

    // Ejecutar cada 24 horas (Expresión cron: a las 2:00 AM todos los días)


    public void executeLogShippingStatusCheck() {
        System.out.println("Iniciando tarea de verificación de Log Shipping...");
        logShippingService.checkLogShippingStatus();
        System.out.println("Tarea de verificación completada.");
    }
}
