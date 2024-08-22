package com.example.ITTools.infrastructure.adapters.jpa.role.repositories;


import com.example.ITTools.infrastructure.entities.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaRoleRepository extends JpaRepository<RoleEntity, UUID> {
    Optional<RoleEntity> findByAuthority(String authority);
    Optional<RoleEntity> findById(UUID id);
}

