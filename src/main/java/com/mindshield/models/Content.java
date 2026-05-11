package com.mindshield.models;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Content {
    private String id;
    private BaseUser author;
    /** forum_topics / forum_replies satırındaki {@code user_id} — yazar nesnesi tutarsız olsa da yetki için. */
    private String persistedAuthorUserId;
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
    public void setId(String id) {
    this.id = id;
}

public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
}
    public String getId() { return id; }
    public BaseUser getAuthor() { return author; }

    public void setPersistedAuthorUserId(String userId) {
        this.persistedAuthorUserId = userId;
    }

    public String getPersistedAuthorUserId() {
        return persistedAuthorUserId;
    }

    public boolean isAuthor(BaseUser user) {
        if (user == null) {
            return false;
        }
        if (persistedAuthorUserId != null && !persistedAuthorUserId.isBlank()
                && persistedAuthorUserId.equals(user.getId())) {
            return true;
        }
        if (author == null) {
            return false;
        }
        return author.getId().equals(user.getId());
    }
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
