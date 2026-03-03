package com.phosl.backend.controller;

import com.phosl.backend.exception.ResourceNotFoundException;
import com.phosl.backend.model.Purchase;
import com.phosl.backend.model.PurchaseItem;
import com.phosl.backend.model.PurchasePayment;
import com.phosl.backend.repository.PurchaseItemRepository;
import com.phosl.backend.repository.PurchasePaymentRepository;
import com.phosl.backend.repository.PurchaseRepository;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final PurchasePaymentRepository purchasePaymentRepository;

    public PurchaseController(PurchaseRepository purchaseRepository,
                              PurchaseItemRepository purchaseItemRepository,
                              PurchasePaymentRepository purchasePaymentRepository) {
        this.purchaseRepository = purchaseRepository;
        this.purchaseItemRepository = purchaseItemRepository;
        this.purchasePaymentRepository = purchasePaymentRepository;
    }

    @GetMapping
    public List<Purchase> list(@RequestParam(required = false) Long supplierId,
                               @RequestParam(required = false) String paymentStatus,
                               @RequestParam(required = false) LocalDate from,
                               @RequestParam(required = false) LocalDate to) {
        if (supplierId != null) return purchaseRepository.findBySupplierId(supplierId);
        if (paymentStatus != null) return purchaseRepository.findByPaymentStatus(paymentStatus);
        if (from != null && to != null) return purchaseRepository.findByPurchaseDateBetween(from, to);
        return purchaseRepository.findAll();
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        Purchase purchase = purchaseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));
        Map<String, Object> res = new HashMap<>();
        res.put("purchase", purchase);
        res.put("items", purchaseItemRepository.findByPurchaseId(id));
        res.put("payments", purchasePaymentRepository.findByPurchaseId(id));
        return res;
    }

    @PostMapping
    public Purchase create(@RequestBody Purchase purchase) {
        purchase.setId(null);
        return purchaseRepository.save(purchase);
    }

    @PutMapping("/{id}")
    public Purchase update(@PathVariable Long id, @RequestBody Purchase req) {
        Purchase purchase = purchaseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));
        purchase.setSupplierId(req.getSupplierId());
        purchase.setPurchaseDate(req.getPurchaseDate());
        purchase.setPaymentStatus(req.getPaymentStatus());
        return purchaseRepository.save(purchase);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        purchaseRepository.deleteById(id);
        return Map.of("message", "Purchase deleted");
    }

    @PostMapping("/{id}/items")
    public PurchaseItem addItem(@PathVariable Long id, @RequestBody PurchaseItem item) {
        purchaseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));
        item.setId(null);
        item.setPurchaseId(id);
        return purchaseItemRepository.save(item);
    }

    @PutMapping("/{id}/items/{itemId}")
    public PurchaseItem updateItem(@PathVariable Long id, @PathVariable Long itemId, @RequestBody PurchaseItem req) {
        purchaseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));
        PurchaseItem item = purchaseItemRepository.findById(itemId).orElseThrow(() -> new ResourceNotFoundException("Purchase item not found"));
        item.setBuyPrice(req.getBuyPrice());
        item.setShippingCost(req.getShippingCost());
        item.setRepairCost(req.getRepairCost());
        item.setOtherCost(req.getOtherCost());
        item.setLandedCost(req.getLandedCost());
        return purchaseItemRepository.save(item);
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public Map<String, String> deleteItem(@PathVariable Long id, @PathVariable Long itemId) {
        purchaseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));
        purchaseItemRepository.deleteById(itemId);
        return Map.of("message", "Purchase item removed");
    }

    @GetMapping("/{id}/payments")
    public List<PurchasePayment> payments(@PathVariable Long id) {
        purchaseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));
        return purchasePaymentRepository.findByPurchaseId(id);
    }

    @PostMapping("/{id}/payments")
    public PurchasePayment addPayment(@PathVariable Long id, @RequestBody PurchasePayment payment) {
        purchaseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));
        payment.setId(null);
        payment.setPurchaseId(id);
        return purchasePaymentRepository.save(payment);
    }

    @DeleteMapping("/{id}/payments/{payId}")
    public Map<String, String> deletePayment(@PathVariable Long id, @PathVariable Long payId) {
        purchaseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));
        purchasePaymentRepository.deleteById(payId);
        return Map.of("message", "Payment removed");
    }

    @PutMapping("/{id}/payment-status")
    public Purchase updatePaymentStatus(@PathVariable Long id, @RequestParam(required = false) String status) {
        Purchase purchase = purchaseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));
        if (status != null) {
            purchase.setPaymentStatus(status);
            return purchaseRepository.save(purchase);
        }

        BigDecimal totalLanded = purchaseItemRepository.findByPurchaseId(id).stream()
                .map(PurchaseItem::getLandedCost)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPaid = purchasePaymentRepository.findByPurchaseId(id).stream()
                .map(PurchasePayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPaid.compareTo(BigDecimal.ZERO) == 0) purchase.setPaymentStatus("PENDING");
        else if (totalPaid.compareTo(totalLanded) >= 0) purchase.setPaymentStatus("PAID");
        else purchase.setPaymentStatus("PARTIAL");

        return purchaseRepository.save(purchase);
    }
}
