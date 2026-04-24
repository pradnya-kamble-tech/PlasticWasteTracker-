package com.plasticaudit.prediction;

import com.plasticaudit.service.IndustryService;
import com.plasticaudit.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * AI Prediction Controller.
 * - GET /prediction → redirect to user's own industry (for industry users)
 * - GET /prediction/{id} → Full prediction page
 * - GET /prediction/api/{id} → JSON API for AJAX/Chart.js (returns
 * PredictionResult)
 */
@Controller
@RequestMapping("/prediction")
public class PredictionController {

    @Autowired
    private PredictionService predictionService;

    @Autowired
    private IndustryService industryService;

    @Autowired
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Root — redirect admin to industry #1 or industry user to their own industry
     */
    @GetMapping
    public String predictionRoot(@AuthenticationPrincipal UserDetails currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            // Show the first available industry or a selection page
            return "redirect:/prediction/select";
        }
        // Industry user → their own industry
        return userService.findByUsername(currentUser.getUsername())
                .filter(u -> u.getIndustry() != null)
                .map(u -> "redirect:/prediction/" + u.getIndustry().getId())
                .orElse("redirect:/dashboard");
    }

    /** Admin selection page — lists all industries with prediction links */
    @GetMapping("/select")
    public String selectIndustry(Model model, @AuthenticationPrincipal UserDetails currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("industries", industryService.findAll());
        model.addAttribute("isAdmin", isAdmin);
        return "prediction/select";
    }

    /** Main prediction page for a specific industry */
    @GetMapping("/{industryId}")
    public String predictionPage(@PathVariable Long industryId,
            Model model,
            @AuthenticationPrincipal UserDetails currentUser) throws JsonProcessingException {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Security: industry user can only see their own prediction
        if (!isAdmin) {
            boolean authorized = userService.findByUsername(currentUser.getUsername())
                    .map(u -> u.getIndustry() != null && u.getIndustry().getId().equals(industryId))
                    .orElse(false);
            if (!authorized) {
                return "redirect:/prediction";
            }
        }

        PredictionResult prediction = predictionService.predictNextMonthWaste(industryId);

        // Serialize chart data to JSON for Chart.js
        model.addAttribute("prediction", prediction);
        model.addAttribute("chartLabelsJson", objectMapper.writeValueAsString(prediction.getChartLabels()));
        model.addAttribute("chartGeneratedJson", objectMapper.writeValueAsString(prediction.getChartGenerated()));
        model.addAttribute("chartRecycledJson", objectMapper.writeValueAsString(prediction.getChartRecycled()));
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("allIndustries", isAdmin ? industryService.findAll() : null);

        return "prediction/index";
    }

    /** JSON API endpoint for lightweight dashboard widget polling */
    @GetMapping(value = "/api/{industryId}", produces = "application/json")
    @ResponseBody
    public PredictionResult predictionApi(@PathVariable Long industryId,
            @AuthenticationPrincipal UserDetails currentUser) {
        // Same authorization logic
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            boolean authorized = userService.findByUsername(currentUser.getUsername())
                    .map(u -> u.getIndustry() != null && u.getIndustry().getId().equals(industryId))
                    .orElse(false);
            if (!authorized) {
                PredictionResult denied = new PredictionResult();
                denied.setSufficientData(false);
                denied.setInsufficientDataMessage("Access denied.");
                return denied;
            }
        }
        return predictionService.predictNextMonthWaste(industryId);
    }
}
