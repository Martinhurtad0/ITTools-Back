package com.example.ITTools.infrastructure.entrypoints.DB_ext.Model;



import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class RecyclingRequest {
    @Getter @Setter
    private List<Pins> pinsList;
    @Getter @Setter// Lista de pines a reciclar
    private int serverId;
    @Getter @Setter// ID del servidor
    private String ticket;
    @Getter @Setter// Ticket proporcionado por el usuario
    private String authorizationFor; // Quien autorizó el reciclaje

    // Getters y Setters


}


