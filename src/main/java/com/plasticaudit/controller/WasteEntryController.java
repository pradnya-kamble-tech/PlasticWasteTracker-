package com.plasticaudit.controller;

import com.plasticaudit.entity.Industry;
import com.plasticaudit.entity.WasteEntry;
import com.plasticaudit.service.IndustryService;
import com.plasticaudit.service.UserService;
import com.plasticaudit.service.WasteEntryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * CO2/CO3 — WasteEntry CRUD Controller.
 */
@Controller
@RequestMapping("/waste")
public class WasteEntryController {

    @Autowired
    private WasteEntryService wasteEntryService;
    @Autowired
    private IndustryService industryService;
    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public String listEntries(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        List<WasteEntry> entries;
        if (isAdmin) {
            entries = wasteEntryService.findAllRecent();
        } else {
            entries = userService.findByUsername(currentUser.getUsername())
                    .map(u -> u.getIndustry() != null
                            ? wasteEntryService.findByIndustryId(u.getIndustry().getId())
                            : List.<WasteEntry>of())
                    .orElse(List.of());
        }
        model.addAttribute("entries", entries);
        model.addAttribute("isAdmin", isAdmin);
        return "waste/list";
    }

    @GetMapping("/new")
    public String showEntryForm(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        WasteEntry entry = new WasteEntry();
        model.addAttribute("wasteEntry", entry);
        model.addAttribute("industries", industryService.findAll());
        model.addAttribute("entryTypes", WasteEntry.EntryType.values());

        // Pre-select user's industry
        userService.findByUsername(currentUser.getUsername()).ifPresent(u -> {
            if (u.getIndustry() != null) {
                model.addAttribute("userIndustryId", u.getIndustry().getId());
            }
        });
        return "waste/form";
    }

    @PostMapping("/save")
    public String saveEntry(@Valid @ModelAttribute("wasteEntry") WasteEntry entry,
            BindingResult result,
            @RequestParam("industryId") Long industryId,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("industries", industryService.findAll());
            model.addAttribute("entryTypes", WasteEntry.EntryType.values());
            return "waste/form";
        }
        Industry industry = industryService.findById(industryId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid industry ID: " + industryId));
        entry.setIndustry(industry);
        wasteEntryService.save(entry);
        redirectAttributes.addFlashAttribute("successMsg", "Waste entry saved successfully!");
        return "redirect:/waste/list";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        WasteEntry entry = wasteEntryService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Waste entry not found: " + id));
        model.addAttribute("wasteEntry", entry);
        model.addAttribute("industries", industryService.findAll());
        model.addAttribute("entryTypes", WasteEntry.EntryType.values());
        return "waste/form";
    }

    @PostMapping("/delete/{id}")
    public String deleteEntry(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        wasteEntryService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMsg", "Entry deleted.");
        return "redirect:/waste/list";
    }
}
