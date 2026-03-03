package com.phosl.backend.model;

import javax.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchases")
@Data
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "purchase_code", unique = true)
    private String purchaseCode;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "payment_status")
    private String paymentStatus = "PENDING";

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;
}
