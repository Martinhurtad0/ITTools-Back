package com.example.ITTools.infrastructure.controllers.user;

import com.example.ITTools.application.usecases.DeleteUserService;
import com.example.ITTools.application.usecases.GetUsersUseCaseImpl;
import com.example.ITTools.application.usecases.UpdateUserUseCaseImpl;
import com.example.ITTools.domain.ports.in.auth.dtos.SaveUserDTO;
import com.example.ITTools.domain.ports.in.auth.repo.auth.DeleteUserRepo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final GetUsersUseCaseImpl getUsersUseCaseImpl;
    private final UpdateUserUseCaseImpl updateUserUseCaseImpl;
    private final DeleteUserService deleteUserService;

    public UserController(GetUsersUseCaseImpl getUsersUseCaseImpl, UpdateUserUseCaseImpl updateUserUseCaseImpl, DeleteUserService deleteUserService) {
        this.getUsersUseCaseImpl = getUsersUseCaseImpl;
        this.updateUserUseCaseImpl = updateUserUseCaseImpl;
        this.deleteUserService = deleteUserService;
    }

    @GetMapping
    public List<SaveUserDTO> getUsers() {
        return getUsersUseCaseImpl.getAllUsers();
    }

    @PutMapping("/{id}")
    public ResponseEntity<SaveUserDTO> updateUser(
            @PathVariable("id") UUID id,
            @RequestBody SaveUserDTO saveUserDTO) {
        try {
            SaveUserDTO updatedUser = updateUserUseCaseImpl.updateUser(id, saveUserDTO);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") UUID id) {
        try {
            deleteUserService.deleteUser(id); // Usa el caso de uso
            return ResponseEntity.noContent().build(); // HTTP 204 No Content
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // HTTP 404 Not Found
        }
    }
}