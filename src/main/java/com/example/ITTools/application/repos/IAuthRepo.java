package com.example.ITTools.application.repos;

import com.example.ITTools.domain.ports.in.auth.dtos.GoogleTokenDTO;
import com.example.ITTools.domain.ports.in.auth.dtos.LoginDTO;
import com.example.ITTools.domain.ports.in.auth.dtos.SaveUserDTO;

public interface IAuthRepo {
    void register(SaveUserDTO saveUserDTO);
    String login(LoginDTO loginDTO) throws Exception;
    String authenticateWithGoogle(GoogleTokenDTO googleTokenDTO) throws Exception;
}
