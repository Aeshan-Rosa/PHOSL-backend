package com.phosl.backend.controller;

import com.phosl.backend.exception.ResourceNotFoundException;
import com.phosl.backend.model.UserRole;
import com.phosl.backend.repository.UserRoleRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-roles")
public class UserRoleController {

    private final UserRoleRepository userRoleRepository;

    public UserRoleController(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    @GetMapping
    public List<UserRole> list() {
        return userRoleRepository.findAll();
    }

    @PostMapping
    public UserRole create(@RequestBody UserRole role) {
        role.setId(null);
        return userRoleRepository.save(role);
    }

    @PutMapping("/{id}")
    public UserRole update(@PathVariable Long id, @RequestBody UserRole req) {
        UserRole role = userRoleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        role.setRoleName(req.getRoleName());
        return userRoleRepository.save(role);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        userRoleRepository.deleteById(id);
        return Map.of("message", "Role deleted");
    }
}
