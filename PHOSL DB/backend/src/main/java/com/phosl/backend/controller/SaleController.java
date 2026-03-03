package com.phosl.backend.controller;

import com.phosl.backend.exception.ResourceNotFoundException;
import com.phosl.backend.model.*;
import com.phosl.backend.repository.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final SalePaymentRepository salePaymentRepository;
    private final InstallmentPlanRepository installmentPlanRepository;
    private final PianoRepository pianoRepository;

    public SaleController(SaleRepository saleRepository,
                          SaleItemRepository saleItemRepository,
                          SalePaymentRepository salePaymentRepository,
                          InstallmentPlanRepository installmentPlanRepository,
                          PianoRepository pianoRepository) {
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.salePaymentRepository = salePaymentRepository;
        this.installmentPlanRepository = installmentPlanRepository;
        this.pianoRepository = pianoRepository;
    }

    @GetMapping
    public List<Sale> list(@RequestParam(required = false) LocalDate from,
                           @RequestParam(required = false) LocalDate to,
                           @RequestParam(required = false) Long customerId,
                           @RequestParam(required = false) String paymentPlan,
                           @RequestParam(required = false) Long salespersonId) {
        if (from != null && to != null) return saleRepository.findBySoldDateBetween(from, to);
        if (customerId != null) return saleRepository.findByCustomerId(customerId);
        if (paymentPlan != null) return saleRepository.findByPaymentPlan(paymentPlan);
        if (salespersonId != null) return saleRepository.findBySalespersonId(salespersonId);
        return saleRepository.findAll();
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        Sale sale = saleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sale not found"));
        Map<String, Object> res = new HashMap<>();
        res.put("sale", sale);
        res.put("items", saleItemRepository.findBySaleId(id));
        res.put("payments", salePaymentRepository.findBySaleId(id));
        res.put("installmentPlan", installmentPlanRepository.findBySaleId(id).orElse(null));
        return res;
    }

    @PostMapping
    public Sale create(@RequestBody Sale sale) {
        sale.setId(null);
        return saleRepository.save(sale);
    }

    @PutMapping("/{id}")
    public Sale update(@PathVariable Long id, @RequestBody Sale req) {
        Sale sale = saleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sale not found"));
        sale.setDiscount(req.getDiscount());
        sale.setWarrantyMonths(req.getWarrantyMonths());
        sale.setDeliveryDate(req.getDeliveryDate());
        sale.setPaymentPlan(req.getPaymentPlan());
        sale.setSubtotal(req.getSubtotal());
        sale.setTotal(req.getTotal());
        sale.setSalespersonId(req.getSalespersonId());
        sale.setRecommenderId(req.getRecommenderId());
        return saleRepository.save(sale);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        saleRepository.deleteById(id);
        return Map.of("message", "Sale deleted");
    }

    @PostMapping("/{id}/items")
    public SaleItem addItem(@PathVariable Long id, @RequestBody SaleItem item) {
        saleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sale not found"));
        item.setId(null);
        item.setSaleId(id);
        SaleItem saved = saleItemRepository.save(item);

        pianoRepository.findById(item.getPianoId()).ifPresent(p -> {
            p.setStatus("SOLD");
            pianoRepository.save(p);
        });

        recalcSaleTotal(id);
        return saved;
    }

    @PutMapping("/{id}/items/{itemId}")
    public SaleItem updateItem(@PathVariable Long id, @PathVariable Long itemId, @RequestBody SaleItem req) {
        saleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sale not found"));
        SaleItem item = saleItemRepository.findById(itemId).orElseThrow(() -> new ResourceNotFoundException("Sale item not found"));
        item.setUnitPrice(req.getUnitPrice());
        item.setLineDiscount(req.getLineDiscount());
        item.setLineTotal(req.getLineTotal());
        SaleItem saved = saleItemRepository.save(item);
        recalcSaleTotal(id);
        return saved;
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public Map<String, String> deleteItem(@PathVariable Long id, @PathVariable Long itemId) {
        saleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sale not found"));
        saleItemRepository.deleteById(itemId);
        recalcSaleTotal(id);
        return Map.of("message", "Sale item removed");
    }

    @GetMapping("/{id}/payments")
    public List<SalePayment> payments(@PathVariable Long id) {
        saleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sale not found"));
        return salePaymentRepository.findBySaleId(id);
    }

    @PostMapping("/{id}/payments")
    public SalePayment addPayment(@PathVariable Long id, @RequestBody SalePayment payment) {
        saleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sale not found"));
        payment.setId(null);
        payment.setSaleId(id);
        return salePaymentRepository.save(payment);
    }

    @DeleteMapping("/{id}/payments/{payId}")
    public Map<String, String> deletePayment(@PathVariable Long id, @PathVariable Long payId) {
        saleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sale not found"));
        salePaymentRepository.deleteById(payId);
        return Map.of("message", "Payment removed");
    }

    private void recalcSaleTotal(Long saleId) {
        Sale sale = saleRepository.findById(saleId).orElseThrow(() -> new ResourceNotFoundException("Sale not found"));
        BigDecimal subtotal = saleItemRepository.findBySaleId(saleId).stream()
                .map(SaleItem::getLineTotal)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = sale.getDiscount() == null ? BigDecimal.ZERO : sale.getDiscount();
        sale.setSubtotal(subtotal);
        sale.setTotal(subtotal.subtract(discount));
        saleRepository.save(sale);
    }
}
