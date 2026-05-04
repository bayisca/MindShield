package com.mindshield.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ChatRoom implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String topic;
    private boolean isActive;
    private BaseUser createdBy;
    private List<BaseUser> members;
    private List<ChatMessage> messages;

    public ChatRoom(String name, String topic, BaseUser createdBy) {
        this.id        = UUID.randomUUID().toString();
        this.name      = name;
        this.topic     = topic;
        this.isActive  = true;
        this.createdBy = createdBy;
        this.members   = new ArrayList<>();
        this.messages  = new ArrayList<>();
    }

    // --- Üye İşlemleri ---

    public void addMember(BaseUser user) {
        if (user != null && !isMember(user)) {
            members.add(user);
        }
    }

    public void removeMember(BaseUser user) {
        members.removeIf(m -> m != null && m.equals(user));
    }

    public boolean isMember(BaseUser user) {
        if (user == null) return false;
        return members.stream().anyMatch(m -> m != null && m.equals(user));
    }

    // --- Mesaj İşlemleri ---

    public void addMessage(ChatMessage message) {
        if (message != null) {
            messages.add(message);
        }
    }

    // --- Getterlar ---

    public String getId()          { return id; }
    public String getName()        { return name; }
    public String getTopic()       { return topic; }
    public boolean isActive()      { return isActive; }
    public BaseUser getCreatedBy() { return createdBy; }

    public List<BaseUser> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public List<ChatMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    // --- Admin İşlemleri ---

    public void deactivate() { this.isActive = false; }
    public void activate()   { this.isActive = true; }

    @Override
    public String toString() {
        return name + (isActive ? "" : " [Kapalı]");
    }
}