package com.example.ITTools.domain.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.util.UUID;

@Data
@NoArgsConstructor
public class Role implements GrantedAuthority {
    private Long id; // Cambia UUID a Long
    private String authority;
    private String description;
    private boolean status;

    @Override
    public String getAuthority() {
        return authority;
    }
}
