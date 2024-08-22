package com.example.ITTools.application.usecases;

import com.example.ITTools.domain.ports.in.auth.dtos.GoogleTokenDTO;
import com.example.ITTools.domain.ports.in.auth.repo.auth.GoogleAuthRepo;
import com.example.ITTools.domain.ports.out.auth.AuthRepositoryPort;

public class GoogleAuthUseCaseImpl implements GoogleAuthRepo {
    private final AuthRepositoryPort authRepositoryPort;

    public GoogleAuthUseCaseImpl(AuthRepositoryPort authRepositoryPort) {
        this.authRepositoryPort = authRepositoryPort;
    }

    @Override
    public String authenticateWithGoogle(GoogleTokenDTO googleTokenDTO) throws Exception {
        return authRepositoryPort.authenticateWithGoogle(googleTokenDTO);
    }
}
