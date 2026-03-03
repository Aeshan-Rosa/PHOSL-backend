package com.phosl.backend.repository;

import com.phosl.backend.model.WorkerCommission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkerCommissionRepository extends JpaRepository<WorkerCommission, Long> {
    List<WorkerCommission> findByStatus(String status);
    Optional<WorkerCommission> findBySaleId(Long saleId);
}
