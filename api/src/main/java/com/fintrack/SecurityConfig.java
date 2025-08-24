package com.fintrack;

import com.fintrack.auth.DevHeaderAuthFilter;
import com.fintrack.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

        private final UserRepository users;

        public SecurityConfig(UserRepository users) {
                this.users = users;
        }

        @Bean
        public SecurityFilterChain api(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .cors(Customizer.withDefaults())
                                .addFilterBefore(new DevHeaderAuthFilter(users),
                                                UsernamePasswordAuthenticationFilter.class)
                                .authorizeHttpRequests(auth -> auth
                                                // Allow preflight
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                                                // open health/static/uploads
                                                .requestMatchers("/actuator/**", "/uploads/**").permitAll()

                                                // EMPLOYEE
                                                .requestMatchers(HttpMethod.POST, "/api/expenses").hasRole("EMPLOYEE")
                                                .requestMatchers(HttpMethod.GET, "/api/expenses/mine")
                                                .hasRole("EMPLOYEE")
                                                .requestMatchers(HttpMethod.POST, "/api/expenses/*/upload-receipt")
                                                .hasRole("EMPLOYEE")

                                                // MANAGER (ONLY managers see pending approvals)
                                                .requestMatchers(HttpMethod.GET, "/api/expenses/pending")
                                                .hasRole("MANAGER")
                                                .requestMatchers(HttpMethod.POST, "/api/expenses/*/approve")
                                                .hasRole("MANAGER")
                                                .requestMatchers(HttpMethod.POST, "/api/expenses/*/reject")
                                                .hasRole("MANAGER")

                                                // ADMIN
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                                                // everything else requires auth
                                                .anyRequest().authenticated());

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration cfg = new CorsConfiguration();
                cfg.setAllowedOrigins(List.of("http://localhost:5173"));
                cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                cfg.setAllowedHeaders(List.of(
                                "Content-Type",
                                "X-Requested-With",
                                "X-User-Email",
                                "X-User-Roles",
                                "Authorization",
                                "Accept",
                                "Origin"));
                cfg.setExposedHeaders(List.of("Location"));
                cfg.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", cfg);
                return source;
        }
}
