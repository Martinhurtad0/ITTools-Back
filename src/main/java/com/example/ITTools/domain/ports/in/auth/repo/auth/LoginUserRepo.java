package com.example.ITTools.domain.ports.in.auth.repo.auth;


import com.example.ITTools.domain.ports.in.auth.dtos.LoginDTO;

public interface LoginUserRepo {
    String login(LoginDTO saveUser) throws Exception;
}
