package com.example.ITTools.domain.ports.in.auth.repo.auth;

import com.example.ITTools.domain.ports.in.auth.dtos.SaveUserDTO;

import java.util.UUID;

public interface UpdateUser {
    SaveUserDTO updateUser(Long id, SaveUserDTO saveUserDTO);
}
