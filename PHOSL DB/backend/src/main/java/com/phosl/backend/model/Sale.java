package com.phosl.backend.model;

import javax.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales")
@Data
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_no", unique = true)
    private String invoiceNo;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "sold_date", nullable = false)
    private LocalDate soldDate;

    @Column(name = "salesperson_id")
    private Long salespersonId;

    @Column(name = "recommender_id")
    private Long recommenderId;

    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal discount = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "payment_plan")
    private String paymentPlan = "FULL";

    @Column(name = "warranty_months")
    private Integer warrantyMonths = 0;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;
}
