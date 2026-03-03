package com.phosl.backend.repository;

import com.phosl.backend.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findBySupplierId(Long supplierId);
    List<Purchase> findByPaymentStatus(String paymentStatus);
    List<Purchase> findByPurchaseDateBetween(LocalDate from, LocalDate to);
}
