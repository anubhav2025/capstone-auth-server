package com.capstone.authServer.config;


import com.capstone.authServer.security.CustomAuthenticationEntryPoint;
import com.capstone.authServer.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

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
            .authorizeHttpRequests(auth -> auth
                // Everyone can access the "login" or "oauth2/authorization" endpoints
                .requestMatchers("/", "/login", "/error", "/css/**","/js/**").permitAll()
                // The rest => must be authenticated
                .anyRequest().authenticated()
            )
            // This is key: if user is not authenticated, call our customEntryPoint
            .exceptionHandling(eh -> eh
                .authenticationEntryPoint(customEntryPoint)
            )
            // OAuth2 login
            .oauth2Login(oauth -> oauth
                .loginPage("http://localhost:5173/login")
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .defaultSuccessUrl("http://localhost:5173/dashboard/", true) // after success go dashboard
            )
            // Session management
            .sessionManagement(sess -> sess
                .sessionFixation().migrateSession()
            )
            // logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("http://localhost:5173/login")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )
            .csrf(csrf -> csrf.disable());

        return http.build();
    }

    // @Bean
    // public CustomOAuth2UserService customOAuth2UserService() {
    //     return new CustomOAuth2UserService();
    // }
}
