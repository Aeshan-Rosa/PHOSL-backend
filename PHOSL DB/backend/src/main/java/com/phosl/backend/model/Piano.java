package com.phosl.backend.model;

import javax.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pianos")
@Data
public class Piano {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "piano_code", unique = true)
    private String pianoCode;

    @Column(nullable = false)
    private String brand;

    private String model;

    @Column(name = "serial_number", unique = true)
    private String serialNumber;

    @Column(name = "piano_type")
    private String pianoType = "Upright";

    private String color;

    @Column(name = "condition_type")
    private String conditionType = "Used";

    @Column(name = "manufacture_year")
    private Integer manufactureYear;

    private String status = "IN_STOCK";

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "expected_selling_price")
    private BigDecimal expectedSellingPrice;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;
}
