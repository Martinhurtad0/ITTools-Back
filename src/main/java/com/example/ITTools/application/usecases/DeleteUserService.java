package com.example.ITTools.application.usecases;

import com.example.ITTools.domain.ports.in.auth.repo.auth.DeleteUserRepo;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteUserService {

    private final DeleteUserRepo deleteUserRepo;

    public DeleteUserService(DeleteUserRepo deleteUserRepo) {
        this.deleteUserRepo = deleteUserRepo;
    }

    public void deleteUser(Long id) {
        deleteUserRepo.deleteUser(id);
    }
}
