package com.fintrack.auth;

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

        // Allow preflight OPTIONS requests through
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Read the email header we use for dev auth
        String email = request.getHeader("X-User-Email");

        if (StringUtils.hasText(email) &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            User user = users.findByEmail(email).orElse(null);

            if (user != null) {
                // Map DB role names (EMPLOYEE / MANAGER / ADMIN)
                // to Spring Security roles (ROLE_EMPLOYEE / ROLE_MANAGER / ROLE_ADMIN)
                List<GrantedAuthority> auths = user.getRoles().stream()
                        .map(r -> {
                            String springRole = "ROLE_" + r.getName().toUpperCase();
                            return new SimpleGrantedAuthority(springRole);
                        })
                        .collect(Collectors.toList());

                // Debug log for visibility
                System.out.println("Authenticated user=" + email + " with roles=" + auths);

                // Create authentication token
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, auths);

                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                System.out.println("No user found with email=" + email);
            }
        }

        filterChain.doFilter(request, response);
    }
}
