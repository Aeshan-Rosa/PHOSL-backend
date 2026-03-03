package com.phosl.backend.controller;

import com.phosl.backend.dto.AuthResponse;
import com.phosl.backend.dto.LoginRequest;
import com.phosl.backend.exception.ResourceNotFoundException;
import com.phosl.backend.model.User;
import com.phosl.backend.model.UserRole;
import com.phosl.backend.repository.UserRepository;
import com.phosl.backend.repository.UserRoleRepository;
import com.phosl.backend.security.JwtService;
import javax.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository,
                          UserRoleRepository userRoleRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.getLogin())
                .or(() -> userRepository.findByUsername(request.getLogin()))
                .orElseThrow(() -> new ResourceNotFoundException("Invalid credentials"));

        if (!Boolean.TRUE.equals(user.getIsActive()) || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResourceNotFoundException("Invalid credentials");
        }

        UserRole role = userRoleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        return AuthResponse.builder()
                .token(jwtService.generateToken(user.getId(), user.getUsername()))
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(role.getRoleName())
                .build();
    }

    @PostMapping("/logout")
    public Map<String, String> logout() {
        return Map.of("message", "Logged out (client should drop token)");
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        UserRole role = userRoleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "role", role.getRoleName(),
                "isActive", user.getIsActive()
        );
    }
}
