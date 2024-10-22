package com.example.ITTools.application.repos;

import com.example.ITTools.domain.ports.in.auth.dtos.SaveUserDTO;
import java.util.UUID;

public interface IUpdateUserRepo {
    SaveUserDTO updateUser(Long id, SaveUserDTO saveUserDTO);
}
