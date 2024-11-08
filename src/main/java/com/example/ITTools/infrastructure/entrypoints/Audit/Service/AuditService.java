package com.example.ITTools.infrastructure.entrypoints.Audit.Service;


import com.example.ITTools.infrastructure.entrypoints.Audit.Model.AuditModel;
import com.example.ITTools.infrastructure.entrypoints.Audit.Model.RecyclingAudit;
import com.example.ITTools.infrastructure.entrypoints.Audit.Repository.AuditRepository;
import com.example.ITTools.infrastructure.entrypoints.Audit.Repository.RecyclingAuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class AuditService {

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private RecyclingAuditRepository recyclingAuditRepository;

    public void audit(String userAction, HttpServletRequest request) {
        // Obtener el nombre del usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        // Obtener los roles del usuario
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // Combinar el nombre de usuario y los roles
        String userNameWithRoles = userName + " | " + roles;

        // Obtener la IP del cliente
        String userIP = getClientIP(request);

        // Crear un registro de auditoría
        AuditModel audit = new AuditModel();
        audit.setUserName(userNameWithRoles);
        audit.setUserAction(userAction);
        audit.setDateTime(LocalDateTime.now());
        audit.setUserIP(userIP);

        // Guardar el registro en la base de datos
        auditRepository.save(audit);
    }

    private String getClientIP(HttpServletRequest request) {
        String remoteAddr = request.getHeader("X-Forwarded-For");
        if (remoteAddr == null || remoteAddr.isEmpty()) {
            remoteAddr = request.getRemoteAddr();
        } else {
            // X-Forwarded-For puede contener múltiples IPs en una cadena separada por comas
            String[] ips = remoteAddr.split(",");
            remoteAddr = ips[0].trim(); // La primera IP es la IP original del cliente
        }
        return remoteAddr;
    }

    public class RecyclingAuditException extends RuntimeException {
        public RecyclingAuditException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public RecyclingAudit saveRecyclingAudit(RecyclingAudit auditIn) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        try {
            // Crear una nueva instancia de RecyclingAudit utilizando el constructor adecuado
            RecyclingAudit objAudit = new RecyclingAudit(
                    auditIn.getFilename(),
                    auditIn.getPin(),
                    auditIn.getTicket(),
                    auditIn.getSKu(),
                    auditIn.getControlNo(),
                    auditIn.getDateRecycling(),
                    userName, // Usando el nombre del usuario autenticado
                    auditIn.getAuthorizationFor(),
                    auditIn.getStatusPinBefore(),
                    auditIn.getStatusPinAfter(),
                    auditIn.getDescriptionError()
            );

            // Guardar el objeto de auditoría en el repositorio
            recyclingAuditRepository.save(objAudit);
            return objAudit;

        } catch (Exception e) {
            throw new RecyclingAuditException("Error al guardar la auditoría de reciclaje", e);
        }
    }




}
