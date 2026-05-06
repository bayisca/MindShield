package com.mindshield.models;

import java.io.Serializable;
import java.time.LocalDate;

import com.mindshield.ui.UserRole;

public abstract class BaseUser implements Serializable { 
    private static final long serialVersionUID = 1L;

    private String persona;
    private String password;
    private UserRole role;
    /** Hesabın oluşturulduğu tarih (eski kayıtlarda null olabilir). */
    private LocalDate registeredAt;

    public BaseUser(String persona, String password, UserRole role) {
        this.persona = persona;
        this.password = password;
        this.role = role;
        this.registeredAt = LocalDate.now();
    }

    public String getPersona() { return persona; }
    public String getPassword() { return password; }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() { return role; }

    public LocalDate getRegisteredAt() {
        return registeredAt != null ? registeredAt : LocalDate.now();
    }

    public void setRegisteredAt(LocalDate registeredAt) {
        this.registeredAt = registeredAt;
    }

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
