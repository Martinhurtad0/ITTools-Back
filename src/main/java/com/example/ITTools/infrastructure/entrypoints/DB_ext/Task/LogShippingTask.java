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

    //esta tarea se ejcuta todos los dias a las 4:00AM
    @Scheduled(cron = "0 0 4 * * ?")

    public void executeLogShippingStatusCheck() {
        System.out.println("Iniciando tarea de verificación de Log Shipping...");
        logShippingService.checkLogShippingStatus();
        System.out.println("Tarea de verificación completada.");
    }
}
