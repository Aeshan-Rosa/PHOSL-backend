package com.phosl.backend.model;

import javax.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "sale_items")
@Data
public class SaleItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sale_id", nullable = false)
    private Long saleId;

    @Column(name = "piano_id", nullable = false, unique = true)
    private Long pianoId;

    @Column(name = "unit_price")
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(name = "line_discount")
    private BigDecimal lineDiscount = BigDecimal.ZERO;

    @Column(name = "line_total")
    private BigDecimal lineTotal = BigDecimal.ZERO;
}
