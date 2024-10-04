package com.example.ITTools.infrastructure.entrypoints.DB_ext.Controller;


import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.LogTransactionServers;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.Pins;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Service.RecyclingPingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/pins")
public class PinsController {

    @Autowired
    private RecyclingPingService recyclingPing;



    @GetMapping("/db-connection")
    public ResponseEntity<String> testConnection(@RequestParam int serverId) {
        try {
            recyclingPing.testDatabaseConnection(serverId);
            return ResponseEntity.ok("Prueba de conexión realizada. Revisa los logs para más detalles.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al realizar la prueba de conexión: " + e.getMessage());
        }
    }

    // Endpoint para subir un archivo Excel
    @PostMapping("/upload")
    public ResponseEntity<List<Pins>> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("serverId") int serverId) {
        try {
            // Validar que el archivo sea de tipo Excel
            String contentType = file.getContentType();
            if (!contentType.equals("application/vnd.ms-excel") &&
                    !contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                return ResponseEntity.badRequest().body(null); // Responder con un error si el archivo no es Excel
            }
            // Llamar al método que procesa el archivo y devuelve los pines
            List<Pins> pins = recyclingPing.uploadFile(file, serverId);

            // Devolver los pines procesados correctamente
            return ResponseEntity.ok(pins);

        } catch (RuntimeException e) {
            // Manejar la excepción y devolver un mensaje de error más informativo
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }






    // Endpoint para consultar pines
    @PostMapping("/consult")
    public ResponseEntity<List<Pins>> consultPins(@RequestBody List<Pins> listExcel, @RequestBody LogTransactionServers serv) {
        try {
            // Llamar al método para consultar los pines desde la base de datos
            List<Pins> pins = recyclingPing.consultPins(listExcel, serv);
            return ResponseEntity.ok(pins);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Endpoint para listar un pin en particular
    @GetMapping("/list")
    public ResponseEntity<List<Pins>> listPin(@RequestParam("pin") String pin, @RequestParam("serverId") int serverId) {
        try {
            List<Pins> pins = recyclingPing.listPin(pin, serverId);

            // Verifica si la lista es nula o está vacía
            if (pins == null || pins.isEmpty()) {
                return ResponseEntity.noContent().build(); // Devuelve 204 No Content si no hay pines
            }

            return ResponseEntity.ok(pins); // Devuelve 200 OK con la lista de pines
        } catch (RuntimeException e) {
            // Manejo de excepciones específicas
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList()); // Devuelve 500 Internal Server Error con una lista vacía
        } catch (Exception e) {
            // Manejo de otras excepciones
            return ResponseEntity.badRequest().body(Collections.emptyList()); // Devuelve 400 BAD_REQUEST con una lista vacía
        }
    }


    // Endpoint para actualizar un pin
    @PostMapping("/update")
    public ResponseEntity<Boolean> updatePin(@RequestBody Pins pinIn, @RequestBody LogTransactionServers serv) {
        try {
            // Llamar al método para actualizar un pin
            boolean updated = recyclingPing.updatePin(pinIn, serv);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
