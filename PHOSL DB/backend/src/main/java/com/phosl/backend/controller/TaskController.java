package com.phosl.backend.controller;

import com.phosl.backend.exception.ResourceNotFoundException;
import com.phosl.backend.model.WorkTask;
import com.phosl.backend.repository.WorkTaskRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final WorkTaskRepository taskRepository;

    public TaskController(WorkTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping
    public List<WorkTask> list(@RequestParam(required = false) String type,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) Long assignedWorkerId,
                               @RequestParam(required = false) LocalDate from,
                               @RequestParam(required = false) LocalDate to) {
        if (type != null) return taskRepository.findByTaskType(type);
        if (status != null) return taskRepository.findByStatus(status);
        if (assignedWorkerId != null) return taskRepository.findByAssignedWorkerId(assignedWorkerId);
        if (from != null && to != null) return taskRepository.findByDueDateBetween(from, to);
        return taskRepository.findAll();
    }

    @GetMapping("/{id}")
    public WorkTask get(@PathVariable Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    @PostMapping
    public WorkTask create(@RequestBody WorkTask task) {
        task.setId(null);
        return taskRepository.save(task);
    }

    @PutMapping("/{id}")
    public WorkTask update(@PathVariable Long id, @RequestBody WorkTask req) {
        WorkTask task = get(id);
        task.setTaskType(req.getTaskType());
        task.setPianoId(req.getPianoId());
        task.setCustomerId(req.getCustomerId());
        task.setAssignedWorkerId(req.getAssignedWorkerId());
        task.setDueDate(req.getDueDate());
        task.setStatus(req.getStatus());
        return taskRepository.save(task);
    }

    @PutMapping("/{id}/status")
    public WorkTask updateStatus(@PathVariable Long id, @RequestParam String status) {
        WorkTask task = get(id);
        task.setStatus(status);
        return taskRepository.save(task);
    }

    @GetMapping("/overdue")
    public List<WorkTask> overdue() {
        return taskRepository.findByDueDateBeforeAndStatusIn(LocalDate.now(), List.of("TODO", "IN_PROGRESS"));
    }

    @GetMapping("/upcoming")
    public List<WorkTask> upcoming(@RequestParam(defaultValue = "7") int days) {
        return taskRepository.findByDueDateBetween(LocalDate.now(), LocalDate.now().plusDays(days));
    }
}
