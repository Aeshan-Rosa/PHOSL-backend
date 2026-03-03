package com.phosl.backend.controller;

import com.phosl.backend.exception.ResourceNotFoundException;
import com.phosl.backend.model.Customer;
import com.phosl.backend.model.Sale;
import com.phosl.backend.repository.CustomerRepository;
import com.phosl.backend.repository.SaleRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;

    public CustomerController(CustomerRepository customerRepository, SaleRepository saleRepository) {
        this.customerRepository = customerRepository;
        this.saleRepository = saleRepository;
    }

    @GetMapping
    public List<Customer> list(@RequestParam(required = false) String q) {
        if (q != null && !q.isBlank()) {
            return customerRepository.findByFullNameContainingIgnoreCaseOrPhoneContainingIgnoreCase(q, q);
        }
        return customerRepository.findAll();
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        List<Sale> history = saleRepository.findByCustomerId(id);
        Map<String, Object> res = new HashMap<>();
        res.put("customer", customer);
        res.put("salesHistory", history);
        return res;
    }

    @PostMapping
    public Customer create(@RequestBody Customer customer) {
        customer.setId(null);
        return customerRepository.save(customer);
    }

    @PutMapping("/{id}")
    public Customer update(@PathVariable Long id, @RequestBody Customer req) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        customer.setFullName(req.getFullName());
        customer.setPhone(req.getPhone());
        customer.setEmail(req.getEmail());
        customer.setAddress(req.getAddress());
        customer.setHowFound(req.getHowFound());
        return customerRepository.save(customer);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        customerRepository.deleteById(id);
        return Map.of("message", "Customer deleted");
    }
}
