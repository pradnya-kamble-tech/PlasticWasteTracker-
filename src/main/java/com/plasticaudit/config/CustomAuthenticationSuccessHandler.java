package com.plasticaudit.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

/**
 * Custom authentication success handler that enforces the explicit role
 * selection
 * from the login page dropdown.
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        String userType = request.getParameter("userType");
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        boolean hasAdminRole = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean hasIndustryRole = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_INDUSTRY"));

        boolean roleMatched = false;

        if ("ADMIN".equalsIgnoreCase(userType) && hasAdminRole) {
            roleMatched = true;
        } else if ("INDUSTRY".equalsIgnoreCase(userType) && hasIndustryRole) {
            roleMatched = true;
        }

        if (roleMatched) {
            response.sendRedirect("/dashboard");
        } else {
            // Role mismatch. Logout user and redirect back to login with specific error.
            request.getSession().invalidate();
            response.sendRedirect("/login?error=role_mismatch");
        }
    }
}
