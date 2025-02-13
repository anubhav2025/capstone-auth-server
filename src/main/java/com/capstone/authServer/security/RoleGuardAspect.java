package com.capstone.authServer.security;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.capstone.authServer.model.User;
import com.capstone.authServer.model.UserTenant;
import com.capstone.authServer.repository.UserRepository;
import com.capstone.authServer.repository.UserTenantRepository;

import java.util.Map;

@Aspect
@Component
public class RoleGuardAspect {

    private final UserRepository userRepo;
    private final UserTenantRepository userTenantRepo;

    public RoleGuardAspect(UserRepository userRepo, UserTenantRepository userTenantRepo) {
        this.userRepo = userRepo;
        this.userTenantRepo = userTenantRepo;
    }

    @Before("@annotation(roleGuard)")
    public void checkRoles(JoinPoint joinPoint, RoleGuard roleGuard) {
        // 1) Ensure authentication is an OAuth2 token
        Authentication auth =
            org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();

        if (!(auth instanceof OAuth2AuthenticationToken oauthToken) || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated or not OAuth2");
        }

        // 2) Extract user from the DB by email
        Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();
        String email = (String) attributes.get("email");
        if (email == null) {
            throw new AccessDeniedException("No email found in OAuth2 attributes.");
        }

        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new AccessDeniedException("User not found in DB.");
        }

        // 3) Read tenantId from the request parameter e.g. ?tenantId=abc
        HttpServletRequest request =
            ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String tenantId = request.getParameter("tenantId");

        // 4) If tenantId is blank or missing, fallback to user's defaultTenantId
        if (tenantId == null || tenantId.isBlank()) {
            tenantId = user.getDefaultTenantId();
        }

        System.out.println("RoleGuardAspect: extracted tenantId => " + tenantId);

        if (tenantId == null || tenantId.isBlank()) {
            throw new AccessDeniedException("No tenantId provided or default tenantId is missing.");
        }

        // 5) Look up the user's role for that tenant
        UserTenant userTenant = userTenantRepo.findByUser_GoogleIdAndTenantId(user.getGoogleId(), tenantId);
        if (userTenant == null) {
            throw new AccessDeniedException("User is not assigned to tenant " + tenantId);
        }

        String userRole = userTenant.getRole();
        var requiredRoles = roleGuard.allowed();

        // 6) Check if userRole matches any required role
        boolean hasPermission = false;
        for (String required : requiredRoles) {
            if (required.equalsIgnoreCase(userRole)) {
                hasPermission = true;
                break;
            }
        }

        if (!hasPermission) {
            throw new AccessDeniedException("Insufficient role for tenant " + tenantId);
        }

        // If we reach here => user has permission
    }
}
