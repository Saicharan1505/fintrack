package com.fintrack.user;

import java.util.List;

public class CurrentUserResponse {
    private Long id;
    private String fullName;
    private String email;
    private List<String> roles;

    public CurrentUserResponse(Long id, String fullName, String email, List<String> roles) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getRoles() {
        return roles;
    }
}
