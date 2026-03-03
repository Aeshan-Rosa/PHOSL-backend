package com.phosl.backend.model;

import javax.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Data
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String phone;

    private String email;
    private String address;

    @Column(name = "how_found")
    private String howFound = "Other";

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;
}
