package com.phosl.backend.controller;

import com.phosl.backend.exception.ResourceNotFoundException;
import com.phosl.backend.model.Worker;
import com.phosl.backend.model.WorkerPayout;
import com.phosl.backend.repository.WorkerPayoutRepository;
import com.phosl.backend.repository.WorkerRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class WorkerController {

    private final WorkerRepository workerRepository;
    private final WorkerPayoutRepository workerPayoutRepository;

    public WorkerController(WorkerRepository workerRepository, WorkerPayoutRepository workerPayoutRepository) {
        this.workerRepository = workerRepository;
        this.workerPayoutRepository = workerPayoutRepository;
    }

    @GetMapping("/api/workers")
    public List<Worker> listWorkers() { return workerRepository.findAll(); }

    @GetMapping("/api/workers/{id}")
    public Worker getWorker(@PathVariable Long id) {
        return workerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Worker not found"));
    }

    @PostMapping("/api/workers")
    public Worker createWorker(@RequestBody Worker worker) {
        worker.setId(null);
        return workerRepository.save(worker);
    }

    @PutMapping("/api/workers/{id}")
    public Worker updateWorker(@PathVariable Long id, @RequestBody Worker req) {
        Worker worker = getWorker(id);
        worker.setEmployeeCode(req.getEmployeeCode());
        worker.setFullName(req.getFullName());
        worker.setPhone(req.getPhone());
        worker.setEmail(req.getEmail());
        worker.setRoleId(req.getRoleId());
        worker.setBaseSalary(req.getBaseSalary());
        worker.setCommissionRate(req.getCommissionRate());
        worker.setJoinedDate(req.getJoinedDate());
        return workerRepository.save(worker);
    }

    @PutMapping("/api/workers/{id}/disable")
    public Worker disableWorker(@PathVariable Long id) {
        Worker worker = getWorker(id);
        worker.setIsActive(false);
        return workerRepository.save(worker);
    }

    @PutMapping("/api/workers/{id}/enable")
    public Worker enableWorker(@PathVariable Long id) {
        Worker worker = getWorker(id);
        worker.setIsActive(true);
        return workerRepository.save(worker);
    }

    @GetMapping("/api/worker-payouts")
    public List<WorkerPayout> listPayouts(@RequestParam Integer year, @RequestParam Integer month) {
        return workerPayoutRepository.findByPeriodYearAndPeriodMonth(year, month);
    }

    @PostMapping("/api/worker-payouts")
    public WorkerPayout addPayout(@RequestBody WorkerPayout payout) {
        payout.setId(null);
        return workerPayoutRepository.save(payout);
    }

    @GetMapping("/api/workers/{id}/payouts")
    public List<WorkerPayout> workerPayoutHistory(@PathVariable Long id) {
        return workerPayoutRepository.findByWorkerId(id);
    }
}
