package com.phosl.backend.repository;

import com.phosl.backend.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findBySoldDateBetween(LocalDate from, LocalDate to);
    List<Sale> findByCustomerId(Long customerId);
    List<Sale> findByPaymentPlan(String paymentPlan);
    List<Sale> findBySalespersonId(Long salespersonId);
}
