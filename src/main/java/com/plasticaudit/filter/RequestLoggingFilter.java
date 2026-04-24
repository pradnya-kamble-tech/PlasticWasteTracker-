package com.plasticaudit.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * CO2 — Servlet Filter for logging all HTTP requests.
 * Captures request URL, method, user, status code, and response time.
 * Demonstrates Servlet API usage in the Spring Boot context.
 */
@Component
@WebFilter(urlPatterns = "/*")
public class RequestLoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void init(FilterConfig filterConfig) {
        log.info("[RequestLoggingFilter] Initialized — Servlet Filter active.");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;

        long startTime = System.currentTimeMillis();

        String method = httpReq.getMethod();
        String uri = httpReq.getRequestURI();
        String queryString = httpReq.getQueryString();
        String remoteAddr = httpReq.getRemoteAddr();
        String user = (httpReq.getUserPrincipal() != null)
                ? httpReq.getUserPrincipal().getName()
                : "ANONYMOUS";
        String timestamp = LocalDateTime.now().format(FORMATTER);

        // Proceed with the filter chain
        chain.doFilter(request, response);

        long duration = System.currentTimeMillis() - startTime;
        int status = httpRes.getStatus();

        // Skip logging for static resources
        if (!uri.startsWith("/css") && !uri.startsWith("/js") && !uri.startsWith("/images")) {
            log.info("[HTTP] {} | {} {} {} | User: {} | IP: {} | Status: {} | {}ms",
                    timestamp, method, uri,
                    (queryString != null ? "?" + queryString : ""),
                    user, remoteAddr, status, duration);
        }
    }

    @Override
    public void destroy() {
        log.info("[RequestLoggingFilter] Destroyed.");
    }
}
