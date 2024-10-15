package com.example.ITTools.infrastructure.entrypoints.HttpRequest;

import java.io.IOException;
import java.time.LocalDateTime;

import com.example.ITTools.infrastructure.entrypoints.HttpRequest.Repositories.RequestLogRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Autowired
    private RequestLogRepository requestLogRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Antes de procesar la solicitud
        String clientIp = request.getRemoteAddr();
        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        LocalDateTime timestamp = LocalDateTime.now();

        // Procesar la solicitud
        filterChain.doFilter(request, response);

        // Después de procesar la solicitud (capturamos el estado HTTP de la respuesta)
        int statusCode = response.getStatus();

        // Guardar la información de la solicitud en la base de datos
        RequestLog requestLog = new RequestLog();
        requestLog.setMethod(method);
        requestLog.setRequestUri(requestUri);
        requestLog.setStatusCode(statusCode);
        requestLog.setClientIp(clientIp);
        requestLog.setTimestamp(timestamp);

        requestLogRepository.save(requestLog);  // Guardar en la base de datos
    }
}
