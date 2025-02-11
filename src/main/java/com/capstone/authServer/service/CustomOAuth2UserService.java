package com.capstone.authServer.service;

import com.capstone.authServer.model.Role;
import com.capstone.authServer.model.User;
import com.capstone.authServer.repository.RoleRepository;
import com.capstone.authServer.repository.UserRepository;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.user.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepo;
    

    private final RoleRepository roleRepo;

    public CustomOAuth2UserService(UserRepository userRepo, RoleRepository roleRepo) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // delegate to default
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = 
            new DefaultOAuth2UserService();

        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // parse user details from google
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        // find user in DB
        User user = userRepo.findByEmail(email);
        if (user == null) {
            user = new User(email, name);
            user.setPictureUrl(picture);

            // default role = USER if new
            Role userRole = roleRepo.findByRoleName("USER");
            if (userRole == null) {
                userRole = new Role("USER");
                roleRepo.save(userRole);
            }
            user.addRole(userRole);
            userRepo.save(user);
        } else {
            // update fields if needed
            user.setName(name);
            user.setPictureUrl(picture);
            userRepo.save(user);
        }

        // build an authority set from user roles
        Set<Role> roles = user.getRoles();
        List<GrantedAuthority> authorities = roles.stream()
        .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getRoleName()))
        .collect(Collectors.toList());

        // We'll store user data in a new custom principal
        return new DefaultOAuth2User(
            authorities,
            oAuth2User.getAttributes(), // or a custom map
            "sub" // or "email"
        );
    }
}
