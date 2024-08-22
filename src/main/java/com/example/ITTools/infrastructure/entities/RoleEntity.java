package com.example.ITTools.infrastructure.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@Table(name = "role")
public class RoleEntity implements GrantedAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String authority;
    private String description;
    private boolean status;


    public RoleEntity(String authority, String description, boolean status) {
        this.authority = authority;
        this.description = description;
        this.status = status;
    }

    @Override
    public String getAuthority() {
        return authority;
    }
}   
