package com.plasticaudit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Plastic Waste Audit & Reduction Tracker
 * SDG 12 (Responsible Consumption) | SDG 14 (Life Below Water)
 *
 * Enterprise Java Web Application — Spring Boot 3.x
 * Extends SpringBootServletInitializer for WAR deployment on Tomcat 10
 */
@SpringBootApplication
@EnableAsync // CO1: Async multithreading support
@EnableScheduling // CO1: Scheduled tasks support
@ServletComponentScan // CO2: Enables @WebFilter discovery for RequestLoggingFilter
public class PlasticWasteTrackerApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(PlasticWasteTrackerApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(PlasticWasteTrackerApplication.class, args);
    }
}
