package com.example.ITTools.infrastructure.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;  // Filtro personalizado para JWT
    private final AuthenticationProvider authProvider;              // AuthenticationProvider configurado

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())  // Deshabilitar CSRF si estás usando tokens JWT
                .authorizeHttpRequests(authRequest -> authRequest
                        .requestMatchers("/auth/login/**").permitAll()  // Permitir acceso a login sin autenticación
                        .anyRequest().authenticated()  // Cualquier otra solicitud debe estar autenticada
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // Sin estado para usar JWT
                )
                .authenticationProvider(authProvider)  // Proveedor de autenticación
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)  // Filtro JWT antes del UsernamePasswordAuthenticationFilter
                .build();
    }

}
