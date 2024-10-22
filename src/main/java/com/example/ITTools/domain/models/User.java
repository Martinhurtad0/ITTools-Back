package com.example.ITTools.domain.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
public class User {
    private Long id;
    private String username;
    private String password;
    private String full_name;
    private String charge;
    private String area;
    private boolean status;

    private Set<Role> authorities;

    public User(String email, String password) {
        this.username = email;
        this.password = password;
    }

}
