package com.capstone.authServer.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    // We store the user’s Google ID as the primary key
    @Id
    @Column(name = "google_id")
    private String googleId;

    private String email;
    private String name;

    @Column(name = "picture_url")
    private String pictureUrl;

    private boolean enabled = true;

    @Column(name = "default_tenant_id")
    private String defaultTenantId; // references tenantId in Tenants table

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public User() {
    }

    public User(String googleId, String email, String name, String pictureUrl) {
        this.googleId = googleId;
        this.email = email;
        this.name = name;
        this.pictureUrl = pictureUrl;
        this.enabled = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters & setters
    public String getGoogleId() {
        return googleId;
    }
    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }
    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDefaultTenantId() {
        return defaultTenantId;
    }
    public void setDefaultTenantId(String defaultTenantId) {
        this.defaultTenantId = defaultTenantId;
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
