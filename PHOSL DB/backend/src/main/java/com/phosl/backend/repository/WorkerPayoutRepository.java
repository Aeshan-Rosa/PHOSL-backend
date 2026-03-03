package com.phosl.backend.repository;

import com.phosl.backend.model.WorkerPayout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkerPayoutRepository extends JpaRepository<WorkerPayout, Long> {
    List<WorkerPayout> findByPeriodYearAndPeriodMonth(Integer year, Integer month);
    List<WorkerPayout> findByWorkerId(Long workerId);
}
