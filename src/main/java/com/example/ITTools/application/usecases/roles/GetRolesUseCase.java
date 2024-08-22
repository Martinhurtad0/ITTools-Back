package com.example.ITTools.application.usecases.roles;

import com.example.ITTools.domain.models.Role;
import com.example.ITTools.domain.ports.in.auth.dtos.RolesDTO;
import com.example.ITTools.domain.ports.in.auth.repo.role.GetRoles;
import com.example.ITTools.domain.ports.out.role.RoleRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GetRolesUseCase implements GetRoles {
    private final RoleRepositoryPort roleRepositoryPort;

    public GetRolesUseCase(RoleRepositoryPort roleRepositoryPort) {
        this.roleRepositoryPort = roleRepositoryPort;
    }

    @Override
    public List<RolesDTO> getAllRoles() {
        return roleRepositoryPort.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
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
