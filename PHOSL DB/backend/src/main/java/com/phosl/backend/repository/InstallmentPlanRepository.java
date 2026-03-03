package com.phosl.backend.repository;

import com.phosl.backend.model.InstallmentPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstallmentPlanRepository extends JpaRepository<InstallmentPlan, Long> {
    Optional<InstallmentPlan> findBySaleId(Long saleId);
}
