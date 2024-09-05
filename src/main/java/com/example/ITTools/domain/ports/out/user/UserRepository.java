package com.example.ITTools.domain.ports.out.user;

import com.example.ITTools.domain.models.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    List<User> findAll();
    Optional<User> findById(UUID id);
    User update(User user);
    void delete(UUID id);
}
