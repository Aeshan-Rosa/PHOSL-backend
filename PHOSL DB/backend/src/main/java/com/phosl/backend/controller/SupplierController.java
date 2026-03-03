package com.phosl.backend.controller;

import com.phosl.backend.exception.ResourceNotFoundException;
import com.phosl.backend.model.Supplier;
import com.phosl.backend.repository.SupplierRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {
    private final SupplierRepository supplierRepository;

    public SupplierController(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @GetMapping
    public List<Supplier> list() { return supplierRepository.findAll(); }

    @GetMapping("/{id}")
    public Supplier get(@PathVariable Long id) {
        return supplierRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
    }

    @PostMapping
    public Supplier create(@RequestBody Supplier supplier) {
        supplier.setId(null);
        return supplierRepository.save(supplier);
    }

    @PutMapping("/{id}")
    public Supplier update(@PathVariable Long id, @RequestBody Supplier req) {
        Supplier supplier = get(id);
        supplier.setName(req.getName());
        supplier.setPhone(req.getPhone());
        supplier.setEmail(req.getEmail());
        supplier.setAddress(req.getAddress());
        supplier.setCountry(req.getCountry());
        supplier.setReliabilityRating(req.getReliabilityRating());
        return supplierRepository.save(supplier);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        supplierRepository.deleteById(id);
        return Map.of("message", "Supplier deleted");
    }
}
