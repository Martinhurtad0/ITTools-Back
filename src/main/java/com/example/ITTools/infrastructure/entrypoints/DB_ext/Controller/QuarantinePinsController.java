package com.example.ITTools.infrastructure.entrypoints.DB_ext.Controller;

import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.Pins;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.Request.QuarantinePinsResponse;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Service.QuarantinePinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/quarantine")
public class QuarantinePinsController {

    @Autowired
    private  QuarantinePinService quarantinePinService;

    @PostMapping("/quarantinePins")
    public ResponseEntity<QuarantinePinsResponse> quarantinePins(
            @RequestBody List<Pins> pinsList,
            @RequestParam int serverId,
            @RequestParam String authorization,
            @RequestParam String ticket,
            @RequestParam (required = false)String filename) {

        // Obtiene el usuario autenticado desde el contexto de seguridad
        // Obtiene el nombre de usuario

        try {
            QuarantinePinsResponse response = quarantinePinService.quarantinePins(pinsList, serverId, authorization, ticket, filename);


            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error inesperado en /recycle: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping("/upload")
    public ResponseEntity<QuarantinePinsResponse> recyclePinsFromFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("serverId") int serverId,
            @RequestParam("Authorization") String authorization,
            @RequestParam("ticket") String ticket) {



        try {
            // Llama al método para reciclar pines desde el archivo
            QuarantinePinsResponse response = quarantinePinService.recyclePinsFromFile(file, serverId, authorization, ticket);
            return ResponseEntity.ok(response); // Devuelve la respuesta con un 200 OK

        } catch (IllegalArgumentException e) {
            System.err.println("Error de argumento: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(null); // 415 Unsupported Media Type
        } catch (RuntimeException e) {
            System.err.println("Error al procesar el archivo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 Internal Server Error
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 Internal Server Error
        }
    }




}
