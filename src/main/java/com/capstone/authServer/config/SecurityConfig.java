package com.capstone.authServer.config;

import com.capstone.authServer.security.CustomAuthenticationEntryPoint;
import com.capstone.authServer.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomAuthenticationEntryPoint customEntryPoint;

    public SecurityConfig(CustomAuthenticationEntryPoint customEntryPoint) {
        this.customEntryPoint = customEntryPoint;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CustomOAuth2UserService customOAuth2UserService) throws Exception {
        http
          .cors(withDefaults())
          .authorizeHttpRequests(auth -> auth
              // allow these endpoints publicly
              .requestMatchers("/", "/login", "/error", "/css/**","/js/**").permitAll()
              .requestMatchers("/user/me").permitAll()  // <--- allow /user/me
              // everything else => must be authenticated
              .anyRequest().authenticated()
          )
          .exceptionHandling(eh -> eh
              .authenticationEntryPoint(customEntryPoint)
          )
          .oauth2Login(oauth -> oauth
              .loginPage("http://localhost:5173/login")
              .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
              .defaultSuccessUrl("http://localhost:5173/dashboard/", true)
          )
          .sessionManagement(session -> session
              // Typically "IF_REQUIRED" is needed for OAuth2
              .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
          )
          .logout(logout -> logout
              .logoutUrl("/logout")
              .logoutSuccessUrl("http://localhost:5173/login")
              .invalidateHttpSession(true)
              .deleteCookies("JSESSIONID")
          )
          .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
