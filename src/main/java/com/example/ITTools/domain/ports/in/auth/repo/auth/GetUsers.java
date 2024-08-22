package com.example.ITTools.domain.ports.in.auth.repo.auth;

import com.example.ITTools.domain.ports.in.auth.dtos.SaveUserDTO;

import java.util.List;

public interface GetUsers {
        List<SaveUserDTO> getAllUsers();
}