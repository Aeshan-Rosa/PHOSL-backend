package com.phosl.backend.model;

import javax.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "purchase_payments")
@Data
public class PurchasePayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "purchase_id", nullable = false)
    private Long purchaseId;

    @Column(name = "pay_date", nullable = false)
    private LocalDate payDate;

    @Column(nullable = false)
    private BigDecimal amount;

    private String method = "BankTransfer";
}
