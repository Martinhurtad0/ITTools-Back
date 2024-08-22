package com.example.ITTools.application.repos;

import com.example.ITTools.domain.ports.in.auth.dtos.GoogleTokenDTO;
import com.example.ITTools.domain.ports.in.auth.dtos.LoginDTO;
import com.example.ITTools.domain.ports.in.auth.dtos.SaveUserDTO;
import com.example.ITTools.domain.ports.in.auth.repo.auth.GoogleAuthRepo;
import com.example.ITTools.domain.ports.in.auth.repo.auth.LoginUserRepo;
import com.example.ITTools.domain.ports.in.auth.repo.auth.RegisterUserRepo;

public class AuthRepo implements IAuthRepo {

    private final RegisterUserRepo registerUserRepo;
    private final LoginUserRepo loginUserRepo;
    private final GoogleAuthRepo googleAuthRepo;

    public AuthRepo(RegisterUserRepo registerUserRepo, LoginUserRepo loginUserRepo, GoogleAuthRepo googleAuthRepo) {
        this.registerUserRepo = registerUserRepo;
        this.loginUserRepo = loginUserRepo;
        this.googleAuthRepo = googleAuthRepo;
    }

    @Override
    public void register(SaveUserDTO saveUserDTO) {
        registerUserRepo.register(saveUserDTO);
    }

    @Override
    public String login(LoginDTO loginDTO) throws Exception {
        return loginUserRepo.login(loginDTO);
    }

    @Override
    public String authenticateWithGoogle(GoogleTokenDTO googleTokenDTO) throws Exception {
        return googleAuthRepo.authenticateWithGoogle(googleTokenDTO);
    }
}
