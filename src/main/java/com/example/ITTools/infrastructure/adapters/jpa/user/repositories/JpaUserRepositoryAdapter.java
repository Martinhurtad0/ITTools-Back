package com.example.ITTools.infrastructure.adapters.jpa.user.repositories;


import com.example.ITTools.application.usecases.GoogleTokenService;
import com.example.ITTools.domain.ports.in.auth.dtos.GoogleTokenDTO;
import com.example.ITTools.domain.ports.in.auth.dtos.LoginDTO;
import com.example.ITTools.domain.ports.in.auth.dtos.SaveUserDTO;
import com.example.ITTools.domain.ports.out.auth.AuthRepositoryPort;
import com.example.ITTools.infrastructure.adapters.jpa.role.repositories.JpaRoleRepository;
import com.example.ITTools.infrastructure.entities.RoleEntity;
import com.example.ITTools.infrastructure.entities.UserEntity;
import com.example.ITTools.infrastructure.entrypoints.Audit.Service.AuditService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.util.Value;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JpaUserRepositoryAdapter implements AuthRepositoryPort {

    private final JpaUserRepository userRepo;
    private final JpaRoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final GoogleTokenService googleTokenService;
    private final HttpServletRequest request;

    @Autowired
    private AuditService auditService;

    @Value("${google.clientId}")
    private String googleClientId;


    public JpaUserRepositoryAdapter(JpaUserRepository userRepo, JpaRoleRepository roleRepo, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder, GoogleTokenService googleTokenService, HttpServletRequest request) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.googleTokenService = googleTokenService;
        this.request=request;

    }


    @Override
    public String authenticateWithGoogle(GoogleTokenDTO googleTokenDTO) throws Exception {
        try {
            // Verificar el token de Google
            GoogleIdToken.Payload payload = googleTokenService.verifyToken(googleTokenDTO.getToken());
            String email = payload.getEmail(); // Método correcto para obtener el email

            // Verificar si el usuario ya está registrado
            UserEntity user = userRepo.findByUsername(email).orElseGet(() -> {
                // Si no está registrado, crear un nuevo usuario
                UserEntity newUser = new UserEntity();
                newUser.setUsername(email);
                newUser.setFullName((String) payload.get("name")); // Usa el método get(String) correctamente
                newUser.setAuthorities(Collections.singleton(roleRepo.findByAuthority("USER")
                        .orElseThrow(() -> new RuntimeException("Default role 'USER' not found"))));
                newUser.setStatus(true);

                return userRepo.save(newUser);
            });

            // Generar un JWT para la aplicación
            Instant now = Instant.now();
            long expiry = 36000L; // 10 hours

            String roles = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(" "));

            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .issuer("self")
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(expiry))
                    .subject(user.getUsername())
                    .claim("scope", roles)
                    .build();

            return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        } catch (Exception e) {
            throw new Exception("Google authentication failed: " + e.getMessage(), e);
        }
    }


    @Override
    public void register(SaveUserDTO saveUserDTO) {
        // Verificar si el usuario ya existe
        if (userRepo.findByUsername(saveUserDTO.getEmail()).isPresent()) {
            throw new UnsupportedOperationException("The email " + saveUserDTO.getEmail() + " already exists");
        }

        // Crear un nuevo usuario
        UserEntity user = new UserEntity();
        Set<RoleEntity> setRole = new HashSet<>();

        // Asignar roles al usuario
        if (saveUserDTO.getRoles().isEmpty()) {
            // Si no se especifican roles, asigna el rol "USER" por defecto
            RoleEntity defaultRole = roleRepo.findByAuthority("USER")
                    .orElseThrow(() -> new RuntimeException("Default role 'USER' not found"));
            setRole.add(defaultRole);
        } else {
            for (String role : saveUserDTO.getRoles()) {
                RoleEntity roleFind = roleRepo.findByAuthority(role)
                        .orElseThrow(() -> new RuntimeException("Role " + role + " not found"));
                setRole.add(roleFind);
            }
        }

        // Configurar el usuario
        user.setAuthorities(setRole);
        user.setUsername(saveUserDTO.getEmail());
        String passwordString = passwordEncoder.encode(saveUserDTO.getPassword());
        user.setPassword(passwordString);
        user.setFullName(saveUserDTO.getFull_name());
        user.setCharge(saveUserDTO.getCharge());
        user.setArea(saveUserDTO.getArea());
        user.setStatus(true);

        // Guardar el usuario en la base de datos
        UserEntity savedUser = userRepo.save(user);

        // Registrar la auditoría
        auditService.audit("Create User : " + savedUser.getUsername() , request);
    }


    @Override
    public String login(LoginDTO login) throws Exception {
        UserEntity findUser = userRepo.findByUsername(login.getEmail()).orElseThrow();

        if (!passwordEncoder.matches(login.getPassword(), findUser.getPassword())) {
            throw new Exception("Wrong password or username");
        }

        Instant now = Instant.now();
        long expiry = 36000L;

        String roles = findUser.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiry))
                .subject(findUser.getUsername())
                .claim("scope", roles)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

}
