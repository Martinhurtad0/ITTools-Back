package com.example.ITTools.domain.ports.in.auth.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class RolesDTO {
    private Long id; // Cambia UUID a Long
    private String authority;
    private String description;
    private boolean status;
}
