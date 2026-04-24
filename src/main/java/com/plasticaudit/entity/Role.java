package com.plasticaudit.entity;

import jakarta.persistence.*;

/**
 * Role Entity — maps to the 'roles' table.
 * Spring Security role-based access: ROLE_ADMIN, ROLE_INDUSTRY.
 */
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true, nullable = false, length = 50)
    private String name; // e.g. "ROLE_ADMIN", "ROLE_INDUSTRY"

    // ── Constructors ────────────────────────────────────────
    public Role() {
    }

    public Role(String name) {
        this.name = name;
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
}
