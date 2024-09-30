package com.example.ITTools.infrastructure.entrypoints.DB_ext.Controller;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.ListJob;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.ListWho5;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Autowired
    private DatabaseService databaseService;

    /**
     * Endpoint para obtener los trabajos en ejecución de un servidor específico.
     *
     * @param serverId ID del servidor para el cual se quieren obtener los trabajos.
     * @return Lista de trabajos en ejecución.
     */
    @GetMapping("/runningJobs/{serverId}")
    public List<ListJob> getRunningJobs(@PathVariable int serverId) {
        return databaseService.listRunningJobs(serverId);
    }

    /**
     * Endpoint para obtener los trabajos programados que comienzan por 'maintenance'.
     *
     * @param serverId ID del servidor para el cual se quieren obtener los trabajos programados.
     * @return Lista de trabajos programados.
     */
    @GetMapping("/scheduled/{serverId}")
    public List<ListJob> getScheduledJobs(@PathVariable int serverId) {
        return databaseService.listScheduledJobs(serverId);
    }
    /**
     * Endpoint para obtener la lista de procesos en ejecución de un servidor específico.
     *
     * @param serverId ID del servidor para obtener los procesos.
     * @return Lista de procesos en ejecución.
     */
    @GetMapping("/runningProcess/{serverId}")
    public List<ListWho5> getRunningProcesses(@PathVariable int serverId) {
        try {
            return databaseService.listQuerys(serverId);
        } catch (Exception ex) {
            // Manejar cualquier excepción no verificada lanzada por el servicio
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al obtener procesos", ex);
        }
    }

    //Metodo para mater el proceso en ejecucion
    @PostMapping("/killProcess/{serverId}/{spid}")
    public ResponseEntity<String> killProcess(@PathVariable int serverId, @PathVariable String spid) {
        try {
            boolean result = databaseService.killProcess(serverId, spid);

            if (result) {
                return ResponseEntity.ok("Proceso con SPID " + spid + " ha sido terminado.");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("No se pudo matar el proceso con SPID: " + spid);
            }
        } catch (DatabaseService.ProcessTerminationException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al intentar matar el proceso: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error inesperado: " + ex.getMessage());
        }
    }

}
