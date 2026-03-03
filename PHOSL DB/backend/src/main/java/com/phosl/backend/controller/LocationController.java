package com.phosl.backend.controller;

import com.phosl.backend.exception.ResourceNotFoundException;
import com.phosl.backend.model.Location;
import com.phosl.backend.repository.LocationRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {
    private final LocationRepository locationRepository;

    public LocationController(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @GetMapping
    public List<Location> list() { return locationRepository.findAll(); }

    @GetMapping("/{id}")
    public Location get(@PathVariable Long id) {
        return locationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Location not found"));
    }

    @PostMapping
    public Location create(@RequestBody Location location) {
        location.setId(null);
        return locationRepository.save(location);
    }

    @PutMapping("/{id}")
    public Location update(@PathVariable Long id, @RequestBody Location req) {
        Location location = get(id);
        location.setName(req.getName());
        location.setAddress(req.getAddress());
        location.setIsActive(req.getIsActive());
        return locationRepository.save(location);
    }

    @PutMapping("/{id}/disable")
    public Location disable(@PathVariable Long id) {
        Location location = get(id);
        location.setIsActive(false);
        return locationRepository.save(location);
    }

    @PutMapping("/{id}/enable")
    public Location enable(@PathVariable Long id) {
        Location location = get(id);
        location.setIsActive(true);
        return locationRepository.save(location);
    }
}
