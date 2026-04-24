# ♻️ Plastic Waste Audit & Reduction Tracker

> **Enterprise Java Web Application** | Spring Boot 3.x | Hibernate 6.x | MySQL | SDG 12 & 14

[![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-green?style=flat-square)](https://spring.io/projects/spring-boot)
[![Hibernate](https://img.shields.io/badge/Hibernate-6.x-blue?style=flat-square)](https://hibernate.org)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-blue?style=flat-square)](https://www.mysql.com)
[![License](https://img.shields.io/badge/License-Academic-lightgrey?style=flat-square)](LICENSE)

---

## 📌 Project Overview

The **Plastic Waste Audit & Reduction Tracker** is a full-stack enterprise Java web application where industries log their plastic waste (generated, recycled, eliminated). A **multithreaded report aggregator** compiles the data for regulator audits.

### UN SDG Alignment

| SDG | Goal | How this app addresses it |
|-----|------|--------------------------|
| **SDG 12** | Responsible Consumption & Production | Industries set annual reduction targets; recycling ratios and reduction rates are tracked as measurable KPIs |
| **SDG 14** | Life Below Water | Plastic eliminated from waste streams before reaching waterways is measured as the primary ocean-protection metric |

**SDG Impact Metric:** Each industry's *Reduction Rate (%)* = `(Recycled + Eliminated) / Generated × 100`

---

## 🏗️ Technology Stack

| Layer | Technology |
|-------|-----------|
| **Framework** | Spring Boot 3.2.5 (`@Controller`, `@Service`, `@Repository`) |
| **ORM** | Hibernate 6.x with JPA (`@Entity`, `@OneToMany`, `@ManyToMany`, `@Transactional`) |
| **Database** | MySQL 8.x — 6 normalized tables with FK constraints |
| **View Layer** | Thymeleaf templates + Thymeleaf Spring Security extras (CO2) |
| **Security** | Spring Security — `ROLE_ADMIN` (Regulator), `ROLE_INDUSTRY` |
| **Servlet Filter** | `RequestLoggingFilter` — logs all HTTP requests (CO2) |
| **JDBC** | `BatchWasteJdbcRepository` — raw `PreparedStatement` batch insert (CO1) |
| **Multithreading** | `ReportAggregatorService` — `@Async` + `ThreadPoolExecutor` (CO1) |
| **Socket** | `AlertServer` (`ServerSocket`) + `AlertClient` (`Socket`) — CO4 |
| **Build** | Maven (WAR packaging, deployable on Tomcat 10) |

---

## 🗂️ Project Structure

```
src/main/java/com/plasticaudit/
├── PlasticWasteTrackerApplication.java   ← @SpringBootApplication + WAR support
├── config/
│   ├── SecurityConfig.java               ← Spring Security (2 roles)
│   ├── DataInitializer.java              ← Seeds demo data on startup
│   └── WebConfig.java
├── controller/
│   ├── AuthController.java
│   ├── DashboardController.java          ← SDG metrics dashboard
│   ├── WasteEntryController.java         ← CRUD for waste entries
│   ├── ReportController.java             ← Triggers async aggregation
│   └── AdminController.java             ← User/industry/socket admin
├── entity/                              ← Hibernate @Entity classes
│   ├── User.java        (@ManyToMany roles, @ManyToOne industry)
│   ├── Role.java
│   ├── Industry.java    (@OneToMany waste entries + reports)
│   ├── WasteEntry.java
│   └── AuditReport.java
├── repository/                          ← Spring Data JPA + HQL
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   ├── IndustryRepository.java
│   ├── WasteEntryRepository.java
│   └── AuditReportRepository.java
├── service/                             ← @Transactional business logic
│   ├── UserService.java
│   ├── IndustryService.java
│   ├── WasteEntryService.java
│   ├── ReportAggregatorService.java     ← CO1: @Async + ThreadPoolExecutor
│   └── CustomUserDetailsService.java   ← Spring Security integration
├── jdbc/
│   └── BatchWasteJdbcRepository.java   ← CO1: PreparedStatement batch
├── socket/
│   ├── AlertServer.java                ← CO4: ServerSocket on port 9090
│   └── AlertClient.java               ← CO4: Socket client
└── filter/
    └── RequestLoggingFilter.java       ← CO2: Servlet filter

src/main/resources/
├── application.properties
├── schema.sql                          ← DDL for 6 normalized tables
├── static/
│   ├── css/style.css
│   └── js/app.js
└── templates/
    ├── layout/base.html
    ├── auth/login.html
    ├── dashboard/index.html            ← SDG metrics cards + progress bar
    ├── waste/form.html                 ← Live reduction rate preview
    ├── waste/list.html
    ├── reports/view.html               ← SDG KPI table
    ├── reports/generate.html           ← Trigger async aggregation
    └── admin/
        ├── users.html
        ├── industries.html
        └── socket.html                 ← CO4 socket test panel
```

---

## 🗄️ Database Schema (6 Tables)

```sql
roles          (id, name)
industries     (id, name, sector, location, registration_no, annual_plastic_target_kg, ...)
users          (id, username, password, email, full_name, enabled, industry_id FK)
user_roles     (user_id FK, role_id FK)          -- @ManyToMany join table
waste_entries  (id, industry_id FK, entry_date, plastic_generated_kg,
                plastic_recycled_kg, plastic_eliminated_kg, entry_type, verified)
audit_reports  (id, industry_id FK, generated_by_user_id FK, period_start, period_end,
                total_generated_kg, total_recycled_kg, reduction_rate_percent,
                recycling_ratio_percent, status)
```

---

## 🚀 Setup & Run

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.x running locally

### 1. Create MySQL database
```sql
CREATE DATABASE plastic_waste_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Configure credentials
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=your_password
```

### 3. Build & Run
```bash
# Run with embedded Tomcat (Spring Boot JAR)
mvn clean spring-boot:run

# OR build WAR for external Tomcat 10
mvn clean package -DskipTests
# Deploy target/plastic-waste-tracker.war to Tomcat webapps/
```

### 4. Access the application
- URL: `http://localhost:8080`
- **Admin (Regulator):** `admin` / `admin123`
- **Industry User 1:** `industry1` / `industry123`
- **Industry User 2:** `industry2` / `industry123`
- **Industry User 3:** `industry3` / `industry123`

---

## ✅ Rubric Coverage

| Component | Implementation | Marks |
|-----------|---------------|-------|
| **OOP Principles** | Entity classes with Hibernate, service layers, encapsulation | CO1 ✓ |
| **JDBC (CO1)** | `BatchWasteJdbcRepository` — `PreparedStatement.addBatch()` with manual transaction | CO1 ✓ |
| **Multithreading (CO1)** | `ReportAggregatorService` — `@Async` + `ThreadPoolExecutor` (4-8 threads) | CO1 ✓ |
| **Servlet/JSP API (CO2)** | `RequestLoggingFilter` (Servlet Filter) + Thymeleaf views | CO2 ✓ |
| **Spring MVC (CO3)** | `@Controller`, `@Service`, `@Repository` layers throughout | CO3 ✓ |
| **Hibernate ORM (CO3)** | `@Entity`, `@OneToMany`, `@ManyToMany`, HQL queries, `@Transactional` | CO3 ✓ |
| **Spring Data JPA (CO3)** | All 5 repositories extend `JpaRepository` with custom HQL | CO3 ✓ |
| **Socket (CO4)** | `AlertServer` (ServerSocket, port 9090) + `AlertClient` broadcast | CO4 ✓ |
| **Spring Security** | BCrypt, 2 roles (`ROLE_ADMIN`, `ROLE_INDUSTRY`), form login | ✓ |
| **MySQL (5+ tables)** | 6 normalized tables with FK constraints | ✓ |
| **SDG Integration** | Dashboard with reduction rate %, recycling KPI, SDG justification | ✓ |
| **WAR Deployment** | `extends SpringBootServletInitializer`, WAR packaging in pom.xml | ✓ |

---

## 🌍 SDG Impact Justification (≤200 words)

This application directly aligns with **SDG 12 (Responsible Consumption & Production)** and **SDG 14 (Life Below Water)**. Industries across manufacturing, packaging, and FMCG sectors log monthly plastic waste data — distinguishing between plastic generated, recycled, and eliminated from waste streams. The system computes two measurable KPIs: the **Reduction Rate (%)** `= (Recycled + Eliminated) / Generated × 100` as the SDG 12 metric, and the **Recycling Ratio (%)** `= Recycled / Generated × 100` as the SDG 14 ocean-protection metric. Government regulators (admins) can trigger a multithreaded audit report covering all industries for any date period, instantly measuring aggregate environmental impact. The system's dashboard visualizes whether industries are meeting their annual plastic reduction targets, creating accountability. Real-time socket alerts notify connected regulators when audits complete, enabling timely compliance enforcement. By digitizing plastic waste accountability, this platform transforms paper-based auditing into a data-driven, measurable SDG implementation tool.

---

## 👨‍💻 Author

Built as part of an Enterprise Java Web Application academic project.
