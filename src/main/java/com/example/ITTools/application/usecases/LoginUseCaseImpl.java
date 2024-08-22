package com.example.ITTools.application.usecases;

import com.example.ITTools.domain.ports.in.auth.dtos.LoginDTO;
import com.example.ITTools.domain.ports.in.auth.repo.auth.LoginUserRepo;
import com.example.ITTools.domain.ports.out.auth.AuthRepositoryPort;

public class LoginUseCaseImpl implements LoginUserRepo {
    private final AuthRepositoryPort authRepositoryPort;

    public LoginUseCaseImpl(AuthRepositoryPort authRepositoryPort) {
        this.authRepositoryPort = authRepositoryPort;
    }

    @Override
    public String login(LoginDTO loginDTO) throws Exception {
        return authRepositoryPort.login(loginDTO);
    }
}
