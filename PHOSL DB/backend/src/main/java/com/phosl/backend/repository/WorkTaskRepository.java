package com.phosl.backend.repository;

import com.phosl.backend.model.WorkTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface WorkTaskRepository extends JpaRepository<WorkTask, Long> {
    List<WorkTask> findByTaskType(String taskType);
    List<WorkTask> findByStatus(String status);
    List<WorkTask> findByAssignedWorkerId(Long workerId);
    List<WorkTask> findByDueDateBeforeAndStatusIn(LocalDate date, List<String> statuses);
    List<WorkTask> findByDueDateBetween(LocalDate from, LocalDate to);
}
