package com.example.ITTools.application.usecases;

import com.example.ITTools.domain.models.Role;
import com.example.ITTools.domain.models.User;
import com.example.ITTools.domain.ports.in.auth.dtos.SaveUserDTO;
import com.example.ITTools.domain.ports.in.auth.repo.auth.GetUsers;
import com.example.ITTools.domain.ports.out.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GetUsersUseCaseImpl implements GetUsers {
    private final UserRepository userRepository;

    public GetUsersUseCaseImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<SaveUserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private SaveUserDTO mapToDTO(User user) {
        SaveUserDTO dto = new SaveUserDTO();
        dto.setId(user.getId()); // Mapea el ID del usuario
        dto.setEmail(user.getUsername());
        dto.setPassword(user.getPassword());
        dto.setFull_name(user.getFull_name()); // Campo actualizado
        dto.setCharge(user.getCharge());
        dto.setArea(user.getArea());
        dto.setStatus(user.isStatus());
        dto.setRoles(user.getAuthorities().stream().map(Role::getAuthority).collect(Collectors.toList()));
        return dto;
    }
}
