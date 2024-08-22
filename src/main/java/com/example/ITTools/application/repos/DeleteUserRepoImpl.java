package com.example.ITTools.application.repos;

import com.example.ITTools.domain.ports.in.auth.repo.auth.DeleteUserRepo;
import com.example.ITTools.domain.ports.out.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteUserRepoImpl implements DeleteUserRepo {

    private final UserRepository userRepository;

    public DeleteUserRepoImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void deleteUser(UUID id) {
        userRepository.delete(id);
    }
}
