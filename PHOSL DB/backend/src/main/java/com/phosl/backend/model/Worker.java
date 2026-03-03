package com.phosl.backend.model;

import javax.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "workers")
@Data
public class Worker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_code", unique = true)
    private String employeeCode;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String phone;
    private String email;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "base_salary")
    private BigDecimal baseSalary = BigDecimal.ZERO;

    @Column(name = "commission_rate")
    private BigDecimal commissionRate = BigDecimal.ZERO;

    @Column(name = "joined_date")
    private LocalDate joinedDate;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
