package com.capstone.authServer.service;

import com.capstone.authServer.model.User;
import com.capstone.authServer.model.Tenant;
import com.capstone.authServer.model.UserTenant;
import com.capstone.authServer.repository.UserRepository;
import com.capstone.authServer.repository.TenantRepository;
import com.capstone.authServer.repository.UserTenantRepository;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepo;
    private final TenantRepository tenantRepo;
    private final UserTenantRepository userTenantRepo;

    public CustomOAuth2UserService(UserRepository userRepo,
                                   TenantRepository tenantRepo,
                                   UserTenantRepository userTenantRepo) {
        this.userRepo = userRepo;
        this.tenantRepo = tenantRepo;
        this.userTenantRepo = userTenantRepo;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");
        String sub = oAuth2User.getAttribute("sub"); // Unique Google user ID

        // If sub is null, fallback to email as the googleId (not recommended, but an option)
        String googleId = (sub != null) ? sub : email;

        User user = userRepo.findById(googleId).orElse(null);
        if (user == null) {
            user = new User(googleId, email, name, picture);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            // Set a default tenant (e.g. "T1")
            user.setDefaultTenantId("1");
            userRepo.save(user);

            // Assign this user the role "USER" in every tenant
            List<Tenant> allTenants = tenantRepo.findAll();
            for (Tenant t : allTenants) {
                UserTenant ut = new UserTenant(user, t.getTenantId(), "USER");
                userTenantRepo.save(ut);
            }
        } else {
            // Update basic info if needed
            user.setEmail(email);
            user.setName(name);
            user.setPictureUrl(picture);
            user.setUpdatedAt(LocalDateTime.now());
            userRepo.save(user);
        }

        // Build authorities from userTenant in DB if needed
        // For demonstration, we fetch the single row from userTenant
        // or all rows if you revert to multi-tenant usage
        // Now we get multiple rows
    List<UserTenant> userTenants = userTenantRepo.findByUser_GoogleId(user.getGoogleId());
    // e.g. a user might have multiple roles across multiple tenants

    // Build a set of authorities from all rows
    Set<GrantedAuthority> authorities = new HashSet<>();
    for (UserTenant ut : userTenants) {
        // if your code uses "ut.getRole()" => e.g. "SUPER_ADMIN", "ADMIN", "USER"
        authorities.add(new SimpleGrantedAuthority("ROLE_" + ut.getRole()));
    }
    // If userTenants is empty => a new user with no roles? 
    // Up to you: might default them to "ROLE_USER" or keep them empty.

    return new DefaultOAuth2User(authorities, oAuth2User.getAttributes(), "sub");
    }
}
