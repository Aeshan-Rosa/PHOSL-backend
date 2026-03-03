package com.phosl.backend.repository;

import com.phosl.backend.model.PurchasePayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchasePaymentRepository extends JpaRepository<PurchasePayment, Long> {
    List<PurchasePayment> findByPurchaseId(Long purchaseId);
}
