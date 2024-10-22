package com.example.ITTools.domain.ports.in.auth.repo.role;

import com.example.ITTools.domain.ports.in.auth.dtos.RolesDTO;

public interface UpdateRoles {
    RolesDTO updateRole(Long id, RolesDTO roleDTO); // Cambia UUID a Long
}
