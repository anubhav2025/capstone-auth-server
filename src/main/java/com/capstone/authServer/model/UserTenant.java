package com.capstone.authServer.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_roles") // or rename if you prefer
public class UserTenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user_id references users.google_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_google_id", referencedColumnName = "google_id", nullable = false)
    private User user;

    // The tenant_id references tenants.tenant_id (NOT tenants.id)
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    // The user’s role in this tenant
    private String role;

    public UserTenant() {
    }

    public UserTenant(User user, String tenantId, String role) {
        this.user = user;
        this.tenantId = tenantId;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public String getTenantId() {
        return tenantId;
    }
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
}
