package com.mindshield.models;

import java.io.Serializable;

import com.mindshield.ui.UserRole;

public abstract class BaseUser implements Serializable { 
    private static final long serialVersionUID = 1L;

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

    public abstract void login();

    @Override
    public boolean equals(Object o) { 
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseUser baseUser = (BaseUser) o;
        return persona != null && persona.equals(baseUser.persona);
    }

    @Override
    public int hashCode() {
        return persona != null ? persona.hashCode() : 0;
    }
}
