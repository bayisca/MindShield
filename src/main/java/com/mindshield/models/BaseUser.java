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

    private java.util.List<String> favoriteBlogIds = new java.util.ArrayList<>();
    private java.util.List<String> favoriteSongTitles = new java.util.ArrayList<>();

    public BaseUser(String persona, String password, UserRole role) {
        this.persona = persona;
        this.password = password;
        this.role = role;
        this.registeredAt = LocalDate.now();
    }

    public java.util.List<String> getFavoriteBlogIds() {
        if (favoriteBlogIds == null) favoriteBlogIds = new java.util.ArrayList<>();
        return favoriteBlogIds;
    }

    public java.util.List<String> getFavoriteSongTitles() {
        if (favoriteSongTitles == null) favoriteSongTitles = new java.util.ArrayList<>();
        return favoriteSongTitles;
    }

    public boolean isFavoriteBlog(String postId) {
        return postId != null && getFavoriteBlogIds().contains(postId);
    }

    public boolean toggleFavoriteBlog(String postId) {
        if (postId == null || postId.isBlank()) {
            return false;
        }
        java.util.List<String> favorites = getFavoriteBlogIds();
        if (favorites.contains(postId)) {
            favorites.remove(postId);
            return false;
        }
        favorites.add(postId);
        return true;
    }

    public boolean isFavoriteSong(String songTitle) {
        return songTitle != null && getFavoriteSongTitles().contains(songTitle);
    }

    public boolean toggleFavoriteSong(String songTitle) {
        if (songTitle == null || songTitle.isBlank()) {
            return false;
        }
        java.util.List<String> favorites = getFavoriteSongTitles();
        if (favorites.contains(songTitle)) {
            favorites.remove(songTitle);
            return false;
        }
        favorites.add(songTitle);
        return true;
    }

    public String getPersona() { return persona; }
    public String getPassword() { return password; }
    public void setPersona(String persona) { this.persona = persona; }

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
