package com.example.ITTools.domain.ports.in.auth.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GoogleTokenDTO {
    private String token;
}
