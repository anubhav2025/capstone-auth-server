package com.capstone.authServer.controller;

import com.capstone.authServer.model.Tenant;
import com.capstone.authServer.model.User;
import com.capstone.authServer.model.UserTenant;
import com.capstone.authServer.repository.TenantRepository;
import com.capstone.authServer.repository.UserRepository;
import com.capstone.authServer.repository.UserTenantRepository;
import com.capstone.authServer.security.RoleGuard;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class UserController {

    private final UserRepository userRepo;
    private final UserTenantRepository userTenantRepo;
    private final TenantRepository tenantRepo;

    public UserController(UserRepository userRepo,
                          UserTenantRepository userTenantRepo,
                          TenantRepository tenantRepo) {
        this.userRepo = userRepo;
        this.userTenantRepo = userTenantRepo;
        this.tenantRepo = tenantRepo;
    }

    @GetMapping("/user/me")
    @RoleGuard(allowed={"SUPER_ADMIN","ADMIN","USER"})
    public ResponseEntity<Map<String, Object>> getCurrentUserInfo(
            @RequestParam(name = "tenantId", required = false) String tenantId
    ) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof OAuth2AuthenticationToken oAuthToken) || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("error", "Not authenticated or no session."));
        }

        Map<String, Object> attributes = ((OAuth2AuthenticationToken) authentication).getPrincipal().getAttributes();
        String email = (String) attributes.get("email");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("error", "No email found in Google user attributes."));
        }

        User user = userRepo.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("error", "User not found in DB."));
        }

        // If tenantId is not provided, use user’s defaultTenantId
        if (tenantId == null || tenantId.isBlank()) {
            tenantId = user.getDefaultTenantId();
        }

        // 1) Build the main user info
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("googleId", user.getGoogleId());
        result.put("email", user.getEmail());
        result.put("name", user.getName());
        result.put("pictureUrl", user.getPictureUrl());
        result.put("defaultTenantId", user.getDefaultTenantId());
        result.put("currentTenantId", tenantId);

        // 2) Fetch all user_tenant rows for this user
        List<UserTenant> userTenants = userTenantRepo.findByUser_GoogleId(user.getGoogleId());

        // 3) For each row, gather minimal tenant info + user’s role
        List<Map<String, Object>> tenantList = new ArrayList<>();
        for (UserTenant ut : userTenants) {
            // If your UserTenant has a relationship to Tenant:
            // e.g. getTenant() or you do a tenantRepo.findByTenantId(...) 
            // We'll assume you have getTenant() or you store the entire Tenant in userTenant
            // Or we do something like:
            Tenant tenant = tenantRepo.findByTenantId(ut.getTenantId()); 
            if (tenant != null) {
                Map<String, Object> tenantInfo = new HashMap<>();
                tenantInfo.put("tenantId", tenant.getTenantId());
                tenantInfo.put("tenantName", tenant.getTenantName());
                tenantInfo.put("role", ut.getRole());
                // any other minimal fields you want to show
                tenantList.add(tenantInfo);
            }
        }
        // 4) Attach to the response
        result.put("tenants", tenantList);

        return ResponseEntity.ok(result);
    }
}
