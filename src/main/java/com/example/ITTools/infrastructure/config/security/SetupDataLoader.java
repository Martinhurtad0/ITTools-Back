package com.example.ITTools.infrastructure.config.security;

import com.example.ITTools.infrastructure.adapters.jpa.role.repositories.JpaRoleRepository;
import com.example.ITTools.infrastructure.adapters.jpa.user.repositories.JpaUserRepository;
import com.example.ITTools.infrastructure.entities.RoleEntity;
import com.example.ITTools.infrastructure.entities.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    boolean alreadySetup = false;

    @Autowired
    private JpaUserRepository userRepository;

    @Autowired
    private JpaRoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (alreadySetup)
            return;

        createRoleIfNotFound("ADMIN", "Administrator role with full access", true);
        createRoleIfNotFound("USER", "Standard user role with limited access", true);

        Set<RoleEntity> setRole = new HashSet<>();
        RoleEntity adminRole = roleRepository.findByAuthority("ADMIN").get();
        setRole.add(adminRole);
        if (userRepository.findByUsername("admin@test.com").isEmpty()) {
            UserEntity user = new UserEntity("admin@test.com", passwordEncoder.encode("test"));
            user.setAuthorities(setRole);
            user.setStatus(true);
            userRepository.save(user);
        }

        alreadySetup = true;
    }

    @Transactional
    RoleEntity createRoleIfNotFound(String authority, String description, boolean status) {
        return roleRepository.findByAuthority(authority).orElseGet(() -> {
            RoleEntity role = new RoleEntity(authority, description, status);
            roleRepository.save(role);
            return role;
        });
    }
}
