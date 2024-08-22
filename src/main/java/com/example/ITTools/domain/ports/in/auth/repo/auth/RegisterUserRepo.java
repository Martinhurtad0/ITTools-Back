package com.example.ITTools.domain.ports.in.auth.repo.auth;


import com.example.ITTools.domain.ports.in.auth.dtos.SaveUserDTO;

public interface RegisterUserRepo {
    void register(SaveUserDTO request);
}
