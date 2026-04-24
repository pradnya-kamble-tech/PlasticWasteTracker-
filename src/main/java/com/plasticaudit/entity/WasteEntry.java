package com.plasticaudit.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * WasteEntry Entity — maps to the 'waste_entries' table.
 * Core domain object: records plastic waste data logged by an industry.
 * Used to calculate SDG 12 & 14 metrics (reduction rate, recycling ratio).
 */
@Entity
@Table(name = "waste_entries")
public class WasteEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // CO3: @ManyToOne — Entry belongs to an Industry
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "industry_id", nullable = false)
    private Industry industry;

    @NotNull
    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Min(0)
    @Column(name = "plastic_generated_kg", nullable = false)
    private Double plasticGeneratedKg = 0.0;

    @Min(0)
    @Column(name = "plastic_recycled_kg", nullable = false)
    private Double plasticRecycledKg = 0.0;

    @Min(0)
    @Column(name = "plastic_eliminated_kg", nullable = false)
    private Double plasticEliminatedKg = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 30)
    private EntryType entryType = EntryType.MONTHLY;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "verified", nullable = false)
    private boolean verified = false;

    // Calculated field — not persisted, SDG metric
    @Transient
    public double getReductionRate() {
        if (plasticGeneratedKg == null || plasticGeneratedKg == 0)
            return 0.0;
        return ((plasticRecycledKg + plasticEliminatedKg) / plasticGeneratedKg) * 100.0;
    }

    public enum EntryType {
        DAILY, WEEKLY, MONTHLY, QUARTERLY, ANNUAL
    }

    // ── Constructors ────────────────────────────────────────
    public WasteEntry() {
    }

    // ── Getters & Setters ───────────────────────────────────
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Industry getIndustry() {
        return industry;
    }

    public void setIndustry(Industry industry) {
        this.industry = industry;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public Double getPlasticGeneratedKg() {
        return plasticGeneratedKg;
    }

    public void setPlasticGeneratedKg(Double plasticGeneratedKg) {
        this.plasticGeneratedKg = plasticGeneratedKg;
    }

    public Double getPlasticRecycledKg() {
        return plasticRecycledKg;
    }

    public void setPlasticRecycledKg(Double plasticRecycledKg) {
        this.plasticRecycledKg = plasticRecycledKg;
    }

    public Double getPlasticEliminatedKg() {
        return plasticEliminatedKg;
    }

    public void setPlasticEliminatedKg(Double plasticEliminatedKg) {
        this.plasticEliminatedKg = plasticEliminatedKg;
    }

    public EntryType getEntryType() {
        return entryType;
    }

    public void setEntryType(EntryType entryType) {
        this.entryType = entryType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
