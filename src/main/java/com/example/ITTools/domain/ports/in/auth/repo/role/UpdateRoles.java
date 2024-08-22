package com.example.ITTools.domain.ports.in.auth.repo.role;

import com.example.ITTools.domain.ports.in.auth.dtos.RolesDTO;

import java.util.UUID;

public interface UpdateRoles {
    RolesDTO updateRole(UUID id, RolesDTO roleDTO);
}
