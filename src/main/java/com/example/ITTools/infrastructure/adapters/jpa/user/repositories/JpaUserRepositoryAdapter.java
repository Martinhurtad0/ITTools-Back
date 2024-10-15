package com.example.ITTools.infrastructure.adapters.jpa.user.repositories;

import com.example.ITTools.application.usecases.GoogleTokenService;
import com.example.ITTools.domain.ports.in.auth.dtos.GoogleTokenDTO;
import com.example.ITTools.domain.ports.in.auth.dtos.LoginDTO;
import com.example.ITTools.domain.ports.in.auth.dtos.SaveUserDTO;
import com.example.ITTools.domain.ports.out.auth.AuthRepositoryPort;
import com.example.ITTools.infrastructure.adapters.jpa.role.repositories.JpaRoleRepository;
import com.example.ITTools.infrastructure.config.security.JwtService;
import com.example.ITTools.infrastructure.entities.RoleEntity;
import com.example.ITTools.infrastructure.entities.UserEntity;
import com.example.ITTools.infrastructure.entrypoints.Audit.Service.AuditService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JpaUserRepositoryAdapter implements AuthRepositoryPort {

    private final JpaUserRepository userRepo;
    private final JpaRoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final GoogleTokenService googleTokenService;
    private final HttpServletRequest request;
    private final JwtService jwtService;

    @Autowired
    private AuditService auditService;

    @Value("${google.clientId}")
    private String googleClientId;


    public JpaUserRepositoryAdapter(JpaUserRepository userRepo, JpaRoleRepository roleRepo, JwtService jwtService, PasswordEncoder passwordEncoder, GoogleTokenService googleTokenService, HttpServletRequest request) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
        this.googleTokenService = googleTokenService;
        this.jwtService = jwtService;
        this.request = request;
    }

    @Override
    public String authenticateWithGoogle(GoogleTokenDTO googleTokenDTO) throws Exception {
        try {
            // Verificar el token de Google usando el servicio de GoogleTokenService
            GoogleIdToken.Payload payload = googleTokenService.verifyToken(googleTokenDTO.getToken());
            if (payload == null) {
                throw new Exception("Invalid Google token");
            }
            String email = payload.getEmail(); // Obtener el email desde el payload

            // Verificar si el usuario ya está registrado en la base de datos
            UserEntity user = userRepo.findByUsername(email).orElseGet(() -> {
                // Si el usuario no está registrado, crear uno nuevo
                UserEntity newUser = new UserEntity();
                newUser.setUsername(email);
                newUser.setFullName((String) payload.get("name")); // Obtener el nombre del payload
                newUser.setAuthorities(Collections.singleton(roleRepo.findByAuthority("USER")
                        .orElseThrow(() -> new RuntimeException("Default role 'USER' not found"))));
                newUser.setStatus(true);

                return userRepo.save(newUser); // Guardar el nuevo usuario en la base de datos
            });

            // Generar un token JWT para la aplicación
            Instant now = Instant.now();
            long expiry = 36000L; // Duración del token: 10 horas

            // Obtener los roles del usuario y convertirlos en un string
            String roles = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(" "));

            // Crear los claims del JWT
            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .issuer("self") // Definir el emisor del token
                    .issuedAt(now)  // Fecha de emisión
                    .expiresAt(now.plusSeconds(expiry)) // Fecha de expiración
                    .subject(user.getUsername()) // Username del usuario autenticado
                    .claim("scope", roles) // Agregar los roles en el claim "scope"
                    .build();

            // Generar el token JWT
            String token = jwtService.getToken(user);  // Usando el método getToken de JwtService

            // Retornar el token JWT generado
            return token;

        } catch (Exception e) {
            throw new Exception("Google authentication failed: " + e.getMessage(), e);
        }
    }


    @Override
    public String login(LoginDTO login) throws Exception {
        // Autenticar al usuario
        UserEntity findUser = userRepo.findByUsername(login.getEmail())
                .orElseThrow(() -> new Exception("User not found"));

        // Verificar la contraseña
        if (!passwordEncoder.matches(login.getPassword(), findUser.getPassword())) {
            throw new Exception("Wrong password or username");
        }

        // Generar el token JWT utilizando JwtService
        String token = jwtService.getToken(findUser);

        // Retornar el token directamente
        return token;
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
        List<String> roles = saveUserDTO.getRoles();
        if (roles == null || roles.isEmpty()) {
            // Si no se especifican roles, asigna el rol "USER" por defecto
            RoleEntity defaultRole = roleRepo.findByAuthority("USER")
                    .orElseThrow(() -> new RuntimeException("Default role 'USER' not found"));
            setRole.add(defaultRole);
        } else {
            for (String role : roles) {
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
        auditService.audit("Create user : " + savedUser.getUsername()+ ", ID: " + savedUser.getId(), request);
    }

}

