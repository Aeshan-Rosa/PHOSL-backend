package com.phosl.backend.model;

import javax.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "recommender_commissions")
@Data
public class RecommenderCommission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sale_id", unique = true)
    private Long saleId;

    @Column(name = "recommender_id", nullable = false)
    private Long recommenderId;

    @Column(name = "commission_rate")
    private BigDecimal commissionRate;

    @Column(name = "commission_amount")
    private BigDecimal commissionAmount;

    private String status = "PENDING";
}
