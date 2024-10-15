package com.example.ITTools.infrastructure.entrypoints.HttpRequest;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class RequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String method;  // Método HTTP (GET, POST, etc.)
    private String requestUri;  // URI de la solicitud
    private int statusCode;  // Código de estado HTTP
    private String clientIp;  // IP del cliente
    private LocalDateTime timestamp;  // Fecha y hora de la solicitud
}
