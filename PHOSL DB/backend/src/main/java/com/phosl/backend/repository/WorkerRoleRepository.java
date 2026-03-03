package com.phosl.backend.repository;

import com.phosl.backend.model.WorkerRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkerRoleRepository extends JpaRepository<WorkerRole, Long> {
}
