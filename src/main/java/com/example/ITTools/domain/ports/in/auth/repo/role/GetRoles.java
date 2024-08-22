package com.example.ITTools.domain.ports.in.auth.repo.role;

import com.example.ITTools.domain.ports.in.auth.dtos.RolesDTO;

import java.util.List;

public interface GetRoles {
    List<RolesDTO> getAllRoles();
}
