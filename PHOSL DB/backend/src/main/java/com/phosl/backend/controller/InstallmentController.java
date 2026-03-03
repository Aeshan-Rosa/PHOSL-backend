package com.phosl.backend.controller;

import com.phosl.backend.exception.ResourceNotFoundException;
import com.phosl.backend.model.InstallmentPlan;
import com.phosl.backend.model.InstallmentSchedule;
import com.phosl.backend.model.SalePayment;
import com.phosl.backend.repository.InstallmentPlanRepository;
import com.phosl.backend.repository.InstallmentScheduleRepository;
import com.phosl.backend.repository.SalePaymentRepository;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/installments")
public class InstallmentController {

    private final InstallmentPlanRepository planRepository;
    private final InstallmentScheduleRepository scheduleRepository;
    private final SalePaymentRepository salePaymentRepository;

    public InstallmentController(InstallmentPlanRepository planRepository,
                                 InstallmentScheduleRepository scheduleRepository,
                                 SalePaymentRepository salePaymentRepository) {
        this.planRepository = planRepository;
        this.scheduleRepository = scheduleRepository;
        this.salePaymentRepository = salePaymentRepository;
    }

    @PostMapping("/plans")
    public InstallmentPlan createPlan(@RequestBody InstallmentPlan plan) {
        plan.setId(null);
        return planRepository.save(plan);
    }

    @GetMapping("/plans/{planId}")
    public Map<String, Object> getPlan(@PathVariable Long planId) {
        InstallmentPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));
        Map<String, Object> res = new HashMap<>();
        res.put("plan", plan);
        res.put("schedule", scheduleRepository.findByPlanId(planId));
        return res;
    }

    @PutMapping("/plans/{planId}")
    public InstallmentPlan updatePlan(@PathVariable Long planId, @RequestBody InstallmentPlan req) {
        InstallmentPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));
        plan.setMonths(req.getMonths());
        plan.setDueDayOfMonth(req.getDueDayOfMonth());
        plan.setStatus(req.getStatus());
        return planRepository.save(plan);
    }

    @PostMapping("/plans/{planId}/schedule/generate")
    public List<InstallmentSchedule> generate(@PathVariable Long planId) {
        InstallmentPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

        scheduleRepository.findByPlanId(planId).forEach(s -> scheduleRepository.deleteById(s.getId()));

        BigDecimal monthly = plan.getRemainingBalance().divide(BigDecimal.valueOf(plan.getMonths()), 2, java.math.RoundingMode.HALF_UP);
        LocalDate due = plan.getStartDate();

        for (int i = 1; i <= plan.getMonths(); i++) {
            InstallmentSchedule s = new InstallmentSchedule();
            s.setPlanId(planId);
            s.setInstallmentNo(i);
            s.setDueDate(due.plusMonths(i - 1));
            s.setAmount(monthly);
            s.setPaidAmount(BigDecimal.ZERO);
            s.setStatus("PENDING");
            scheduleRepository.save(s);
        }
        return scheduleRepository.findByPlanId(planId);
    }

    @PutMapping("/plans/{planId}/schedule/{scheduleId}")
    public InstallmentSchedule updateSchedule(@PathVariable Long planId,
                                              @PathVariable Long scheduleId,
                                              @RequestBody InstallmentSchedule req) {
        planRepository.findById(planId).orElseThrow(() -> new ResourceNotFoundException("Plan not found"));
        InstallmentSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));
        schedule.setDueDate(req.getDueDate());
        schedule.setAmount(req.getAmount());
        schedule.setStatus(req.getStatus());
        return scheduleRepository.save(schedule);
    }

    @PostMapping("/plans/{planId}/pay")
    public InstallmentSchedule pay(@PathVariable Long planId,
                                   @RequestParam Long scheduleId,
                                   @RequestParam BigDecimal amount,
                                   @RequestParam(defaultValue = "Cash") String method) {
        InstallmentPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));
        InstallmentSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));

        BigDecimal paid = schedule.getPaidAmount().add(amount);
        schedule.setPaidAmount(paid);
        if (paid.compareTo(schedule.getAmount()) >= 0) schedule.setStatus("PAID");
        else schedule.setStatus("PARTIAL");
        scheduleRepository.save(schedule);

        SalePayment payment = new SalePayment();
        payment.setSaleId(plan.getSaleId());
        payment.setPayDate(LocalDate.now());
        payment.setAmount(amount);
        payment.setMethod(method);
        salePaymentRepository.save(payment);

        return schedule;
    }

    @GetMapping("/overdue")
    public List<InstallmentSchedule> overdue() {
        return scheduleRepository.findByDueDateBeforeAndStatusIn(LocalDate.now(), List.of("PENDING", "PARTIAL"));
    }

    @GetMapping("/upcoming")
    public List<InstallmentSchedule> upcoming(@RequestParam(defaultValue = "7") int days) {
        return scheduleRepository.findByDueDateBetween(LocalDate.now(), LocalDate.now().plusDays(days));
    }
}
