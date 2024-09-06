package com.example.ITTools.infrastructure.config.auth;

import com.example.ITTools.application.repos.AuthRepo;
import com.example.ITTools.application.usecases.GoogleAuthUseCaseImpl;
import com.example.ITTools.application.usecases.LoginUseCaseImpl;
import com.example.ITTools.application.usecases.RegisterUseCaseImpl;
import com.example.ITTools.domain.ports.out.auth.AuthRepositoryPort;
import com.example.ITTools.infrastructure.adapters.jpa.user.repositories.JpaUserRepositoryAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AuthRepositoryConfig {

    @Bean
    public AuthRepo authRepo(
            AuthRepositoryPort authRepositoryPort,
            LoginUseCaseImpl loginUseCase,
            RegisterUseCaseImpl registerUseCase,
            GoogleAuthUseCaseImpl googleAuthUseCase
    ) {
        return new AuthRepo(
                registerUseCase,
                loginUseCase,
                googleAuthUseCase
        );
    }

    @Bean
    public AuthRepositoryPort authRepositoryPort(JpaUserRepositoryAdapter jpaAuthRepositoryAdapter) {
        return jpaAuthRepositoryAdapter;
    }

    @Bean
    public LoginUseCaseImpl loginAuthUseCaseImpl(AuthRepositoryPort authRepositoryPort) {
        return new LoginUseCaseImpl(authRepositoryPort);
    }

    @Bean
    public RegisterUseCaseImpl registerAuthUseCaseImpl(AuthRepositoryPort authRepositoryPort) {
        return new RegisterUseCaseImpl(authRepositoryPort);
    }

    @Bean
    public GoogleAuthUseCaseImpl googleAuthUseCaseImpl(AuthRepositoryPort authRepositoryPort) {
        return new GoogleAuthUseCaseImpl(authRepositoryPort);
    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
