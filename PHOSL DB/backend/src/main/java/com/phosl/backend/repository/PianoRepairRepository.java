package com.phosl.backend.repository;

import com.phosl.backend.model.PianoRepair;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PianoRepairRepository extends JpaRepository<PianoRepair, Long> {
    List<PianoRepair> findByStatus(String status);
    List<PianoRepair> findByPianoId(Long pianoId);
}
