package com.plasticaudit.controller;

import com.plasticaudit.entity.Industry;
import com.plasticaudit.entity.User;
import com.plasticaudit.service.IndustryService;
import com.plasticaudit.service.UserService;
import com.plasticaudit.socket.AlertClient;
import com.plasticaudit.socket.AlertServer;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * CO2 — Admin Controller (ROLE_ADMIN only).
 * Manages industries, users, and socket alert testing.
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private IndustryService industryService;
    @Autowired
    private AlertServer alertServer;
    @Autowired
    private AlertClient alertClient;

    // ── Users ─────────────────────────────────────────────────────────────

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("industries", industryService.findAll());
        model.addAttribute("newUser", new User());
        return "admin/users";
    }

    @PostMapping("/users/toggle/{id}")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes ra) {
        userService.toggleUserStatus(id);
        ra.addFlashAttribute("successMsg", "User status updated.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/register")
    public String registerUser(@Valid @ModelAttribute("newUser") User user,
            BindingResult result,
            @RequestParam String roleName,
            @RequestParam(required = false) Long industryId,
            Model model,
            RedirectAttributes ra) {
        if (result.hasErrors() || userService.usernameExists(user.getUsername())) {
            model.addAttribute("users", userService.findAllUsers());
            model.addAttribute("industries", industryService.findAll());
            model.addAttribute("errorMsg", "Username already exists or invalid fields.");
            return "admin/users";
        }
        if (industryId != null) {
            industryService.findById(industryId).ifPresent(user::setIndustry);
        }
        userService.registerUser(user, roleName);
        ra.addFlashAttribute("successMsg", "User '" + user.getUsername() + "' registered.");
        return "redirect:/admin/users";
    }

    // ── Industries ────────────────────────────────────────────────────────

    @GetMapping("/industries")
    public String listIndustries(Model model) {
        model.addAttribute("industries", industryService.findAll());
        model.addAttribute("newIndustry", new Industry());
        return "admin/industries";
    }

    @PostMapping("/industries/save")
    public String saveIndustry(@Valid @ModelAttribute("newIndustry") Industry industry,
            BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("industries", industryService.findAll());
            return "admin/industries";
        }
        industryService.saveIndustry(industry);
        ra.addFlashAttribute("successMsg", "Industry saved.");
        return "redirect:/admin/industries";
    }

    @PostMapping("/industries/delete/{id}")
    public String deleteIndustry(@PathVariable Long id, RedirectAttributes ra) {
        industryService.deleteById(id);
        ra.addFlashAttribute("successMsg", "Industry deleted.");
        return "redirect:/admin/industries";
    }

    // ── Socket Alert Test ─────────────────────────────────────────────────

    @GetMapping("/socket")
    public String socketPage(Model model) {
        model.addAttribute("connectedClients", alertServer.getConnectedClientCount());
        return "admin/socket";
    }

    @PostMapping("/socket/broadcast")
    public String broadcastAlert(@RequestParam String message, RedirectAttributes ra) {
        alertServer.broadcastAlert(message);
        ra.addFlashAttribute("successMsg", "Alert broadcast sent: " + message);
        return "redirect:/admin/socket";
    }

    @PostMapping("/socket/test-client")
    public String testClient(@RequestParam String command, Model model, RedirectAttributes ra) {
        String response = alertClient.sendAlertCommand(command);
        ra.addFlashAttribute("socketResponse", response);
        return "redirect:/admin/socket";
    }
}
