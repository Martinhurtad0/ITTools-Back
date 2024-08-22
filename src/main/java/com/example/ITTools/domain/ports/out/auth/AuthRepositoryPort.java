package com.example.ITTools.domain.ports.out.auth;

import com.example.ITTools.domain.ports.in.auth.dtos.GoogleTokenDTO;
import com.example.ITTools.domain.ports.in.auth.dtos.LoginDTO;
import com.example.ITTools.domain.ports.in.auth.dtos.SaveUserDTO;

public interface AuthRepositoryPort {
    String authenticateWithGoogle(GoogleTokenDTO googleTokenDTO) throws Exception;
    void register(SaveUserDTO saveUserDTO);
    String login(LoginDTO loginDTO) throws Exception;
}
