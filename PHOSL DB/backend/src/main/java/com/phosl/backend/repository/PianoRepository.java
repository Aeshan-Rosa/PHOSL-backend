package com.phosl.backend.repository;

import com.phosl.backend.model.Piano;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PianoRepository extends JpaRepository<Piano, Long> {
    List<Piano> findByStatus(String status);
    List<Piano> findByStatusAndBrandContainingIgnoreCase(String status, String brand);
    List<Piano> findByLocationId(Long locationId);
}
