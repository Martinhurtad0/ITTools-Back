package com.example.ITTools.domain.ports.in.auth.repo.role;

import com.example.ITTools.domain.ports.in.auth.dtos.RolesDTO;

public interface RegisterRoles {
    RolesDTO register(RolesDTO rolesDTO);
}
