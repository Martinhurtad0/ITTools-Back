package com.example.ITTools.domain.ports.in.auth.repo.auth;

import com.example.ITTools.domain.ports.in.auth.dtos.GoogleTokenDTO;

public interface GoogleAuthRepo {
    String authenticateWithGoogle(GoogleTokenDTO googleTokenDTO) throws Exception;
}

