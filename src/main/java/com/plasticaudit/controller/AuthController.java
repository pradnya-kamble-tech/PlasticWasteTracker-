package com.plasticaudit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * CO2 — Auth Controller for login/logout.
 */
@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }
}
