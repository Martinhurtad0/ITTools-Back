package com.example.ITTools.infrastructure.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/hello")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String hello() {
        return "Hello World!";
    }

    @GetMapping("/bay")
    @PreAuthorize("hasAuthority('CLIENT')")
    public String bay() {
        return "Bay World!";
    }

    @GetMapping("/health")
    public String health() {
        return "Health World!";
    }
}
