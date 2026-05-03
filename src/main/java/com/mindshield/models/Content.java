package com.mindshield.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Content implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private BaseUser author;
    private String title;
    private String body;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Content(BaseUser author, String title, String body) {
        this.id = UUID.randomUUID().toString();
        this.author = author;
        this.title = title;
        this.body = body;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public BaseUser getAuthor() { return author; }
    public boolean isAuthor(BaseUser user) { return this.author != null && this.author.equals(user); }
    public String getPersonaName() { return author.getPersona(); }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = LocalDateTime.now();
    }

    public void setBody(String body) {
        this.body = body;
        this.updatedAt = LocalDateTime.now();
    }

    protected void touchUpdated() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean containsInTitle(String searchTerm) {
        return title.toLowerCase().contains(searchTerm.toLowerCase());
    }

    public boolean containsInBody(String searchTerm) {
        return body.toLowerCase().contains(searchTerm.toLowerCase());
    }
}
