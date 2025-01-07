package com.example.ITTools.infrastructure.entrypoints.DB_ext.Controller;


import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.ErrorLog;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.LogShippingStatus;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Service.LogShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/log-shipping")
public class LogShippingController {

    @Autowired
    private LogShippingService logShippingService;

    /**
     * Endpoint para ejecutar la lógica de verificación de Log Shipping.
     */
    @GetMapping("/status")
    @ResponseStatus(HttpStatus.OK)
    public List<LogShippingStatus> getLogShippingStatus() {
        return logShippingService.getAll();
    }

    @GetMapping("/{sp}")
    public ResponseEntity<List<ErrorLog>> getErrorsBySp(@PathVariable String sp) {
        List<ErrorLog> errors = logShippingService.findErrorsBySp(sp);
        return ResponseEntity.ok(errors);
    }

    /**
     * Endpoint para ejecutar el proceso de verificación de Log Shipping y almacenar los resultados.
     */
    @PostMapping("/check")
    public ResponseEntity<String> checkLogShippingStatus() {
        try {
            logShippingService.checkLogShippingStatus();
            return ResponseEntity.ok("Log shipping status checked successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
        }
    }
}