package com.example.ITTools.infrastructure.adapters.jpa.role.repositories;

import com.example.ITTools.domain.models.Role;
import com.example.ITTools.domain.ports.in.auth.dtos.RolesDTO;
import com.example.ITTools.domain.ports.out.role.RoleRepositoryPort;
import com.example.ITTools.infrastructure.entities.RoleEntity;
import com.example.ITTools.infrastructure.entrypoints.Audit.Service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RoleRepositoryAdapter implements RoleRepositoryPort {

    private final JpaRoleRepository jpaRoleRepository;
    private final AuditService auditService;
    private final HttpServletRequest request;

    public RoleRepositoryAdapter(JpaRoleRepository jpaRoleRepository, AuditService auditService, HttpServletRequest request) {
        this.jpaRoleRepository = jpaRoleRepository;
        this.auditService = auditService;
        this.request = request;
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

    public class RoleAlreadyExistsException extends RuntimeException {
        public RoleAlreadyExistsException(String authority) {
            super("The role " + authority + " already exists");
        }
    }

    @Override
    public RolesDTO register(RolesDTO roleDTO) {
        // Verificar si el rol ya existe
        if (jpaRoleRepository.findByAuthority(roleDTO.getAuthority()).isPresent()) {
            // Lanzar excepción específica
            throw new UnsupportedOperationException("The role " + roleDTO.getAuthority() + " already exists");
        }

        // Crear un nuevo rol
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setAuthority(roleDTO.getAuthority());
        roleEntity.setDescription(roleDTO.getDescription());
        roleEntity.setStatus(roleDTO.isStatus());

        // Guardar el rol en la base de datos
        RoleEntity savedRoleEntity = jpaRoleRepository.save(roleEntity);
        auditService.audit("Create role: " + savedRoleEntity.getAuthority()+ ", ID: " + savedRoleEntity.getId() ,request);
        // Retornar el DTO del rol guardado
        return mapToDTO(savedRoleEntity);
    }

    @Override
    public Role update(Role role) {
        // Verificar si otro rol con el mismo nombre ya existe
        Optional<RoleEntity> existingRoleEntity = jpaRoleRepository.findByAuthority(role.getAuthority());

        if (existingRoleEntity.isPresent() && !existingRoleEntity.get().getId().equals(role.getId())) {
            throw new RoleAlreadyExistsException(role.getAuthority());
        }

        // Mapear el objeto de dominio a la entidad
        RoleEntity roleEntity = mapToEntity(role);

        // Guardar el rol actualizado
        RoleEntity updatedRoleEntity = jpaRoleRepository.save(roleEntity);

        auditService.audit("Update role: " + updatedRoleEntity.getAuthority()+ ", ID: " + updatedRoleEntity.getId() ,request);

        // Convertir la entidad guardada de nuevo a un objeto de dominio
        return mapToDomain(updatedRoleEntity);
    }

    @Override
    public Optional<Role> findById(UUID id) {
        Optional<RoleEntity> roleEntity = jpaRoleRepository.findById(id);
        return roleEntity.map(this::mapToDomain);
    }

    public class RoleInUseException extends RuntimeException {
        public RoleInUseException(String message) {
            super(message);
        }
    }

    @Override
    public void delete(UUID id) {
        try {
            // Obtener el rol antes de eliminar para registrar detalles de auditoría
            Optional<RoleEntity> roleEntity = jpaRoleRepository.findById(id);
            if (roleEntity.isPresent()) {
                RoleEntity role = roleEntity.get();
                jpaRoleRepository.deleteById(id);
                auditService.audit("Delete role: " + role.getAuthority()+ ", ID: " + role.getId() ,request);
            } else {
                auditService.audit("Delete Role Failed: Role with ID " + id + " not found", request);
            }
        } catch (DataIntegrityViolationException e) {
            auditService.audit("Delete Role Failed: Role with ID " + id + " cannot be deleted due to integrity constraints", request);
            throw new RoleInUseException("Role cannot be deleted because it has associated users.");
        }
    }

    private Role mapToDomain(RoleEntity roleEntity) {
        Role role = new Role();
        role.setId(roleEntity.getId());
        role.setAuthority(roleEntity.getAuthority());
        role.setDescription(roleEntity.getDescription());
        role.setStatus(roleEntity.isStatus());
        return role;
    }

    private RoleEntity mapToEntity(Role role) {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(role.getId());
        roleEntity.setAuthority(role.getAuthority());
        roleEntity.setDescription(role.getDescription());
        roleEntity.setStatus(role.isStatus());
        return roleEntity;
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
