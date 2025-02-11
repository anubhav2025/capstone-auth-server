package com.capstone.authServer.security;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


import com.capstone.authServer.model.Role;
import com.capstone.authServer.model.User;
import com.capstone.authServer.repository.UserRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Aspect
@Component
public class RoleGuardAspect {

    private final UserRepository userRepository;

    public RoleGuardAspect(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Before("@annotation(roleGuard)")
    public void checkRoles(JoinPoint joinPoint, RoleGuard roleGuard) {
        Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");
        }

        // Typically, we only proceed if it's an OAuth2AuthenticationToken
        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            throw new AccessDeniedException("Authentication is not OAuth2, or user not found in DB");
        }

        // Extract email from OAuth2 attributes
        Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();
        String email = (String) attributes.get("email");
        if (email == null) {
            throw new AccessDeniedException("No email found in OAuth attributes");
        }

        // Fetch user from DB by email
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new AccessDeniedException("User not found in DB");
        }

        // Build a set of role names from DB, e.g. ["USER","ADMIN","SUPER_ADMIN"]
        var userRoles = user.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());

        // The annotation's required roles
        var requiredRoles = roleGuard.allowed();

        // Check if user has at least one required role
        boolean hasPermission = false;
        for (String required : requiredRoles) {
            // e.g. "ADMIN" or "SUPER_ADMIN"
            if (userRoles.contains(required)) {
                hasPermission = true;
                break;
            }
        }

        // If annotation says e.g. @RoleGuard(allowed={"ADMIN","SUPER_ADMIN"}),
        // but user does not have them, deny
        if (requiredRoles.length > 0 && !hasPermission) {
            System.out.println("User " + email + " not authorized. Required: " 
                + Arrays.toString(requiredRoles) + ", user has: " + userRoles);
            throw new AccessDeniedException("You do not have the required role(s): " 
                + String.join(",", requiredRoles));
        }
    }
}





// @Aspect
// @Component
// public class RoleGuardAspect {

//     @Before("@annotation(roleGuard)")
//     public void checkRoles(JoinPoint joinPoint, RoleGuard roleGuard) {
//         var authentication = SecurityContextHolder.getContext().getAuthentication();
//         if (authentication == null || !authentication.isAuthenticated()) {
//             throw new AccessDeniedException("Not authenticated");
//         }

//         var requiredRoles = roleGuard.allowed();
//         System.out.println(authentication);
//         // user roles => "ROLE_USER","ROLE_ADMIN","ROLE_SUPER_ADMIN"
//         var userAuthorities = authentication.getAuthorities();

//         boolean hasPermission = false;
//         for (String required : requiredRoles) {
//             // e.g. required = "SUPER_ADMIN"
//             // userAuthorities contain "ROLE_SUPER_ADMIN"
//             // Check if userAuthorities has "ROLE_" + required
//             String needed = "ROLE_" + required;
//             boolean found = userAuthorities.stream()
//                 .anyMatch(a -> a.getAuthority().equals(needed));
//             if (found) {
//                 hasPermission = true;
//                 break;
//             }
//         }

//         if (requiredRoles.length > 0 && !hasPermission) {
//             System.out.println("not authorized");
//             throw new AccessDeniedException("You do not have the required role(s): " + String.join(",", requiredRoles));
//         }
//     }
// }
