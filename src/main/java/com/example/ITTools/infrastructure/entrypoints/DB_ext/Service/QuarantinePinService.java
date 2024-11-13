package com.example.ITTools.infrastructure.entrypoints.DB_ext.Service;

import com.example.ITTools.infrastructure.entrypoints.Audit.Model.RecyclingAudit;
import com.example.ITTools.infrastructure.entrypoints.Audit.Service.AuditService;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.Pins;

import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.Request.QuarantinePinsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
@Service

public class QuarantinePinService {
    @Autowired
    private RecyclingPingService recyclingPingService;

    @Autowired
    private AuditService auditService;


    public QuarantinePinsResponse quarantinePins(List<Pins> pinsList, int serverId, String authorizedBy, String ticketNumber, String fileName) {
        List<String> quarantinedPins = new ArrayList<>();
        List<String> notUpdatedPins = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();
        List<String> satisfyingMessages= new ArrayList<>();
        JdbcTemplate jdbcTemplate = recyclingPingService.getJdbcTemplate(serverId);

        for (Pins pin : pinsList) {
            String statusBefore = null;
            String statusAfter = null; // Inicializa antes del bloque try
            String error = ""; // Inicializa antes del bloque try

            try {
                // Obtener detalles del pin desde la base de datos
                List<Pins> pinDetails = recyclingPingService.listPin(pin.getPin(), serverId);
                if (pinDetails.isEmpty()) {
                    errorMessages.add("The pin " + pin.getPin() + " was not found in the database.");
                    error = "Pin not found in database."; // Mensaje de error
                    RecyclingAudit audit = new RecyclingAudit(
                            fileName,
                            pin.getPin(),
                            ticketNumber,
                            pin.getProductId(),
                            pin.getControlNo(),
                            null,
                            null, // El usuario se obtiene automáticamente
                            authorizedBy,
                            null,
                            null,
                            error
                    );
                    auditService.saveRecyclingAudit(audit);
                    notUpdatedPins.add(pin.getPin());
                    continue; // Continuar con el siguiente pin
                }

                Pins pinFromDb = pinDetails.get(0);
                statusBefore = pinFromDb.getState(); // Estado antes de la actualización
                int pinStatusId = pinFromDb.getPinStatusId(); // Obtener el estado del pin por ID


                // Solo actualizar si el estado es igual a 1 (suponiendo que 1 es el estado 'SOLD')
                if (pinStatusId == 1) {
                    String sqlUpdate = "UPDATE pins SET pinstatusid = ?, recycle_date = GETDATE() WHERE pin = ? AND product_id = ? AND pinstatusid = 1";


                    int updatedRows = jdbcTemplate.update(sqlUpdate,
                            17,  // Estado 'CUARENTENA'
                            pin.getPin(),
                            pin.getProductId());


                    if (updatedRows > 0) {
                        quarantinedPins.add(pin.getPin());
                        statusAfter = "QUARANTINE"; // Asumimos que el estado después de mover a cuarentena es 'CUARENTENA'
                        error = "Pin moved to quarantine successfully";

                        RecyclingAudit audit = new RecyclingAudit(
                                fileName,
                                pin.getPin(),
                                ticketNumber,
                                pin.getProductId(),
                                pin.getControlNo(),
                                null, // Fecha de reciclaje no aplica aquí
                                null, // El usuario se obtiene automáticamente
                                authorizedBy,
                                statusBefore,
                                statusAfter,
                                error
                        );
                        auditService.saveRecyclingAudit(audit);
                        satisfyingMessages.add("Pin moved to quarantine: " + pin.getPin());
                    } else {
                        errorMessages.add("Pin not updated: " + pin.getPin());
                        notUpdatedPins.add(pin.getPin());
                    }
                } else {
                    // Si el estado no es 1, agregar a la lista de pines no actualizados
                    errorMessages.add("The pin " + pin.getPin() + "  is not in the 'AVALAIBLE' state. It cannot be moved to quarantine.");
                    notUpdatedPins.add(pin.getPin());
                    error = "Error moving to quarantine, incorrect status: " + pinStatusId; // Mensaje de error

                    RecyclingAudit audit = new RecyclingAudit(
                            fileName,
                            pin.getPin(),
                            ticketNumber,
                            pin.getProductId(),
                            pin.getControlNo(),
                            null, // Fecha de reciclaje no aplica aquí
                            null, // El usuario se obtiene automáticamente
                            authorizedBy,
                            statusBefore,
                            statusAfter,
                            error
                    );
                    auditService.saveRecyclingAudit(audit);
                }

            }  catch (Exception e) {
                errorMessages.add("Unexpected error moving pin to quarantine: " + (pin != null ? pin.getPin() : "Unknown") + " - " + e.getMessage());
                notUpdatedPins.add(pin != null ? pin.getPin() : "Desconocido");
            }
        }

        return new QuarantinePinsResponse(quarantinedPins, notUpdatedPins, errorMessages, satisfyingMessages);
    }



    public QuarantinePinsResponse recyclePinsFromFile(MultipartFile file, int serverId, String authorizedBy, String ticketNumber) {
        List<Pins> pinsList = new ArrayList<>();
        String fileName = file.getOriginalFilename();

        try (InputStream inputStream = file.getInputStream()) {
            if (fileName != null && fileName.endsWith(".xlsx")) {
                pinsList = recyclingPingService.readPinsFromExcel(inputStream);
            } else if (fileName != null && (fileName.endsWith(".csv") || fileName.endsWith(".txt") || fileName.endsWith(".dat"))) {
                pinsList =recyclingPingService.readPinsFromFile(inputStream);
            } else {
                throw new IllegalArgumentException("File type not supported. Please use .xlsx, .csv, or .txt.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + e.getMessage(), e);
        }

        return quarantinePins(pinsList, serverId, authorizedBy, ticketNumber, fileName);
    }







}
