package com.example.ITTools.domain.ports.in.auth.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Data
public class SaveUserDTO {
    private UUID id;
    private String email;
    private String password;
    private String full_name;
    private String charge;
    private String area;
    private boolean status;
    private List<String> roles;
} 
