package com.phosl.backend.model;

import javax.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "work_tasks")
@Data
public class WorkTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_type")
    private String taskType = "OTHER";

    @Column(name = "piano_id")
    private Long pianoId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "assigned_worker_id")
    private Long assignedWorkerId;

    @Column(name = "due_date")
    private LocalDate dueDate;

    private String status = "TODO";
}
