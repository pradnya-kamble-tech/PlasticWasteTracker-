package com.plasticaudit.controller;

import com.plasticaudit.entity.AuditReport;
import com.plasticaudit.entity.User;
import com.plasticaudit.service.IndustryService;
import com.plasticaudit.service.ReportAggregatorService;
import com.plasticaudit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * CO2/CO3/CO1 — Report Controller.
 * Triggers the async multithreaded report aggregator.
 */
@Controller
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportAggregatorService reportAggregatorService;
    @Autowired
    private UserService userService;
    @Autowired
    private IndustryService industryService;

    @GetMapping
    public String myReports(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            model.addAttribute("reports", reportAggregatorService.findAllReports());
            model.addAttribute("industries", industryService.findAll());
        } else {
            userService.findByUsername(currentUser.getUsername()).ifPresent(u -> {
                if (u.getIndustry() != null) {
                    model.addAttribute("reports",
                            reportAggregatorService.findReportsByIndustry(u.getIndustry().getId()));
                }
            });
        }
        model.addAttribute("isAdmin", isAdmin);
        return "reports/view";
    }

    @GetMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public String showGenerateForm(Model model) {
        model.addAttribute("industries", industryService.findAll());
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("monthAgo", LocalDate.now().minusMonths(1));
        return "reports/generate";
    }

    /**
     * CO1 — Triggers multithreaded @Async report aggregation for all industries.
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public String generateReports(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd,
            @AuthenticationPrincipal UserDetails currentUser,
            RedirectAttributes redirectAttributes) {

        User user = userService.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // CO1 — Fire and forget: async multithreaded aggregation
        CompletableFuture<List<AuditReport>> future = reportAggregatorService
                .aggregateReportsForAllIndustries(periodStart, periodEnd, user);

        future.thenAccept(reports -> System.out
                .println("[CO1] Async aggregation complete: " + reports.size() + " reports generated"));

        redirectAttributes.addFlashAttribute("successMsg",
                "Report generation started for period " + periodStart + " to " + periodEnd +
                        ". Check back in a moment for results.");
        return "redirect:/reports";
    }

    @PostMapping("/status/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateStatus(@PathVariable Long id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {
        reportAggregatorService.updateReportStatus(id, AuditReport.ReportStatus.valueOf(status));
        redirectAttributes.addFlashAttribute("successMsg", "Report status updated.");
        return "redirect:/reports";
    }
}
