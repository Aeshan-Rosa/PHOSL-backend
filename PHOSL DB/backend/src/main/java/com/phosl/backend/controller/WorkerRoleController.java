package com.phosl.backend.controller;

import com.phosl.backend.exception.ResourceNotFoundException;
import com.phosl.backend.model.WorkerRole;
import com.phosl.backend.repository.WorkerRoleRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/worker-roles")
public class WorkerRoleController {

    private final WorkerRoleRepository workerRoleRepository;

    public WorkerRoleController(WorkerRoleRepository workerRoleRepository) {
        this.workerRoleRepository = workerRoleRepository;
    }

    @GetMapping
    public List<WorkerRole> list() { return workerRoleRepository.findAll(); }

    @PostMapping
    public WorkerRole create(@RequestBody WorkerRole role) {
        role.setId(null);
        return workerRoleRepository.save(role);
    }

    @PutMapping("/{id}")
    public WorkerRole update(@PathVariable Long id, @RequestBody WorkerRole req) {
        WorkerRole role = workerRoleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Worker role not found"));
        role.setRoleName(req.getRoleName());
        return workerRoleRepository.save(role);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        workerRoleRepository.deleteById(id);
        return Map.of("message", "Worker role deleted");
    }
}
