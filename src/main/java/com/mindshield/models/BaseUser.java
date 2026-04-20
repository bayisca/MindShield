package com.mindshield.models;

import com.mindshield.ui.UserRole;

public class BaseUser {
    private String persona;
    private String password;
    private UserRole role;

    public BaseUser(String persona, String password, UserRole role) {
        this.persona = persona;
        this.password = password;
        this.role = role;
    }

    public String getPersona() { return persona; }
    public String getPassword() { return password; }
    public UserRole getRole() { return role; }
}
