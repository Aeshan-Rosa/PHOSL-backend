package com.phosl.backend.controller;

import com.phosl.backend.exception.ResourceNotFoundException;
import com.phosl.backend.model.*;
import com.phosl.backend.repository.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/commissions")
public class CommissionController {

    private final WorkerCommissionRepository workerCommissionRepository;
    private final RecommenderCommissionRepository recommenderCommissionRepository;
    private final SaleRepository saleRepository;
    private final WorkerRepository workerRepository;

    public CommissionController(WorkerCommissionRepository workerCommissionRepository,
                                RecommenderCommissionRepository recommenderCommissionRepository,
                                SaleRepository saleRepository,
                                WorkerRepository workerRepository) {
        this.workerCommissionRepository = workerCommissionRepository;
        this.recommenderCommissionRepository = recommenderCommissionRepository;
        this.saleRepository = saleRepository;
        this.workerRepository = workerRepository;
    }

    @GetMapping("/workers")
    public List<WorkerCommission> workerCommissions(@RequestParam(defaultValue = "PENDING") String status) {
        return workerCommissionRepository.findByStatus(status);
    }

    @PostMapping("/workers/calculate")
    public WorkerCommission calculateWorker(@RequestParam Long saleId) {
        Sale sale = saleRepository.findById(saleId).orElseThrow(() -> new ResourceNotFoundException("Sale not found"));
        if (sale.getSalespersonId() == null) throw new ResourceNotFoundException("Sale has no salesperson");

        Worker worker = workerRepository.findById(sale.getSalespersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found"));
        BigDecimal rate = worker.getCommissionRate() == null ? BigDecimal.ZERO : worker.getCommissionRate();
        BigDecimal total = sale.getTotal() == null ? BigDecimal.ZERO : sale.getTotal();
        BigDecimal amount = total.multiply(rate).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);

        WorkerCommission commission = workerCommissionRepository.findBySaleId(saleId).orElseGet(WorkerCommission::new);
        commission.setSaleId(saleId);
        commission.setWorkerId(worker.getId());
        commission.setCommissionRate(rate);
        commission.setCommissionAmount(amount);
        commission.setStatus("PENDING");
        return workerCommissionRepository.save(commission);
    }

    @PutMapping("/workers/{id}/mark-paid")
    public WorkerCommission markWorkerPaid(@PathVariable Long id) {
        WorkerCommission commission = workerCommissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Worker commission not found"));
        commission.setStatus("PAID");
        return workerCommissionRepository.save(commission);
    }

    @GetMapping("/recommenders")
    public List<RecommenderCommission> recommenderCommissions(@RequestParam(defaultValue = "PENDING") String status) {
        return recommenderCommissionRepository.findByStatus(status);
    }

    @PostMapping("/recommenders/calculate")
    public RecommenderCommission calculateRecommender(@RequestParam Long saleId,
                                                      @RequestParam(defaultValue = "5") BigDecimal rate) {
        Sale sale = saleRepository.findById(saleId).orElseThrow(() -> new ResourceNotFoundException("Sale not found"));
        if (sale.getRecommenderId() == null) throw new ResourceNotFoundException("Sale has no recommender");

        BigDecimal total = sale.getTotal() == null ? BigDecimal.ZERO : sale.getTotal();
        BigDecimal amount = total.multiply(rate).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);

        RecommenderCommission commission = recommenderCommissionRepository.findBySaleId(saleId)
                .orElseGet(RecommenderCommission::new);
        commission.setSaleId(saleId);
        commission.setRecommenderId(sale.getRecommenderId());
        commission.setCommissionRate(rate);
        commission.setCommissionAmount(amount);
        commission.setStatus("PENDING");
        return recommenderCommissionRepository.save(commission);
    }

    @PutMapping("/recommenders/{id}/mark-paid")
    public RecommenderCommission markRecommenderPaid(@PathVariable Long id) {
        RecommenderCommission commission = recommenderCommissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recommender commission not found"));
        commission.setStatus("PAID");
        return recommenderCommissionRepository.save(commission);
    }
}
