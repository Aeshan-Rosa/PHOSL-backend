package com.phosl.backend.model;

import javax.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "suppliers")
@Data
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    private String phone;
    private String email;
    private String address;
    private String country;

    @Column(name = "reliability_rating")
    private Integer reliabilityRating;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;
}
