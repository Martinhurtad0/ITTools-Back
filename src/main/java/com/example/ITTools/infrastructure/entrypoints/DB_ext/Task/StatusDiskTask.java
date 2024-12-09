package com.example.ITTools.infrastructure.entrypoints.DB_ext.Task;

import com.example.ITTools.infrastructure.entrypoints.DB_ext.Service.StatusDiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StatusDiskTask {
    @Autowired
    private StatusDiskService statusDiskService;

    @Scheduled(fixedRate = 20400000)
    public void executeStatusDisk(){
        System.out.println("Iniciando tarea de verificación de Status Disk...");
        statusDiskService.checkStatusDisk();
        System.out.println("Tarea de Status Disk verificación completada.");

    }
}
