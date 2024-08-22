package com.example.ITTools.application.usecases;

import com.example.ITTools.domain.ports.in.auth.dtos.SaveUserDTO;
import com.example.ITTools.domain.ports.in.auth.repo.auth.RegisterUserRepo;
import com.example.ITTools.domain.ports.out.auth.AuthRepositoryPort;

public class RegisterUseCaseImpl implements RegisterUserRepo {
    private final AuthRepositoryPort authRepositoryPort;

    public RegisterUseCaseImpl(AuthRepositoryPort authRepositoryPort) {
        this.authRepositoryPort = authRepositoryPort;
    }

    @Override
    public void register(SaveUserDTO saveUserDTO) {
        authRepositoryPort.register(saveUserDTO);
    }
}
