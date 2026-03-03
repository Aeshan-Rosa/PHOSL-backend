package com.phosl.backend.controller;

import com.phosl.backend.dto.PasswordUpdateRequest;
import com.phosl.backend.exception.ResourceNotFoundException;
import com.phosl.backend.model.User;
import com.phosl.backend.repository.UserRepository;
import javax.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<User> list() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public User get(@PathVariable Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @PostMapping
    public User create(@RequestBody User user) {
        user.setId(null);
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        return userRepository.save(user);
    }

    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @RequestBody User req) {
        User user = get(id);
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setRoleId(req.getRoleId());
        user.setIsActive(req.getIsActive());
        return userRepository.save(user);
    }

    @PutMapping("/{id}/password")
    public Map<String, String> updatePassword(@PathVariable Long id, @Valid @RequestBody PasswordUpdateRequest request) {
        User user = get(id);
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return Map.of("message", "Password updated");
    }

    @PutMapping("/{id}/disable")
    public User disable(@PathVariable Long id) {
        User user = get(id);
        user.setIsActive(false);
        return userRepository.save(user);
    }

    @PutMapping("/{id}/enable")
    public User enable(@PathVariable Long id) {
        User user = get(id);
        user.setIsActive(true);
        return userRepository.save(user);
    }
}
