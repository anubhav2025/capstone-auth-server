package com.capstone.authServer.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.capstone.authServer.model.Role;
import com.capstone.authServer.model.User;
import com.capstone.authServer.repository.UserRepository;
import com.capstone.authServer.security.RoleGuard;

@RestController
public class UserController {

    private final UserRepository userRepo;

    public UserController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/user/me")
    @RoleGuard(allowed={"SUPER_ADMIN","ADMIN", "USER"})
    public ResponseEntity<Map<String, Object>> getCurrentUserInfo() {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        // Check basic auth conditions
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("error", "Not authenticated or no session."));
        }

        // Confirm it's OAuth2
        if (!(authentication instanceof OAuth2AuthenticationToken oAuthToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("error", "Not an OAuth2 authentication token."));
        }

        // Extract "email" from the user attributes 
        // (since 'authentication.getName()' is the 'sub' from Google, not the email).
        Map<String, Object> attributes = oAuthToken.getPrincipal().getAttributes();
        String email = (String) attributes.get("email");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("error", "No email found in Google user attributes."));
        }

        // Fetch from DB by email
        User user = userRepo.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("error", "User not found in DB."));
        }

        // Build roles from the user entity
        var roles = user.getRoles().stream()
                        .map(Role::getRoleName)
                        .toList();

        // Build JSON response
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", user.getId());
        result.put("name", user.getName());
        result.put("email", user.getEmail());
        result.put("pictureUrl", user.getPictureUrl());
        result.put("roles", roles);

        return ResponseEntity.ok(result);
    }
}


