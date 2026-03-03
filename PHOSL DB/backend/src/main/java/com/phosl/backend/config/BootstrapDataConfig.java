package com.phosl.backend.config;

import com.phosl.backend.model.User;
import com.phosl.backend.model.UserRole;
import com.phosl.backend.repository.UserRepository;
import com.phosl.backend.repository.UserRoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BootstrapDataConfig {

    @Bean
    CommandLineRunner seedUsers(UserRoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            UserRole adminRole = roleRepository.findByRoleName("ADMIN").orElseGet(() -> {
                UserRole role = new UserRole();
                role.setRoleName("ADMIN");
                return roleRepository.save(role);
            });

            roleRepository.findByRoleName("STAFF").orElseGet(() -> {
                UserRole role = new UserRole();
                role.setRoleName("STAFF");
                return roleRepository.save(role);
            });

            userRepository.findByUsername("admin").orElseGet(() -> {
                User user = new User();
                user.setUsername("admin");
                user.setEmail("admin@phosl.local");
                user.setPasswordHash(passwordEncoder.encode("admin123"));
                user.setRoleId(adminRole.getId());
                user.setIsActive(true);
                return userRepository.save(user);
            });
        };
    }
}
