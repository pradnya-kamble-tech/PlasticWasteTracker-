package com.plasticaudit.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * Industry Entity — maps to the 'industries' table.
 * CO3: @OneToMany with WasteEntry and AuditReport.
 */
@Entity
@Table(name = "industries")
public class Industry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "sector", length = 100)
    private String sector;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "registration_no", unique = true, length = 60)
    private String registrationNo;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "annual_plastic_target_kg")
    private Double annualPlasticTargetKg;

    // CO3: @OneToMany — Industry has many WasteEntries
    @OneToMany(mappedBy = "industry", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WasteEntry> wasteEntries;

    // CO3: @OneToMany — Industry has many AuditReports
    @OneToMany(mappedBy = "industry", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AuditReport> auditReports;

    // CO3: @OneToMany — Industry has many Users
    @OneToMany(mappedBy = "industry", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    private List<User> users;

    // ── Constructors ────────────────────────────────────────
    public Industry() {
    }

    // ── Getters & Setters ───────────────────────────────────
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public Double getAnnualPlasticTargetKg() {
        return annualPlasticTargetKg;
    }

    public void setAnnualPlasticTargetKg(Double annualPlasticTargetKg) {
        this.annualPlasticTargetKg = annualPlasticTargetKg;
    }

    public List<WasteEntry> getWasteEntries() {
        return wasteEntries;
    }

    public void setWasteEntries(List<WasteEntry> wasteEntries) {
        this.wasteEntries = wasteEntries;
    }

    public List<AuditReport> getAuditReports() {
        return auditReports;
    }

    public void setAuditReports(List<AuditReport> auditReports) {
        this.auditReports = auditReports;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
