package com.invitique.config;

import com.invitique.domain.model.Role;
import com.invitique.domain.model.User;
import com.invitique.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "admin@inviteque.com";
        if (!userRepository.existsByEmail(adminEmail)) {
            System.out.println("=== ADMIN INITIALIZER: Creating default admin user ===");
            User admin = User.builder()
                    .name("System Admin")
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode("InvitiqueAdmin2026!Secure"))
                    .provider(User.AuthProvider.LOCAL)
                    .build();
            admin.addRole(Role.ADMIN);
            admin.addRole(Role.USER);
            userRepository.save(admin);
            System.out.println("=== ADMIN INITIALIZER: Admin user created successfully ===");
        } else {
            System.out.println("=== ADMIN INITIALIZER: Admin user already exists ===");
        }
    }
}
