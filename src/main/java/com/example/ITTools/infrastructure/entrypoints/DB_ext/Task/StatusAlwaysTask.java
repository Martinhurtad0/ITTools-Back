package com.example.ITTools.infrastructure.entrypoints.DB_ext.Task;

import com.example.ITTools.infrastructure.entrypoints.DB_ext.Service.StatusAlwaysService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StatusAlwaysTask {

    @Autowired
    private StatusAlwaysService statusAlwaysService;


    //esta tarea se ejcuta todos los dias a las 4:20AM
    @Scheduled(cron = "0 20 4 * * ?")
    public void executeStatusAlways(){
        System.out.println("Iniciando tarea de verificación de Status Always...");
        statusAlwaysService. checkStatusAlways();
        System.out.println("Tarea de  Status Always verificación completada.");
    }

}
