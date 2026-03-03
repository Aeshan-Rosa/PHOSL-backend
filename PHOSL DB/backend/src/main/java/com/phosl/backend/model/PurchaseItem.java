package com.phosl.backend.model;

import javax.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_items")
@Data
public class PurchaseItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "purchase_id", nullable = false)
    private Long purchaseId;

    @Column(name = "piano_id", nullable = false, unique = true)
    private Long pianoId;

    @Column(name = "buy_price")
    private BigDecimal buyPrice = BigDecimal.ZERO;

    @Column(name = "shipping_cost")
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @Column(name = "repair_cost")
    private BigDecimal repairCost = BigDecimal.ZERO;

    @Column(name = "other_cost")
    private BigDecimal otherCost = BigDecimal.ZERO;

    @Column(name = "landed_cost")
    private BigDecimal landedCost = BigDecimal.ZERO;
}
