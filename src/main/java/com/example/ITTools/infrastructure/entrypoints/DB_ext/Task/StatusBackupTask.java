package com.example.ITTools.infrastructure.entrypoints.DB_ext.Task;

import com.example.ITTools.infrastructure.entrypoints.DB_ext.Service.StatusBackupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StatusBackupTask {

    @Autowired
    private StatusBackupService statusBackupService;


    //esta tarea se ejcuta todos los dias a las 4:30AM
   @Scheduled(cron = "0 30 4 * * ?")
   public void executeStatusBackup(){
        System.out.println("Iniciando tarea de verificación de Status Backup...");
        statusBackupService.checkStatusBackup();
        System.out.println("Tarea de Status Backup verificación completada.");

    }
}
