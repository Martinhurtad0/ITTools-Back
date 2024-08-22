package com.example.ITTools.infrastructure.adapters.jpa.user.repositories;

import com.example.ITTools.domain.models.User;
import com.example.ITTools.domain.models.Role;
import com.example.ITTools.domain.ports.out.user.UserRepository;
import com.example.ITTools.infrastructure.entities.UserEntity;
import com.example.ITTools.infrastructure.entities.RoleEntity;
import com.example.ITTools.infrastructure.entrypoints.Audit.Service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class UserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    private final AuditService auditService;

    private final HttpServletRequest request;



    @Autowired
    public UserRepositoryAdapter(JpaUserRepository jpaUserRepository, AuditService auditService, HttpServletRequest request) {
        this.jpaUserRepository = jpaUserRepository;
        this.auditService = auditService;
        this.request = request;
    }

    @Override
    public List<User> findAll() {
        return jpaUserRepository.findAll().stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaUserRepository.findById(id).map(this::mapToDomain);
    }

    @Override

    public User save(User user) {
        UserEntity entity = mapToEntity(user);
        UserEntity savedEntity = jpaUserRepository.save(entity);
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        auditService.audit("UPDATE_USER: " + savedEntity.getUsername(), request);
        return mapToDomain(savedEntity);
    }

    @Override
    public User update(User user) {
        UserEntity existingEntity = jpaUserRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Actualizar los campos existentes con los datos proporcionados
        existingEntity.setUsername(user.getUsername());
        existingEntity.setPassword(user.getPassword());
        existingEntity.setFullName(user.getFull_name());
        existingEntity.setCharge(user.getCharge());
        existingEntity.setArea(user.getArea());
        existingEntity.setStatus(user.isStatus());
        existingEntity.setAuthorities(mapRoles(user.getAuthorities()));

        UserEntity updatedEntity = jpaUserRepository.save(existingEntity);
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        auditService.audit("UPDATE_USER: " + updatedEntity.getUsername(), request);
        return mapToDomain(updatedEntity);
    }

    @Override
    public void delete(UUID id) {
        // Buscar el usuario antes de eliminarlo para obtener sus detalles
        UserEntity user = jpaUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        // Eliminar el usuario de la base de datos
        jpaUserRepository.deleteById(id);

        // Registrar la auditoría
        auditService.audit("DELETE_USER: " + user.getUsername(), request);
    }


    private User mapToDomain(UserEntity entity) {
        User user = new User();
        user.setId(entity.getId());
        user.setUsername(entity.getUsername());
        user.setPassword(entity.getPassword());
        user.setFull_name(entity.getFullName());
        user.setCharge(entity.getCharge());
        user.setArea(entity.getArea());
        user.setStatus(entity.isStatus());
        user.setAuthorities(mapAuthorities(entity.getAuthorities()));
        return user;
    }

    private UserEntity mapToEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setUsername(user.getUsername());
        entity.setPassword(user.getPassword());
        entity.setFullName(user.getFull_name());
        entity.setCharge(user.getCharge());
        entity.setArea(user.getArea());
        entity.setStatus(user.isStatus());
        entity.setAuthorities(mapRoles(user.getAuthorities()));
        return entity;
    }

    private Set<Role> mapAuthorities(Set<RoleEntity> entities) {
        return entities.stream().map(this::mapToRole).collect(Collectors.toSet());
    }

    private Set<RoleEntity> mapRoles(Set<Role> roles) {
        return roles.stream().map(this::mapToEntityRole).collect(Collectors.toSet());
    }

    private Role mapToRole(RoleEntity entity) {
        Role role = new Role();
        role.setId(entity.getId());
        role.setAuthority(entity.getAuthority());
        return role;
    }

    private RoleEntity mapToEntityRole(Role role) {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(role.getId());
        roleEntity.setAuthority(role.getAuthority());
        return roleEntity;
    }
}
