package com.phosl.backend.repository;

import com.phosl.backend.model.Recommender;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecommenderRepository extends JpaRepository<Recommender, Long> {
}
