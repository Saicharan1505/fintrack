package com.fintrack.user;

import java.util.List;

public record UserMeResponse(
        Long id,
        String fullName,
        String email,
        List<String> roles) {
}
