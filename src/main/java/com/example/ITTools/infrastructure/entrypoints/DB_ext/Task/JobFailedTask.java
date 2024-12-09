package com.example.ITTools.infrastructure.entrypoints.DB_ext.Task;

import com.example.ITTools.infrastructure.entrypoints.DB_ext.Service.JobFailedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobFailedTask {

    @Autowired
    private JobFailedService jobFailedService;

    @Scheduled(fixedRate = 19800000)
    public void executeJobFailed() {
        System.out.println("Iniciando tarea de verificación de Log Failed...");
        jobFailedService.checkJobFailed();
        System.out.println("Tarea de Jobs Failed verificación completada.");
    }
}
