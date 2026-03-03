package com.phosl.backend.controller;

import com.phosl.backend.model.*;
import com.phosl.backend.repository.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AnalyticsController {

    private final PianoRepository pianoRepository;
    private final SaleRepository saleRepository;
    private final SalePaymentRepository salePaymentRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final InstallmentScheduleRepository installmentScheduleRepository;
    private final WorkerCommissionRepository workerCommissionRepository;

    public AnalyticsController(PianoRepository pianoRepository,
                               SaleRepository saleRepository,
                               SalePaymentRepository salePaymentRepository,
                               PurchaseItemRepository purchaseItemRepository,
                               InstallmentScheduleRepository installmentScheduleRepository,
                               WorkerCommissionRepository workerCommissionRepository) {
        this.pianoRepository = pianoRepository;
        this.saleRepository = saleRepository;
        this.salePaymentRepository = salePaymentRepository;
        this.purchaseItemRepository = purchaseItemRepository;
        this.installmentScheduleRepository = installmentScheduleRepository;
        this.workerCommissionRepository = workerCommissionRepository;
    }

    @GetMapping("/dashboard/overview")
    public Map<String, Object> dashboardOverview() {
        List<Piano> pianos = pianoRepository.findAll();
        List<Sale> sales = saleRepository.findAll();

        BigDecimal stockValue = pianos.stream().filter(p -> "IN_STOCK".equalsIgnoreCase(p.getStatus()))
                .map(Piano::getExpectedSellingPrice).filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal salesTotal = sales.stream().map(Sale::getTotal).filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal costTotal = purchaseItemRepository.findAll().stream().map(PurchaseItem::getLandedCost)
                .filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal paid = salePaymentRepository.findAll().stream().map(SalePayment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pendingPayments = salesTotal.subtract(paid);

        return Map.of(
                "stockValue", stockValue,
                "salesTotal", salesTotal,
                "profitEstimate", salesTotal.subtract(costTotal),
                "pendingPayments", pendingPayments
        );
    }

    @GetMapping("/dashboard/sales")
    public Map<String, BigDecimal> dashboardSales(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        return saleRepository.findBySoldDateBetween(from, to).stream()
                .collect(Collectors.groupingBy(s -> s.getSoldDate().toString(),
                        Collectors.mapping(Sale::getTotal,
                                Collectors.reducing(BigDecimal.ZERO, v -> v == null ? BigDecimal.ZERO : v, BigDecimal::add))));
    }

    @GetMapping("/dashboard/inventory")
    public Map<String, Object> dashboardInventory() {
        List<Piano> pianos = pianoRepository.findAll();
        return Map.of(
                "byStatus", pianos.stream().collect(Collectors.groupingBy(Piano::getStatus, Collectors.counting())),
                "byBrand", pianos.stream().collect(Collectors.groupingBy(Piano::getBrand, Collectors.counting())),
                "byType", pianos.stream().collect(Collectors.groupingBy(Piano::getPianoType, Collectors.counting()))
        );
    }

    @GetMapping("/dashboard/worker-performance")
    public Map<String, Object> workerPerformance(@RequestParam int month, @RequestParam int year) {
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());
        List<Sale> sales = saleRepository.findBySoldDateBetween(from, to);

        Map<Long, BigDecimal> salesPerWorker = sales.stream()
                .filter(s -> s.getSalespersonId() != null)
                .collect(Collectors.groupingBy(Sale::getSalespersonId,
                        Collectors.mapping(Sale::getTotal,
                                Collectors.reducing(BigDecimal.ZERO, v -> v == null ? BigDecimal.ZERO : v, BigDecimal::add))));

        BigDecimal commissions = workerCommissionRepository.findAll().stream()
                .filter(c -> c.getCommissionAmount() != null)
                .map(WorkerCommission::getCommissionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of("salesPerWorker", salesPerWorker, "totalCommissions", commissions);
    }

    @GetMapping("/reports/sales-monthly")
    public List<Sale> salesMonthly(@RequestParam int month, @RequestParam int year) {
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());
        return saleRepository.findBySoldDateBetween(from, to);
    }

    @GetMapping("/reports/profit")
    public Map<String, BigDecimal> profit(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        BigDecimal sales = saleRepository.findBySoldDateBetween(from, to).stream()
                .map(Sale::getTotal).filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal costs = purchaseItemRepository.findAll().stream()
                .map(PurchaseItem::getLandedCost).filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        return Map.of("sales", sales, "costs", costs, "profit", sales.subtract(costs));
    }

    @GetMapping("/reports/stock-aging")
    public List<Map<String, Object>> stockAging(@RequestParam(defaultValue = "90") int days) {
        LocalDate threshold = LocalDate.now().minusDays(days);
        return pianoRepository.findAll().stream()
                .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().toLocalDate().isBefore(threshold))
                .map(p -> Map.<String, Object>of("pianoId", p.getId(), "brand", p.getBrand(), "createdAt", p.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @GetMapping("/reports/installments-overdue")
    public List<InstallmentSchedule> installmentsOverdue() {
        return installmentScheduleRepository.findByDueDateBeforeAndStatusIn(LocalDate.now(), List.of("PENDING", "PARTIAL"));
    }

    @GetMapping("/reports/commissions")
    public List<WorkerCommission> commissions(@RequestParam int month, @RequestParam int year) {
        return workerCommissionRepository.findAll();
    }

    @GetMapping("/reports/customers-followup")
    public List<Sale> customersFollowup(@RequestParam(defaultValue = "365") int daysSinceSale) {
        LocalDate before = LocalDate.now().minusDays(daysSinceSale);
        return saleRepository.findAll().stream()
                .filter(s -> s.getSoldDate() != null && s.getSoldDate().isBefore(before))
                .collect(Collectors.toList());
    }
}
