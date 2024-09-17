package com.example.ITTools.infrastructure.config.security;

import com.example.ITTools.infrastructure.adapters.jpa.role.repositories.RoleRepositoryAdapter;
import com.example.ITTools.infrastructure.adapters.jpa.user.repositories.UserRepositoryAdapter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserRepositoryAdapter.EmailAlreadyExistsException.class)
    public ResponseEntity<String> handleEmailAlreadyExistsException(UserRepositoryAdapter.EmailAlreadyExistsException ex) {
        // Retornar 409 Conflict con mensaje de error
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(RoleRepositoryAdapter.RoleAlreadyExistsException.class)
    public ResponseEntity<String> handleRoleAlreadyExistsException(RoleRepositoryAdapter.RoleAlreadyExistsException ex) {
        // Retornar 409 Conflict con mensaje de error
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        // Retornar 404 Not Found u otro estado según la excepción
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}




