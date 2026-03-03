package com.phosl.backend.controller;

import com.phosl.backend.exception.ResourceNotFoundException;
import com.phosl.backend.model.Recommender;
import com.phosl.backend.repository.RecommenderCommissionRepository;
import com.phosl.backend.repository.RecommenderRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommenders")
public class RecommenderController {

    private final RecommenderRepository recommenderRepository;
    private final RecommenderCommissionRepository recommenderCommissionRepository;

    public RecommenderController(RecommenderRepository recommenderRepository,
                                 RecommenderCommissionRepository recommenderCommissionRepository) {
        this.recommenderRepository = recommenderRepository;
        this.recommenderCommissionRepository = recommenderCommissionRepository;
    }

    @GetMapping
    public List<Recommender> list() { return recommenderRepository.findAll(); }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        Recommender recommender = recommenderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recommender not found"));
        Map<String, Object> res = new HashMap<>();
        res.put("recommender", recommender);
        res.put("commissionHistory", recommenderCommissionRepository.findByRecommenderId(id));
        return res;
    }

    @PostMapping
    public Recommender create(@RequestBody Recommender recommender) {
        recommender.setId(null);
        return recommenderRepository.save(recommender);
    }

    @PutMapping("/{id}")
    public Recommender update(@PathVariable Long id, @RequestBody Recommender req) {
        Recommender recommender = recommenderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recommender not found"));
        recommender.setName(req.getName());
        recommender.setPhone(req.getPhone());
        recommender.setEmail(req.getEmail());
        return recommenderRepository.save(recommender);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        recommenderRepository.deleteById(id);
        return Map.of("message", "Recommender deleted");
    }
}
