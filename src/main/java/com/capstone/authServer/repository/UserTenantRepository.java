package com.capstone.authServer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.capstone.authServer.model.UserTenant;

public interface UserTenantRepository extends JpaRepository<UserTenant, Long> {

    // Now returns a single UserTenant instead of a list
    List<UserTenant> findByUser_GoogleId(String googleId);

    // If needed, you can also have:
    UserTenant findByUser_GoogleIdAndTenantId(String googleId, String tenantId);
}
