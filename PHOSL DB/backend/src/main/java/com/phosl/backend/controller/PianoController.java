package com.phosl.backend.controller;

import com.phosl.backend.exception.ResourceNotFoundException;
import com.phosl.backend.model.Piano;
import com.phosl.backend.repository.PianoRepository;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class PianoController {

    private final PianoRepository pianoRepository;

    public PianoController(PianoRepository pianoRepository) {
        this.pianoRepository = pianoRepository;
    }

    @GetMapping("/api/pianos")
    public List<Piano> list(@RequestParam(required = false) String status,
                            @RequestParam(required = false) String brand,
                            @RequestParam(required = false) BigDecimal minPrice,
                            @RequestParam(required = false) BigDecimal maxPrice,
                            @RequestParam(required = false) Long locationId,
                            @RequestParam(required = false, name = "type") String pianoType) {
        return pianoRepository.findAll().stream()
                .filter(p -> status == null || status.equalsIgnoreCase(p.getStatus()))
                .filter(p -> brand == null || (p.getBrand() != null && p.getBrand().toLowerCase().contains(brand.toLowerCase())))
                .filter(p -> minPrice == null || (p.getExpectedSellingPrice() != null && p.getExpectedSellingPrice().compareTo(minPrice) >= 0))
                .filter(p -> maxPrice == null || (p.getExpectedSellingPrice() != null && p.getExpectedSellingPrice().compareTo(maxPrice) <= 0))
                .filter(p -> locationId == null || locationId.equals(p.getLocationId()))
                .filter(p -> pianoType == null || (p.getPianoType() != null && p.getPianoType().equalsIgnoreCase(pianoType)))
                .collect(Collectors.toList());
    }

    @GetMapping("/api/pianos/{id}")
    public Piano get(@PathVariable Long id) {
        return pianoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Piano not found"));
    }

    @PostMapping("/api/pianos")
    public Piano create(@RequestBody Piano piano) {
        piano.setId(null);
        return pianoRepository.save(piano);
    }

    @PutMapping("/api/pianos/{id}")
    public Piano update(@PathVariable Long id, @RequestBody Piano req) {
        Piano piano = get(id);
        piano.setPianoCode(req.getPianoCode());
        piano.setBrand(req.getBrand());
        piano.setModel(req.getModel());
        piano.setSerialNumber(req.getSerialNumber());
        piano.setPianoType(req.getPianoType());
        piano.setColor(req.getColor());
        piano.setConditionType(req.getConditionType());
        piano.setManufactureYear(req.getManufactureYear());
        piano.setStatus(req.getStatus());
        piano.setLocationId(req.getLocationId());
        piano.setExpectedSellingPrice(req.getExpectedSellingPrice());
        return pianoRepository.save(piano);
    }

    @PutMapping("/api/pianos/{id}/status")
    public Piano updateStatus(@PathVariable Long id, @RequestParam String status) {
        Piano piano = get(id);
        piano.setStatus(status);
        return pianoRepository.save(piano);
    }

    @PutMapping("/api/pianos/{id}/transfer")
    public Piano transfer(@PathVariable Long id, @RequestParam Long locationId) {
        Piano piano = get(id);
        piano.setLocationId(locationId);
        return pianoRepository.save(piano);
    }

    @DeleteMapping("/api/pianos/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        pianoRepository.deleteById(id);
        return Map.of("message", "Piano deleted");
    }

    @GetMapping("/api/inventory/summary")
    public Map<String, Object> summary() {
        List<Piano> pianos = pianoRepository.findAll();
        BigDecimal totalExpectedValue = pianos.stream()
                .map(Piano::getExpectedSellingPrice)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Long> byStatus = pianos.stream()
                .collect(Collectors.groupingBy(Piano::getStatus, Collectors.counting()));

        return Map.of(
                "totalCount", pianos.size(),
                "totalExpectedValue", totalExpectedValue,
                "countByStatus", byStatus
        );
    }

    @GetMapping("/api/inventory/aging")
    public List<Map<String, Object>> aging() {
        LocalDateTime now = LocalDateTime.now();
        return pianoRepository.findAll().stream()
                .filter(p -> "IN_STOCK".equalsIgnoreCase(p.getStatus()))
                .map(p -> Map.<String, Object>of(
                        "pianoId", p.getId(),
                        "brand", p.getBrand(),
                        "model", p.getModel() == null ? "" : p.getModel(),
                        "daysInStock", p.getCreatedAt() == null ? 0 : ChronoUnit.DAYS.between(p.getCreatedAt(), now)
                ))
                .sorted(Comparator.<Map<String, Object>>comparingLong(m -> ((Number) m.get("daysInStock")).longValue())
                        .reversed())
                .collect(Collectors.toList());
    }
}
