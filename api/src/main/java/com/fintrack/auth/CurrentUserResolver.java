package com.fintrack.auth;

import com.fintrack.user.User;
import com.fintrack.user.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class CurrentUserResolver {
    private final UserRepository users;

    public CurrentUserResolver(UserRepository users) {
        this.users = users;
    }

    public User resolve(NativeWebRequest req) {
        String email = req.getHeader("X-User-Email");
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Missing X-User-Email header (temporary auth stub).");
        return users.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No such user: " + email));
    }
}
