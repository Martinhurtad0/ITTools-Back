package com.example.ITTools.infrastructure.entrypoints.DB_ext.Task;

import com.example.ITTools.infrastructure.entrypoints.DB_ext.Service.JobFailedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobFailedTask {

    @Autowired
    private JobFailedService jobFailedService;
 //esta tarea se ejcuta todos los dias a las 4:10AM
    @Scheduled(cron = "0 10 4 * * ?")
    public void executeJobFailed() {
        System.out.println("Iniciando tarea de verificación de Log Failed...");
        jobFailedService.checkJobFailed();
        System.out.println("Tarea de Jobs Failed verificación completada.");
    }
}
