package com.example.ITTools.application.usecases.roles;

import com.example.ITTools.domain.ports.in.auth.dtos.RolesDTO;
import com.example.ITTools.domain.ports.in.auth.repo.role.RegisterRoles;
import com.example.ITTools.domain.ports.out.role.RoleRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class RegisterRolesUseCase implements RegisterRoles {
    private final RoleRepositoryPort roleRepositoryPort;

    public RegisterRolesUseCase(RoleRepositoryPort roleRepositoryPort) {
        this.roleRepositoryPort = roleRepositoryPort;
    }

    @Override
    public RolesDTO register(RolesDTO rolesDTO) {
        return roleRepositoryPort.register(rolesDTO);
    }
}
