package com.fintrack.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class DevHeaderAuthFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String email = request.getHeader("X-User-Email");
        String roles = request.getHeader("X-User-Roles"); // e.g. EMPLOYEE,MANAGER

        if (email != null && !email.isBlank()) {
            var authorities = Arrays.stream(roles != null ? roles.split(",") : new String[0])
                    .map(String::trim)
                    .filter(r -> !r.isEmpty())
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                    .collect(Collectors.toList());

            var auth = new AbstractAuthenticationToken(authorities) {
                @Override
                public Object getCredentials() {
                    return "";
                }

                @Override
                public Object getPrincipal() {
                    return email;
                }
            };
            auth.setAuthenticated(true);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
