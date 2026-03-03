package com.phosl.backend.repository;

import com.phosl.backend.model.InstallmentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface InstallmentScheduleRepository extends JpaRepository<InstallmentSchedule, Long> {
    List<InstallmentSchedule> findByPlanId(Long planId);
    List<InstallmentSchedule> findByDueDateBeforeAndStatusIn(LocalDate dueDate, List<String> statuses);
    List<InstallmentSchedule> findByDueDateBetween(LocalDate from, LocalDate to);
}
