package com.phosl.backend.model;

import javax.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "piano_repairs")
@Data
public class PianoRepair {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "piano_id", nullable = false)
    private Long pianoId;

    @Column(name = "opened_date", nullable = false)
    private LocalDate openedDate;

    @Column(name = "closed_date")
    private LocalDate closedDate;

    private String issue;

    @Column(name = "repair_cost")
    private BigDecimal repairCost;

    @Column(name = "technician_id")
    private Long technicianId;

    private String status = "OPEN";
}
