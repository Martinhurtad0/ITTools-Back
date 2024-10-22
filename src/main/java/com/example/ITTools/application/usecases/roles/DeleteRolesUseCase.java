package com.example.ITTools.application.usecases.roles;

import com.example.ITTools.domain.ports.in.auth.repo.role.DeleteRoles;
import com.example.ITTools.domain.ports.out.role.RoleRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteRolesUseCase implements DeleteRoles {

    private final RoleRepositoryPort roleRepositoryPort;

    public DeleteRolesUseCase(RoleRepositoryPort roleRepositoryPort) {
        this.roleRepositoryPort = roleRepositoryPort;
    }

    @Override
    public void deleteRole(Long id) {
        roleRepositoryPort.delete(id);
    }
}
