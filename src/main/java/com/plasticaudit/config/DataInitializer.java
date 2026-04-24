package com.plasticaudit.config;

import com.plasticaudit.entity.Industry;
import com.plasticaudit.entity.Role;
import com.plasticaudit.entity.User;
import com.plasticaudit.entity.WasteEntry;
import com.plasticaudit.repository.IndustryRepository;
import com.plasticaudit.repository.RoleRepository;
import com.plasticaudit.repository.UserRepository;
import com.plasticaudit.repository.WasteEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.plasticaudit.service.ReportAggregatorService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * DataInitializer — Seeds initial roles, admin user, and sample data on first
 * run.
 * Uses Spring Security BCrypt to hash passwords.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private IndustryRepository industryRepository;
    @Autowired
    private WasteEntryRepository wasteEntryRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ReportAggregatorService reportAggregatorService;

    @Override
    @Transactional
    public void run(String... args) {
        // Seed Roles
        Role adminRole = seedRole("ROLE_ADMIN");
        Role industryRole = seedRole("ROLE_INDUSTRY");

        // Seed Industries
        Industry greenPack = seedIndustry("GreenPack Solutions", "Packaging",
                "Mumbai, Maharashtra", "REG-001", "contact@greenpack.in", 50000.0);
        Industry ecoFabric = seedIndustry("EcoFabric Ltd.", "Textiles",
                "Surat, Gujarat", "REG-002", "info@ecofabric.in", 30000.0);
        Industry plastiCycle = seedIndustry("PlastiCycle Corp.", "Recycling",
                "Chennai, Tamil Nadu", "REG-003", "ops@plasticycle.in", 75000.0);

        // Seed Admin User
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@plasticaudit.gov.in");
            admin.setFullName("Regulator Admin");
            admin.setEnabled(true);
            admin.addRole(adminRole);
            userRepository.save(admin);
            log.info("[DataInit] Admin user created: admin / admin123");
        }

        // Seed Industry Users
        seedIndustryUser("industry1", "industry123", "user1@greenpack.in",
                "Rajesh Kumar", industryRole, greenPack);
        seedIndustryUser("industry2", "industry123", "user2@ecofabric.in",
                "Priya Nair", industryRole, ecoFabric);
        seedIndustryUser("industry3", "industry123", "user3@plasticycle.in",
                "Ankit Shah", industryRole, plastiCycle);

        // Seed Sample Waste Entries
        seedWasteEntries(greenPack);
        seedWasteEntries(ecoFabric);
        seedWasteEntries(plastiCycle);

        User adminUser = userRepository.findByUsername("admin").orElse(null);
        if (adminUser != null) {
            reportAggregatorService.aggregateReportsForAllIndustries(
                    LocalDate.now().minusMonths(6), LocalDate.now(), adminUser);
        }

        log.info("[DataInit] Sample data initialization complete.");
    }

    private Role seedRole(String name) {
        return roleRepository.findByName(name).orElseGet(() -> {
            log.info("[DataInit] Creating role: {}", name);
            return roleRepository.save(new Role(name));
        });
    }

    private Industry seedIndustry(String name, String sector, String location,
            String regNo, String email, Double target) {
        return industryRepository.findByRegistrationNo(regNo).orElseGet(() -> {
            Industry i = new Industry();
            i.setName(name);
            i.setSector(sector);
            i.setLocation(location);
            i.setRegistrationNo(regNo);
            i.setContactEmail(email);
            i.setAnnualPlasticTargetKg(target);
            log.info("[DataInit] Creating industry: {}", name);
            return industryRepository.save(i);
        });
    }

    private void seedIndustryUser(String username, String password, String email,
            String fullName, Role role, Industry industry) {
        if (!userRepository.existsByUsername(username)) {
            User u = new User();
            u.setUsername(username);
            u.setPassword(passwordEncoder.encode(password));
            u.setEmail(email);
            u.setFullName(fullName);
            u.setEnabled(true);
            u.setIndustry(industry);
            u.addRole(role);
            userRepository.save(u);
            log.info("[DataInit] Industry user created: {} / {}", username, password);
        }
    }

    private void seedWasteEntries(Industry industry) {
        log.info("[DataInit] Seeding sample waste entries for {}", industry.getName());
        for (int i = 1; i <= 3; i++) {
            WasteEntry entry = new WasteEntry();
            entry.setIndustry(industry);
            entry.setEntryDate(LocalDate.now().minusMonths(i));
            entry.setPlasticGeneratedKg(250.0 * i);
            entry.setPlasticRecycledKg(100.0 * i);
            entry.setPlasticEliminatedKg(50.0 * i);
            entry.setEntryType(WasteEntry.EntryType.MONTHLY);
            entry.setNotes("Auto-seeded sample data " + i);
            entry.setVerified(true);
            wasteEntryRepository.save(entry);
        }
    }

}
