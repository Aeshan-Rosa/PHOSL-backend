package com.phosl.backend.repository;

import com.phosl.backend.model.RecommenderCommission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecommenderCommissionRepository extends JpaRepository<RecommenderCommission, Long> {
    List<RecommenderCommission> findByStatus(String status);
    Optional<RecommenderCommission> findBySaleId(Long saleId);
    List<RecommenderCommission> findByRecommenderId(Long recommenderId);
}
