package com.example.ITTools.infrastructure.adapters.jpa.role.repositories;

import com.example.ITTools.domain.models.Role;
import com.example.ITTools.domain.ports.in.auth.dtos.RolesDTO;
import com.example.ITTools.domain.ports.out.role.RoleRepositoryPort;
import com.example.ITTools.infrastructure.entities.RoleEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RoleRepositoryAdapter implements RoleRepositoryPort {

    private final JpaRoleRepository jpaRoleRepository;

    public RoleRepositoryAdapter(JpaRoleRepository jpaRoleRepository) {
        this.jpaRoleRepository = jpaRoleRepository;
    }

    @Override
    public Optional<Role> findByAuthority(String authority) {
        Optional<RoleEntity> roleEntity = jpaRoleRepository.findByAuthority(authority);
        return roleEntity.map(this::mapToDomain);
    }

    @Override
    public List<Role> findAll() {
        return jpaRoleRepository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public RolesDTO register(RolesDTO roleDTO) {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setAuthority(roleDTO.getAuthority());
        roleEntity.setDescription(roleDTO.getDescription());
        roleEntity.setStatus(roleDTO.isStatus());
        RoleEntity savedRoleEntity = jpaRoleRepository.save(roleEntity);
        return mapToDTO(savedRoleEntity);
    }

    @Override
    public Role update(Role role) {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(role.getId());
        roleEntity.setAuthority(role.getAuthority());
        roleEntity.setDescription(role.getDescription());
        roleEntity.setStatus(role.isStatus());

        RoleEntity updatedRoleEntity = jpaRoleRepository.save(roleEntity);
        return mapToDomain(updatedRoleEntity);
    }

    @Override
    public Optional<Role> findById(UUID id) {
        Optional<RoleEntity> roleEntity = jpaRoleRepository.findById(id);
        return roleEntity.map(this::mapToDomain);
    }

    @Override
    public void delete(UUID id) {
        jpaRoleRepository.deleteById(id);
    }

    private Role mapToDomain(RoleEntity roleEntity) {
        Role role = new Role();
        role.setId(roleEntity.getId());
        role.setAuthority(roleEntity.getAuthority());
        role.setDescription(roleEntity.getDescription());
        role.setStatus(roleEntity.isStatus());
        return role;
    }

    private RolesDTO mapToDTO(RoleEntity roleEntity) {
        RolesDTO dto = new RolesDTO();
        dto.setId(roleEntity.getId());
        dto.setAuthority(roleEntity.getAuthority());
        dto.setDescription(roleEntity.getDescription());
        dto.setStatus(roleEntity.isStatus());
        return dto;
    }
}
