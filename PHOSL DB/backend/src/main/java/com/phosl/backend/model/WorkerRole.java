package com.phosl.backend.model;

import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "worker_roles")
@Data
public class WorkerRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_name", nullable = false, unique = true)
    private String roleName;
}
