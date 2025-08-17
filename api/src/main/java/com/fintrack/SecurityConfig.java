package com.fintrack;

import com.fintrack.auth.DevHeaderAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                // Inject dev header-based auth before anonymous auth
                .addFilterBefore(
                        new DevHeaderAuthFilter(),
                        org.springframework.security.web.authentication.AnonymousAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public/lightweight endpoints (health + current-user echo)
                        .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/me").authenticated()

                        // Employee flows
                        .requestMatchers(HttpMethod.GET, "/api/expenses/mine")
                        .hasAnyRole("EMPLOYEE", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/expenses").hasRole("EMPLOYEE")

                        // Manager/Admin flows
                        .requestMatchers(HttpMethod.GET, "/api/expenses/pending").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/expenses/*/approve").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/expenses/*/reject").hasAnyRole("MANAGER", "ADMIN")

                        // Everything else requires auth
                        .anyRequest().authenticated());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        // Vite dev server origin(s)
        cfg.setAllowedOrigins(List.of(
                "http://localhost:5173"
        // add more origins here if needed
        ));

        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Include custom identity headers used by DevHeaderAuthFilter
        cfg.setAllowedHeaders(List.of(
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "X-User-Email",
                "X-User-Roles",
                "Authorization"));

        // If you use redirection/Location header on 201 Created, etc.
        cfg.setExposedHeaders(List.of("Location"));

        // Allow cookies/credentials in dev (fine because origin is explicitly
        // whitelisted)
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
