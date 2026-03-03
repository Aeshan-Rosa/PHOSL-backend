package com.phosl.backend.model;

import javax.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "installment_plans")
@Data
public class InstallmentPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sale_id", unique = true)
    private Long saleId;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "down_payment")
    private BigDecimal downPayment = BigDecimal.ZERO;

    @Column(name = "remaining_balance")
    private BigDecimal remainingBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer months;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "due_day_of_month")
    private Integer dueDayOfMonth = 1;

    private String status = "ACTIVE";
}
