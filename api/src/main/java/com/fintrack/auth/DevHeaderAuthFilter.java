
package com.fintrack.auth;

import java.util.stream.Collectors;

import com.fintrack.user.User;
import com.fintrack.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class DevHeaderAuthFilter extends OncePerRequestFilter {

    private final UserRepository users;

    public DevHeaderAuthFilter(UserRepository users) {
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // Let preflight through untouched
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Read email from header (dev only)
        String email = request.getHeader("X-User-Email");

        if (StringUtils.hasText(email) && SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = users.findByEmail(email).orElse(null);

            if (user != null) {
                // Map role names (EMPLOYEE/MANAGER/ADMIN) -> ROLE_EMPLOYEE / etc.
                List<GrantedAuthority> auths = user.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName()))
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, auths);

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
