package com.plasticaudit.config;

import com.plasticaudit.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * CO2 — Spring Security configuration for role-based access control.
 * Roles: ROLE_ADMIN (Regulator) and ROLE_INDUSTRY (Industry User).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userDetailsService);
        auth.setPasswordEncoder(passwordEncoder());
        return auth;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        // Public resources
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        .requestMatchers("/login", "/logout", "/error").permitAll()

                        // ROLE_ADMIN only — admin panel, report generation
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/reports/generate/**").hasRole("ADMIN")
                        .requestMatchers("/reports/all").hasRole("ADMIN")
                        .requestMatchers("/socket/alert/**").hasRole("ADMIN")

                        // ROLE_INDUSTRY — waste entry management (own data)
                        .requestMatchers("/waste/**").hasAnyRole("ADMIN", "INDUSTRY")
                        .requestMatchers("/dashboard/**").hasAnyRole("ADMIN", "INDUSTRY")
                        .requestMatchers("/reports/**").hasAnyRole("ADMIN", "INDUSTRY")
                        .requestMatchers("/sdg/**").hasAnyRole("ADMIN", "INDUSTRY")

                        // AI Prediction — accessible to both roles; controller enforces ownership
                        .requestMatchers("/prediction/**").hasAnyRole("ADMIN", "INDUSTRY")

                        // Everything else requires authentication
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(successHandler)
                        .failureUrl("/login?error=true")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .permitAll())
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                                    "Access Denied: Insufficient privileges");
                        }))
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .expiredUrl("/login?sessionExpired=true"));

        return http.build();
    }
}
