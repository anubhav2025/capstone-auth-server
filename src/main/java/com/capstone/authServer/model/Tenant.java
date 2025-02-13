package com.capstone.authServer.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Primary key

    @Column(name = "tenant_id", unique = true, nullable = false)
    private String tenantId;  // Business key (e.g. "T1", "T2")

    @Column(name = "tenant_name", nullable = false)
    private String tenantName;

    // These three fields represent repository info and PAT
    private String owner;
    private String repo;
    private String pat;

    @Column(name = "es_index")
    private String esIndex;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Tenant() {
    }

    // Getters & setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }
    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getOwner() {
        return owner;
    }
    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getRepo() {
        return repo;
    }
    public void setRepo(String repo) {
        this.repo = repo;
    }

    public String getPat() {
        return pat;
    }
    public void setPat(String pat) {
        this.pat = pat;
    }

    public String getEsIndex() {
        return esIndex;
    }
    public void setEsIndex(String esIndex) {
        this.esIndex = esIndex;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
