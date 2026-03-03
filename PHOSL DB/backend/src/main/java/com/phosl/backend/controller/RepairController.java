package com.phosl.backend.controller;

import com.phosl.backend.exception.ResourceNotFoundException;
import com.phosl.backend.model.PianoRepair;
import com.phosl.backend.repository.PianoRepairRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/repairs")
public class RepairController {

    private final PianoRepairRepository pianoRepairRepository;

    public RepairController(PianoRepairRepository pianoRepairRepository) {
        this.pianoRepairRepository = pianoRepairRepository;
    }

    @GetMapping
    public List<PianoRepair> list(@RequestParam(required = false) String status,
                                  @RequestParam(required = false) Long pianoId) {
        if (status != null) return pianoRepairRepository.findByStatus(status);
        if (pianoId != null) return pianoRepairRepository.findByPianoId(pianoId);
        return pianoRepairRepository.findAll();
    }

    @GetMapping("/{id}")
    public PianoRepair get(@PathVariable Long id) {
        return pianoRepairRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Repair not found"));
    }

    @PostMapping
    public PianoRepair create(@RequestBody PianoRepair repair) {
        repair.setId(null);
        return pianoRepairRepository.save(repair);
    }

    @PutMapping("/{id}")
    public PianoRepair update(@PathVariable Long id, @RequestBody PianoRepair req) {
        PianoRepair repair = get(id);
        repair.setIssue(req.getIssue());
        repair.setRepairCost(req.getRepairCost());
        repair.setTechnicianId(req.getTechnicianId());
        repair.setStatus(req.getStatus());
        repair.setClosedDate(req.getClosedDate());
        return pianoRepairRepository.save(repair);
    }

    @PutMapping("/{id}/close")
    public PianoRepair close(@PathVariable Long id) {
        PianoRepair repair = get(id);
        repair.setStatus("DONE");
        repair.setClosedDate(LocalDate.now());
        return pianoRepairRepository.save(repair);
    }

    @PutMapping("/{id}/status")
    public PianoRepair updateStatus(@PathVariable Long id, @RequestParam String status) {
        PianoRepair repair = get(id);
        repair.setStatus(status);
        return pianoRepairRepository.save(repair);
    }
}
