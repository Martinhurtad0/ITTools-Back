package com.example.ITTools.domain.ports.out.role;


import com.example.ITTools.domain.models.Role;
import com.example.ITTools.domain.models.User;
import com.example.ITTools.domain.ports.in.auth.dtos.RolesDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepositoryPort {
     Optional<Role> findByAuthority(String authority);
     List<Role> findAll();
     RolesDTO register(RolesDTO roleDTO);
     Role update(Role role);
     Optional<Role> findById(UUID id);
     void delete(UUID id);
}
