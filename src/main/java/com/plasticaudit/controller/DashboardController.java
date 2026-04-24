package com.plasticaudit.controller;

import com.plasticaudit.entity.User;
import com.plasticaudit.service.IndustryService;
import com.plasticaudit.service.ReportAggregatorService;
import com.plasticaudit.service.UserService;
import com.plasticaudit.service.WasteEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * CO2/CO3 — Dashboard Controller.
 * Shows different views for ADMIN vs INDUSTRY role.
 * Loads SDG metrics for the dashboard widget.
 */
@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private IndustryService industryService;
    @Autowired
    private WasteEntryService wasteEntryService;
    @Autowired
    private UserService userService;
    @Autowired
    private ReportAggregatorService reportAggregatorService;

    @GetMapping
    public String dashboard(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        model.addAttribute("currentUser", currentUser.getUsername());
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("totalIndustries", industryService.getTotalCount());
        model.addAttribute("totalEntries", wasteEntryService.getTotalCount());

        // SDG Metrics — global aggregate
        java.util.List<Object[]> metricsList = wasteEntryService.getGlobalSdgMetrics();
        double generated = 0;
        double recycled = 0;
        double eliminated = 0;

        if (metricsList != null && !metricsList.isEmpty()) {
            Object[] metrics = metricsList.get(0);
            generated = metrics[0] != null ? ((Number) metrics[0]).doubleValue() : 0;
            recycled = metrics[1] != null ? ((Number) metrics[1]).doubleValue() : 0;
            eliminated = metrics[2] != null ? ((Number) metrics[2]).doubleValue() : 0;
        }

        double reductionRate = (generated > 0) ? ((recycled + eliminated) / generated) * 100.0 : 0;
        double recyclingRatio = (generated > 0) ? (recycled / generated) * 100.0 : 0;

        model.addAttribute("sdgGenerated", String.format("%.2f", generated));
        model.addAttribute("sdgRecycled", String.format("%.2f", recycled));
        model.addAttribute("sdgEliminated", String.format("%.2f", eliminated));
        model.addAttribute("sdgReductionRate", String.format("%.1f", reductionRate));
        model.addAttribute("sdgRecyclingRatio", String.format("%.1f", recyclingRatio));

        if (isAdmin) {
            model.addAttribute("recentReports", reportAggregatorService.findAllReports());
            model.addAttribute("allIndustries", industryService.findAll());
        } else {
            // Find current user's industry-specific data
            userService.findByUsername(currentUser.getUsername()).ifPresent(u -> {
                if (u.getIndustry() != null) {
                    model.addAttribute("userIndustry", u.getIndustry());
                    model.addAttribute("myEntries",
                            wasteEntryService.findByIndustryId(u.getIndustry().getId()));
                    model.addAttribute("myReports",
                            reportAggregatorService.findReportsByIndustry(u.getIndustry().getId()));
                }
            });
        }

        return "dashboard/index";
    }
}
