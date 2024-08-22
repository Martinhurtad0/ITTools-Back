package com.example.ITTools.application.usecases.roles;

import com.example.ITTools.domain.models.Role;
import com.example.ITTools.domain.ports.in.auth.dtos.RolesDTO;
import com.example.ITTools.domain.ports.out.role.RoleRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UpdateRolesUseCase {

    private final RoleRepositoryPort roleRepositoryPort;

    public UpdateRolesUseCase(RoleRepositoryPort roleRepositoryPort) {
        this.roleRepositoryPort = roleRepositoryPort;
    }

    public RolesDTO updateRole(UUID id, RolesDTO roleDTO) {
        // Primero encontrar el rol existente por su ID
        Optional<Role> existingRoleOpt = roleRepositoryPort.findById(id);

        if (existingRoleOpt.isPresent()) {
            Role existingRole = existingRoleOpt.get();

            // Actualizar las propiedades del rol existente
            existingRole.setAuthority(roleDTO.getAuthority());
            existingRole.setDescription(roleDTO.getDescription());
            existingRole.setStatus(roleDTO.isStatus());

            // Guardar y devolver el rol actualizado
            Role updatedRole = roleRepositoryPort.update(existingRole);
            return mapToDTO(updatedRole);
        } else {
            throw new RuntimeException("Role not found");
        }
    }

    private RolesDTO mapToDTO(Role role) {
        RolesDTO dto = new RolesDTO();
        dto.setId(role.getId());
        dto.setAuthority(role.getAuthority());
        dto.setDescription(role.getDescription());
        dto.setStatus(role.isStatus());
        return dto;
    }
}
