package com.example.ITTools.infrastructure.entrypoints.DB_ext.Controller;



import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.Pins;


import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.Request.RecyclePinsResponse;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Service.RecyclingPingService;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.*;

@RestController
@RequestMapping("/api/pins")
public class PinsController {

    @Autowired
    private RecyclingPingService recyclingPing;






    // Endpoint para listar un pin en particular
    @GetMapping("/list")
    public ResponseEntity<List<Pins>> listPin(@RequestParam("pin") String pin, @RequestParam("serverId") int serverId) {
        try {
            List<Pins> pins = recyclingPing.listPin(pin, serverId);

            if (pins == null || pins.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(pins);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }


    @PostMapping("/recycle")
    public ResponseEntity<RecyclePinsResponse> recyclePins(
            @RequestBody List<Pins> pinsList,
            @RequestParam int serverId,
            @RequestParam String authorization,
            @RequestParam String ticket,
            @RequestParam (required = false)String filename) {

        // Obtiene el usuario autenticado desde el contexto de seguridad
        // Obtiene el nombre de usuario

        try {
            RecyclePinsResponse response = recyclingPing.recycleMultiplePins(pinsList, serverId, authorization, ticket, filename);


            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error inesperado en /recycle: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<RecyclePinsResponse> recyclePinsFromFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("serverId") int serverId,
            @RequestParam("Authorization") String authorization,
            @RequestParam("ticket") String ticket) {



        try {
            // Llama al método para reciclar pines desde el archivo
            RecyclePinsResponse response = recyclingPing.recyclePinsFromFile(file, serverId, authorization, ticket);
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