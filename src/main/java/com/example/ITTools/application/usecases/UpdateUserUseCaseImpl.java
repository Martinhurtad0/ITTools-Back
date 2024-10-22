package com.example.ITTools.application.usecases;

import com.example.ITTools.domain.models.Role;
import com.example.ITTools.domain.models.User;
import com.example.ITTools.domain.ports.in.auth.dtos.SaveUserDTO;
import com.example.ITTools.domain.ports.in.auth.repo.auth.UpdateUser;
import com.example.ITTools.domain.ports.out.role.RoleRepositoryPort;
import com.example.ITTools.domain.ports.out.user.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UpdateUserUseCaseImpl implements UpdateUser {

    private final UserRepository userRepository;
    private final RoleRepositoryPort roleRepository;

    public UpdateUserUseCaseImpl(UserRepository userRepository, RoleRepositoryPort roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public SaveUserDTO updateUser(Long id, SaveUserDTO saveUserDTO) {
        Optional<User> existingUser = userRepository.findById(id);

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            // Actualiza los campos del usuario con los datos del DTO
            user.setUsername(saveUserDTO.getEmail());
            if (saveUserDTO.getPassword() != null && !saveUserDTO.getPassword().isEmpty()) {
                user.setPassword(saveUserDTO.getPassword());
            }
            user.setFull_name(saveUserDTO.getFull_name());
            user.setCharge(saveUserDTO.getCharge());
            user.setArea(saveUserDTO.getArea());
            user.setStatus(saveUserDTO.isStatus());

            // Asigna los roles
            Set<Role> roles = new HashSet<>();
            for (String roleName : saveUserDTO.getRoles()) {
                Role role = roleRepository.findByAuthority(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
                roles.add(role);
            }
            user.setAuthorities(roles);

            // Guarda el usuario actualizado
            User updatedUser = userRepository.update(user);
            return mapToDTO(updatedUser);
        } else {
            throw new UsernameNotFoundException("User not found with ID: " + id);
        }
    }

    private SaveUserDTO mapToDTO(User user) {
        SaveUserDTO dto = new SaveUserDTO();
        dto.setEmail(user.getUsername());
        dto.setPassword(user.getPassword());
        dto.setFull_name(user.getFull_name());
        dto.setCharge(user.getCharge());
        dto.setArea(user.getArea());
        dto.setStatus(user.isStatus());
        dto.setRoles(user.getAuthorities().stream()
                .map(Role::getAuthority)
                .collect(Collectors.toList()));
        return dto;
    }
}
