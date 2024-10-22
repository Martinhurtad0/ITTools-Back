package com.example.ITTools.application.repos;

import com.example.ITTools.domain.ports.in.auth.dtos.SaveUserDTO;
import com.example.ITTools.domain.ports.in.auth.repo.auth.UpdateUser;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UpdateUserRepo implements IUpdateUserRepo {

    private final UpdateUser updateUser;

    public UpdateUserRepo(UpdateUser updateUser) {
        this.updateUser = updateUser;
    }

    @Override
    public SaveUserDTO updateUser(Long id, SaveUserDTO saveUserDTO) {
        return updateUser.updateUser(id, saveUserDTO);
    }
}
